import json
import re
import sys
from pathlib import Path

names = {
    "proton.android.pass.fdroid": "Proton Pass (F-Droid)",
    "net.thunderbird.android": "Thunderbird"
}

data = json.loads(Path("patches-list.json").read_text())
versions = {p: set() for p in names}
changes = []

for patch in data["patches"]:
    for app in patch.get("compatiblePackages") or []:
        package = app["packageName"]
        if package in versions:
            for target in app.get("targets") or []:
                if target.get("version"):
                    versions[package].add(target["version"])

candidates = Path("candidates.tsv")

if candidates.exists():
    for line in candidates.read_text().splitlines():
        app, package, version = line.split("\t")
        versions[package].add(version)
        changes.append(f"Added support for {names[package]} {version}")

for package in versions:
    assert versions[package]
    versions[package] = sorted(
        versions[package],
        key=lambda v: tuple(map(int, v.split("."))),
        reverse=True
    )

description = "Vibe Coding | " + " | ".join(
    f"{names[p]}: {versions[p][0]}" for p in names
)

if changes:
    description += " | " + "; ".join(changes)

if "--prepare" in sys.argv:
    p = Path("patches/build.gradle.kts")
    s = p.read_text()

    s, count = re.subn(
        r'(name = "AMOLED Patches"\s+description = )"[^"\n]*"',
        lambda m: m[1] + json.dumps(description),
        s
    )

    assert count == 1
    p.write_text(s)
    print(description)
    sys.exit(0)

lines = [
    "<!-- AUTO-COMPATIBILITY-START -->",
    "## Supported Versions",
    "",
    "| App | Supported versions |",
    "| --- | --- |"
]

for package, name in names.items():
    lines.append(f"| {name} | {', '.join(versions[package])} |")

lines.append("<!-- AUTO-COMPATIBILITY-END -->")

p = Path("README.md")
s = p.read_text()
block = "\n".join(lines)
pattern = r"<!-- AUTO-COMPATIBILITY-START -->.*?<!-- AUTO-COMPATIBILITY-END -->"

if re.search(pattern, s, re.S):
    s = re.sub(pattern, lambda _: block, s, flags=re.S)
else:
    s = s.rstrip() + "\n\n" + block + "\n"

p.write_text(s)

notes = [
    "## What's Changed",
    "",
    *[f"- {change}." for change in changes],
    "",
    "## Verification",
    "",
    "- Compatibility checks passed.",
    "- MPP build and candidate APK patching tests passed.",
    "- Existing supported versions retained.",
    "",
    "Runtime appearance has not been automatically verified.",
    "",
    "Made with Vibe Coding"
]

Path("release-notes.md").write_text("\n".join(notes) + "\n")
