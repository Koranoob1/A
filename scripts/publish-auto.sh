#!/usr/bin/env bash
set -euo pipefail

test -s candidates.tsv

OLD=$(jq -er '.version' patches-bundle.json)
IFS=. read -r MAJOR MINOR PATCH <<< "$OLD"
VERSION="$MAJOR.$MINOR.$((PATCH+1))"

if gh release view "v$VERSION" >/dev/null 2>&1; then
  echo "Release already exists"
  exit 1
fi

while IFS=$'\t' read -r app package version; do
  python3 scripts/add-target.py "$app" "$version"
done < candidates.tsv

sed -i "s/^version = .*/version = \"$VERSION\"/" patches/build.gradle.kts

DESCRIPTION=$(python3 scripts/update-docs.py --prepare)

chmod +x gradlew

./gradlew :patches:buildAndroid \
  :patches:generatePatchesList --no-daemon

MPP=$(find patches/build/libs -maxdepth 1 \
  -name '*.mpp' ! -name '*sources*' ! -name '*javadoc*')

test "$(printf '%s\n' "$MPP" | grep -c .)" -eq 1

cp "$MPP" "patches-$VERSION.mpp"

unzip -p "patches-$VERSION.mpp" META-INF/MANIFEST.MF |
  tr -d '\r' | grep -Fx "Version: $VERSION"

test "$(jq -er '.version' patches-list.json)" = "$VERSION"

python3 - "patches-$VERSION.mpp" "$DESCRIPTION" <<'VERIFY'
import sys
import zipfile

with zipfile.ZipFile(sys.argv[1]) as z:
    manifest = z.read("META-INF/MANIFEST.MF").decode()
    manifest = manifest.replace("\r\n", "\n").replace("\n ", "")
    assert f"Description: {sys.argv[2]}" in manifest.splitlines()

print("PASS: Bundle description verified")
VERIFY


while IFS=$'\t' read -r app package version; do

  java -jar morphe-desktop.jar patch "$app.apk" \
    --patches="patches-$VERSION.mpp" \
    --out="$app-patched.apk" \
    --result-file="$app-result.json" \
    --validate --unsigned

  jq -e --arg p "$package" --arg v "$version" '
    .success == true and
    .packageName == $p and
    .packageVersion == $v and
    .appliedPatches == ["Pure AMOLED"] and
    .failedPatches == [] and
    all(.stepResults[]; .success == true)
  ' "$app-result.json"

  test -s "$app-patched.apk"

  jq -e --arg p "$package" --arg v "$version" '
    any(.patches[].compatiblePackages[]?;
      .packageName == $p and
      any(.targets[]?; .version == $v))
  ' patches-list.json

done < candidates.tsv

jq -n \
  --arg description "$DESCRIPTION" \
  --arg version "$VERSION" \
  --arg date "$(date -u +%Y-%m-%dT%H:%M:%S)" \
  --arg url "https://github.com/$GITHUB_REPOSITORY/releases/download/v$VERSION/patches-$VERSION.mpp" \
  '{created_at:$date,description:$description,download_url:$url,signature_download_url:"",version:$version}' \
  > patches-bundle.json

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"

git add patches/src/main/kotlin/app/passblack \
  patches/build.gradle.kts patches-bundle.json

python3 scripts/update-docs.py
git add README.md
git add -f patches-list.json

git commit -m "Automatic compatibility release $VERSION"

git archive --format=zip \
  --output="AMOLED-Patches-$VERSION-source.zip" HEAD

git tag "v$VERSION"
git push origin "refs/tags/v$VERSION"

gh release create "v$VERSION" \
  "patches-$VERSION.mpp" \
  patches-list.json \
  "AMOLED-Patches-$VERSION-source.zip" \
  --verify-tag \
  --title "AMOLED Patches $VERSION" \
  --notes-file release-notes.md

git push origin HEAD:main
