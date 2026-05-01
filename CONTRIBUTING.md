# Contributing to NetoolHunter

Thanks for considering a contribution. The most common one is **adding a new tool to the catalog**, and that requires only editing one JSON file — no Kotlin, no rebuild, no Android Studio.

## Adding a new tool

### 1. Locate the catalog

The catalog of available tools lives at the **root of this repository**, in [`catalog.json`](catalog.json). It's a single file with a `tools` array.

There's a second copy at `app/src/main/assets/catalog.json` that gets bundled inside the APK. **You don't need to touch the bundled copy in your PR** — the maintainer syncs it before each APK release. You only edit the root `catalog.json`.

### 2. Pick the right installer type

Each tool has an `installer` block describing how it gets installed inside the Kali chroot. Pick the option that matches the tool:

| Installer type | When to use | JSON shape |
|---|---|---|
| **`apt`** | Available in Kali's official repos | `{"type":"apt","pkg":"PACKAGE_NAME"}` |
| **`go`** | Go program installable with `go install` | `{"type":"go","module":"github.com/USER/REPO/cmd/TOOL@latest"}` |
| **`pip`** | Plain Python package on PyPI | `{"type":"pip","pkg":"PACKAGE_NAME"}` |
| **`pipx`** | Python CLI better installed isolated | `{"type":"pipx","source":"PACKAGE_NAME or git+https://..."}` |
| **`git`** | Cloned from GitHub, optionally with build/install steps after | `{"type":"git","repo":"https://github.com/USER/REPO","cloneTo":"/opt/REPO","postInstall":["pip install -r requirements.txt --break-system-packages"]}` |
| **`docker`** | Distributed as a Docker image | `{"type":"docker","image":"namespace/image:tag","runArgs":"-it -p 8000:8000"}` |
| **`script`** | Has its own `curl \| bash` installer | `{"type":"script","curlUrl":"https://example.com/install.sh"}` |

For `git`, `cloneTo` and `postInstall` are optional. Default `cloneTo` is `/opt/<repo-name>`. `postInstall` commands run with `cd $cloneTo` already done.

### 3. Pick a category

Use one of these enum values exactly (case-sensitive):

- `RECON` — Reconnaissance, port scanning, subdomain enum
- `OSINT` — Open source intelligence, social media, lookup
- `WEB` — Web application security, fuzzers, scanners, proxies
- `WIRELESS` — WiFi, Bluetooth, RF
- `EXPLOITATION` — Frameworks, C2, post-exploitation, social engineering
- `AD` — Active Directory, SMB, network protocols
- `PASSWORDS` — Crackers, brute-forcers, wordlist generators
- `ANDROID` — Mobile security, reversing, decompilers
- `FORENSICS` — Memory analysis, file carving, metadata
- `WORDLISTS` — Wordlist and payload collections

### 4. Write the JSON entry

Add an object to the `tools` array. Use this shape:

```json
{
  "id": "lowercase-slug",
  "name": "Display Name",
  "description": "One-line summary in Spanish (the in-app UI is Spanish for now)",
  "category": "WEB",
  "emoji": "🔧",
  "installer": { "type": "apt", "pkg": "package-name" },
  "tags": ["tag1", "tag2"]
}
```

Field rules:

- **`id`** — lowercase, no spaces, ascii-friendly. Used internally and as a stable identifier; if you change it later you'll break detection of already-installed copies on users' devices. Pick carefully.
- **`name`** — display name, any case, can include spaces.
- **`description`** — short one-liner. Keep it Spanish to match the rest of the catalog (the in-app UI is Spanish for now).
- **`category`** — one of the values listed above.
- **`emoji`** — single emoji shown as the tool's icon. Match the vibe of the tool.
- **`installer`** — one of the seven types from the table above.
- **`tags`** — array of lowercase strings used for in-app search. 2–5 tags is plenty.

### 5. Validate locally

You can validate your JSON without building the app:

```bash
# Pretty-print and check parse
python3 -m json.tool catalog.json > /dev/null && echo "OK"

# If you have `jq`:
jq '.tools | length' catalog.json   # should print the new total tool count
jq '.tools[] | .id' catalog.json | sort | uniq -d   # any duplicate ids? should be empty
```

If you have the Android toolchain set up, the unit tests also validate the bundled catalog (parses, unique ids, all categories non-empty, slug-safe ids):

```bash
./gradlew test
```

### 6. Open a PR

Branch, commit, push, open a pull request against `main`:

```bash
git checkout -b add-mytool
git add catalog.json
git commit -m "catalog: add MyTool"
git push -u origin add-mytool
gh pr create --title "catalog: add MyTool" --body "Adds <description of tool> to the <CATEGORY> section."
```

The maintainer reviews and merges. After merge, **users with the app installed can pull the new tool immediately** by tapping the "Refresh catalog" button — no APK update required.

### 7. (Maintainer only) Bundling for new installs

When the maintainer cuts a new APK release, they sync the bundled copy and bump the version:

```bash
cp catalog.json app/src/main/assets/catalog.json
# bump versionCode and versionName in app/build.gradle.kts
./gradlew assembleRelease
gh release create vX.Y.Z --title "..." --notes "..." \
  app/build/outputs/apk/release/app-release.apk
```

New installs get the latest catalog bundled. Existing users only need to tap refresh.

## Style notes

- Keep descriptions terse. One line. Something like "Web fuzzer ultrarrápido" — not "A really fast web fuzzer that lets you do many things".
- Don't duplicate tools that already exist with the same purpose. If you think a replacement is better, open an issue first to discuss.
- For `git` installers with `postInstall`, prefer commands that are idempotent (re-running them shouldn't break the install).
- For Python tools, prefer `pipx` over `pip` when the tool exposes a CLI — pipx isolates dependencies and avoids polluting the system Python.

## Other contributions

- **Bug reports** — open an issue with the `adb logcat -s NetoolHunter` output and the steps to reproduce.
- **UI translations** — `strings.xml` is currently Spanish-only. Adding `values-en/strings.xml` (or other locales) is welcome.
- **Architecture changes** — open an issue first to discuss before sending a big PR. Read [`CLAUDE.md`](CLAUDE.md) to understand the non-negotiable rules.

## License

By contributing, you agree your changes are licensed under [GPL-3.0](LICENSE), the same license as the project.
