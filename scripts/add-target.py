import sys
import re
from pathlib import Path

app, version = sys.argv[1:]

if not re.fullmatch(r"[0-9]+(?:\.[0-9]+)+", version):
    raise ValueError("Invalid version")

root = Path("patches/src/main/kotlin/app/passblack/patches")

paths = {
    "proton-pass": root / "shared/Constants.kt",
    "thunderbird": root / "thunderbird/ThunderbirdAmoledPatch.kt"
}

p = paths[app]
s = p.read_text()
target = f'AppTarget(version = "{version}")'

if target not in s:
    s, count = re.subn(
        r'(targets\s*=\s*listOf\()',
        lambda m: m[0] + "\n                " + target + ",",
        s
    )
    assert count == 1
    p.write_text(s)

print(f"Added {app}: {version}")
