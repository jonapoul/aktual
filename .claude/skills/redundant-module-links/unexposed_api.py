#!/usr/bin/env python3
"""Find `api(project(...))` dependencies whose types the module doesn't expose.

Reads Kotlin metadata from each module's compiled main classes (via Exposed.java) to find which
project modules appear in its public API, then reports `api` links that could be `implementation`.
Needs fresh `compileAll` output and `atlasGenerate` files. Pass --apply to flip them.
"""

import collections
import glob
import json
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
CACHE = os.path.expanduser("~/.gradle/caches/modules-2/files-2.1")


def version_key(path):
  return [int(x) if x.isdigit() else x for x in re.split(r"[.-]", path.split("/")[-3])]


def jar(group, artifact):
  found = [
    j
    for j in glob.glob(f"{CACHE}/{group}/{artifact}/*/*/{artifact}-*.jar")
    if os.path.basename(j) == f"{artifact}-{j.split('/')[-3]}.jar"
  ]
  if not found:
    sys.exit(f"Couldn't find {group}:{artifact} in the Gradle cache")
  return max(found, key=version_key)


def is_api(configs):
  return any(c.lower().endswith("api") for c in configs)


def build_file(module):
  return module.strip(":").replace(":", "/") + "/build.gradle.kts"


def load_links():
  links = collections.defaultdict(dict)
  dirs = {}
  for f in glob.glob("**/build/atlas/project-links.json", recursive=True):
    d = os.path.dirname(os.path.dirname(os.path.dirname(f)))
    if d:
      dirs[":" + d.replace("/", ":")] = d
    with open(f) as fh:
      for link in json.load(fh):
        links[link["fromPath"]].setdefault(link["toPath"], set()).add(link["configuration"])
  return links, dirs


def exposed_modules(dirs):
  classpath = ":".join(
    [
      jar("org.ow2.asm", "asm"),
      jar("org.jetbrains.kotlin", "kotlin-metadata-jvm"),
      jar("org.jetbrains.kotlin", "kotlin-stdlib"),
    ]
  )
  args = [f"{m}={d}" for m, d in dirs.items()]
  out = subprocess.run(
    ["java", "-cp", classpath, os.path.join(HERE, "Exposed.java"), ".", *args],
    capture_output=True,
    text=True,
  )
  if out.returncode != 0:
    sys.exit(out.stderr)
  out = out.stdout
  exposed = collections.defaultdict(dict)
  for line in out.splitlines():
    module, other, where = line.split("\t")
    exposed[module].setdefault(other, where)
  return exposed


def closure(links, start):
  seen, stack = set(), [start]
  while stack:
    for target, configs in links.get(stack.pop(), {}).items():
      if is_api(configs) and target not in seen:
        seen.add(target)
        stack.append(target)
  return seen


GRAPH_MODULES = [":aktual-app:android", ":aktual-app:desktop"]


def contributing_modules(dirs):
  return {
    m
    for m, d in dirs.items()
    if glob.glob(f"{d}/build/classes/kotlin/*/main/metro/hints/*")
    or glob.glob(f"{d}/build/classes/kotlin/main/metro/hints/*")
  }


def graph_reach(links):
  reach = set()
  for graph in GRAPH_MODULES:
    for target in links[graph]:
      reach |= {target} | closure(links, target)
  return reach


def main():
  links, dirs = load_links()
  exposed = exposed_modules(dirs)

  depth = {}

  def depth_of(m):
    if m not in depth:
      depth[m] = 0
      depth[m] = 1 + max((depth_of(t) for t in links[m]), default=-1)
    return depth[m]

  # Metro only sees contributions from modules on the graph module's compile classpath, and a
  # missing one fails at runtime rather than compile time
  contributing = contributing_modules(dirs)
  reachable = graph_reach(links) & contributing

  # Bottom-up, so a flip lower down shrinks the api reach seen by modules above it
  flips, kept = [], []
  for module in sorted(dirs, key=lambda m: (depth_of(m), m)):
    if module.startswith(":aktual-app"):
      continue
    path = build_file(module)
    text = open(path).read() if os.path.exists(path) else ""
    for dep, configs in sorted(links[module].items()):
      if not is_api(configs) or text.count(f'api(project("{dep}"))') != 1:
        continue
      if exposed[module].keys() & ({dep} | closure(links, dep)):
        continue
      links[module][dep] = {"implementation"}
      lost = reachable - graph_reach(links)
      if lost:
        links[module][dep] = configs
        kept.append((module, dep, lost))
        continue
      flips.append((module, dep))

  for module, dep in flips:
    print(f"{build_file(module)}: {dep}")
  if kept:
    print("\nNot exposed, but kept as api so the app graphs still see these contributions:")
    for module, dep, lost in kept:
      print(f"  {build_file(module)}: {dep}  ({', '.join(sorted(lost))})")
  s = "" if len(flips) == 1 else "s"
  print(f"\n{len(flips)} api link{s} could be implementation")

  if "--apply" in sys.argv:
    for module, dep in flips:
      path = build_file(module)
      with open(path) as f:
        text = f.read()
      with open(path, "w") as f:
        f.write(text.replace(f'api(project("{dep}"))', f'implementation(project("{dep}"))'))
    print("Applied")


if __name__ == "__main__":
  main()
