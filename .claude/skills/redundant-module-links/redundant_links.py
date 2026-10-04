#!/usr/bin/env python3
"""Find project dependencies already exposed through another dependency's `api` chain.

Reads the `build/atlas/project-links.json` files written by `./gradlew atlasGenerate`, so run that
first. Pass --apply to delete the redundant lines from the build files.
"""

import collections
import glob
import json
import os
import sys


def is_api(configs):
  return any(c.lower().endswith("api") for c in configs)


def is_common(configs):
  # An androidMain link only exposes its target to android, so it can't stand in for a common one
  return any(not c.startswith("android") for c in configs)


def build_file(module):
  return module.strip(":").replace(":", "/") + "/build.gradle.kts"


def declarations(module, dependency):
  path = build_file(module)
  if not os.path.exists(path):
    return []
  with open(path) as f:
    return [(i + 1, line.strip()) for i, line in enumerate(f) if f'project("{dependency}")' in line]


def load_links():
  links = collections.defaultdict(dict)
  files = glob.glob("**/build/atlas/project-links.json", recursive=True)
  if not files:
    sys.exit("No project-links.json files found - run ./gradlew atlasGenerate first")
  for file in files:
    with open(file) as f:
      for link in json.load(f):
        links[link["fromPath"]].setdefault(link["toPath"], set()).add(link["configuration"])
  return links


def api_closure(links, start):
  paths = {}
  stack = [(start, [start])]
  while stack:
    node, path = stack.pop()
    for target, configs in links[node].items():
      if is_api(configs) and is_common(configs) and target not in paths:
        paths[target] = path + [target]
        stack.append((target, paths[target]))
  return paths


def find_redundant(links):
  closures = {module: api_closure(links, module) for module in list(links)}
  redundant, undeclared = [], []
  for module in sorted(links):
    for dependency, configs in sorted(links[module].items()):
      routes = [
        closures[sibling][dependency]
        for sibling, sibling_configs in links[module].items()
        if sibling != dependency
        and is_common(sibling_configs)
        and dependency in closures.get(sibling, {})
        # An api link is only redundant if the module still re-exports it afterwards
        and (not is_api(configs) or is_api(sibling_configs))
      ]
      if not routes:
        continue
      route = min(routes, key=len)
      declared = declarations(module, dependency)
      if len(declared) == 1:
        redundant.append((module, dependency, declared[0], route))
      else:
        undeclared.append((module, dependency, route))
  return redundant, undeclared


def short(module):
  return module.replace(":aktual-", "")


def main():
  links = load_links()
  redundant, undeclared = find_redundant(links)

  current = None
  for module, _, (line, text), route in redundant:
    if module != current:
      print(f"\n{build_file(module)}")
      current = module
    print(f"  L{line} {text}  <- {' > '.join(short(m) for m in route)}")

  if undeclared:
    print("\nNot declared in the build file (added by a convention plugin), left alone:")
    for module, dependency, route in undeclared:
      print(f"  {short(module)} -> {short(dependency)}  <- {' > '.join(short(m) for m in route)}")

  print(f"\n{len(redundant)} redundant link(s)")

  if "--apply" in sys.argv and redundant:
    lines_by_file = collections.defaultdict(set)
    for module, _, (line, _), _ in redundant:
      lines_by_file[build_file(module)].add(line)
    for path, remove in lines_by_file.items():
      with open(path) as f:
        lines = f.readlines()
      with open(path, "w") as f:
        f.writelines(l for i, l in enumerate(lines) if i + 1 not in remove)
    print(f"Removed from {len(lines_by_file)} build file(s)")


if __name__ == "__main__":
  main()
