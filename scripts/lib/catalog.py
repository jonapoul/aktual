#!/usr/bin/env python3
# Prints search terms for the version catalog entries that changed since the given git ref, one per line.
# Each entry gives its accessor (e.g. libs.ktor.core), and libraries also give their quoted path (e.g.
# "ktor.core") for libs["..."] lookups. Libraries and plugins count as changed when their version.ref does,
# and bundles when one of their libraries does.
# Usage: catalog.py <base-ref>

import subprocess
import sys
import tomllib

CATALOG = "gradle/libs.versions.toml"


def load_base(ref):
  text = subprocess.run(
    ["git", "show", f"{ref}:{CATALOG}"], capture_output=True, text=True, check=True
  ).stdout
  return tomllib.loads(text)


def version_ref(entry):
  if isinstance(entry, dict) and isinstance(entry.get("version"), dict):
    return entry["version"].get("ref")
  return None


def main():
  old = load_base(sys.argv[1])
  with open(CATALOG, "rb") as f:
    new = tomllib.load(f)

  def changed(section, also=lambda key: False):
    o, n = old.get(section, {}), new.get(section, {})
    return {k for k in o.keys() | n.keys() if o.get(k) != n.get(k) or also(k)}

  versions = changed("versions")

  def uses_changed_version(section):
    return lambda k: any(
      version_ref(d.get(section, {}).get(k)) in versions for d in (old, new)
    )

  libraries = changed("libraries", uses_changed_version("libraries"))
  plugins = changed("plugins", uses_changed_version("plugins"))
  bundles = changed(
    "bundles", lambda k: bool(libraries & set(new.get("bundles", {}).get(k, [])))
  )

  terms = set()
  for prefix, keys in (
    ("libs.", libraries),
    ("libs.plugins.", plugins),
    ("libs.bundles.", bundles),
    ("libs.versions.", versions),
  ):
    for key in keys:
      path = key.replace("-", ".").replace("_", ".")
      terms.add(prefix + path)
      if prefix == "libs.":
        terms.add(f'"{path}"')

  print("\n".join(sorted(terms)))


main()
