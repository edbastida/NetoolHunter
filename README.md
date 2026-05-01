# NetoolHunter

Visual Android manager for the pentest tools shipped with the Kali NetHunter chroot. Install, uninstall and track 92 tools across recon, OSINT, web, wireless, exploitation, AD, passwords, Android/reversing, forensics and wordlists from a native UI — instead of typing commands by hand inside the chroot.

**Fully offline.** No telemetry, no tracking, no analytics, no network calls. The tool catalog is hard-coded; modifying it requires recompiling.

> The in-app UI is currently Spanish-only. Contributions to translate the strings to other languages are welcome.

## Screenshots

_(add screenshots here when you have them)_

## Requirements

- **Android 8.0+** (API 26).
- **Rooted device** with one of the supported root managers:
  - **KernelSU Next** (recommended — ships a ready-to-use `nethunter.root` template).
  - Classic KernelSU, Magisk, APatch.
- **NetHunter chroot** installed and working (Magisk/KSU module + Kali chroot at `/data/local/nhsystem/kali-arm64`).
- ~5 GB free in `/data` for the base packages it installs (`git`, `golang`, `python3-pip`, `pipx`, `docker.io`).

### Root manager setup

NetoolHunter runs commands inside the chroot via `nh -r`, `bootkali_bash`, or a raw `chroot` fallback. For the app's su context to see the NetHunter module's binaries, your root manager must apply a **global mount namespace + CAP_SYS_CHROOT** to NetoolHunter.

- **KernelSU Next**: KernelSU Next manager → SuperUser apps → NetoolHunter → App Profile → "Template" tab → select **`nethunter.root`** → force-stop the app and reopen.
- **Classic KernelSU**: Mount Namespace Mode → Global Mount; capabilities CAP_SYS_CHROOT, CAP_SYS_ADMIN.
- **Magisk**: confirm NetoolHunter is granted in SuperUser and that NetHunter is installed as a Magisk module.
- **APatch**: App Profile → namespace Global + capabilities including CAP_SYS_CHROOT.

The app auto-detects your manager and displays the exact steps on the prerequisites screen if the chroot can't be reached.

## Installation

### Pre-built APK

Download the signed APK from the [Releases](../../releases) tab and install:

```bash
adb install app-release.apk
```

Then apply the root manager configuration described above.

### Build from source

```bash
git clone https://github.com/edbastida/NetoolHunter.git
cd NetoolHunter
./gradlew assembleDebug          # debug APK in app/build/outputs/apk/debug/
./gradlew assembleRelease        # release APK (requires keystore.properties — see below)
```

For `assembleRelease` you need your own keystore. Generate one and create `keystore.properties` (gitignored):

```bash
mkdir -p keystore
keytool -genkey -v -keystore keystore/netoolhunter-release.jks \
  -alias netoolhunter -keyalg RSA -keysize 2048 -validity 10000

cp keystore.properties.template keystore.properties
# Edit keystore.properties with your password
```

## Tech stack

- Kotlin 2.0 + Jetpack Compose (BOM 2024.10)
- Material 3 + Navigation Compose
- DataStore Preferences + Coroutines/Flow
- No DI framework (manual constructor injection)
- No database, no network

## Architecture

```
domain/      Pure models (Tool, Category, Installer sealed, Repo, InstallEvent)
data/        ToolsCatalog (92 tools, hard-coded), DefaultRepos, ReposRepository,
             InstalledRepository, PrerequisitesChecker, RootManagerDetector
shell/       ShellExecutor (Flow<InstallEvent>), KaliEntryPoint (resolves
             nh / bootkali / raw chroot at runtime), KaliCommand, RootChecker,
             TerminalBus, InstallForegroundService
ui/screens/  One Screen.kt + ViewModel.kt pair per screen (Tools, Installed,
             Prerequisites, Repos, Terminal)
ui/components/   ToolCard, TerminalView, ConfirmDialog, …
ui/navigation/   Routes (sealed), BottomBar, NavHost
ui/theme/    Color (KaliBlue + forced dark), Type, Theme
```

All communication between `ShellExecutor` and the UI goes through `TerminalBus` (a singleton `MutableSharedFlow<InstallEvent>`). Long installs run inside a Foreground Service so they survive the lock screen.

## Non-negotiable rules

1. Chroot commands ALWAYS go through `ShellExecutor.execInKali` — it resolves the wrapper at runtime (`nh`, `bootkali_bash`, or raw `chroot` as last fallback).
2. Before touching `sources.list`, an automatic `.bak` backup is written.
3. Confirmation dialog is mandatory before: applying repo changes, uninstalling a tool, installing prerequisites.
4. Catalog is hard-coded. Never loaded from JSON/network.
5. No tracking, analytics, or third-party crash reporting. Privacy-first.
6. Material 3 + forced dark theme.

## Catalog

92 tools across 10 categories:

| Category | Examples |
|---|---|
| Reconnaissance | nmap, masscan, amass, theharvester |
| OSINT | sherlock, socialscan, holehe |
| Web | burpsuite, gobuster, ffuf, sqlmap, nikto |
| Wireless | aircrack-ng, kismet, wifite, reaver |
| Exploitation | metasploit, exploitdb, set, beef-xss |
| Network / AD | impacket, crackmapexec, responder, bloodhound |
| Passwords | hydra, john, hashcat, medusa, crunch |
| Android / Reversing | apktool, jadx, frida, radare2, ghidra |
| Forensics | volatility, autopsy, foremost, binwalk |
| Wordlists / Payloads | seclists, rockyou, payloadsallthethings |

Full list in `app/src/main/kotlin/com/netoolhunter/app/data/ToolsCatalog.kt`.

## License

[GPL-3.0](LICENSE). Compatible with the Kali ecosystem (also GPL).

## Disclaimer

This tool is meant for **authorized penetration testing**, CTFs, defensive security research and educational environments. Using it against systems you don't have explicit permission to test is illegal in most jurisdictions. The author is not responsible for misuse.
