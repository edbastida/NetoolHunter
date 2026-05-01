# NetoolHunter

Gestor visual Android para herramientas de pentesting que viven en el chroot Kali NetHunter. Permite instalar, desinstalar, lanzar y gestionar 92 herramientas (recon, web, wireless, exploitation, AD, passwords, forensics, wordlists…) desde una UI nativa, en lugar de tirar comandos a mano dentro del chroot.

App **100 % offline**: no telemetría, no tracking, no analíticas, no red. El catálogo de herramientas está hardcoded — modificarlo requiere recompilar.

## Capturas

_(añade screenshots aquí cuando los tengas)_

## Requisitos

- **Android 8.0+** (API 26).
- **Device rooteado** con un manager soportado:
  - **KernelSU Next** (recomendado — incluye plantilla `nethunter.root` lista para usar).
  - KernelSU clásico, Magisk, APatch.
- **NetHunter Chroot** instalado y funcionando (módulo Magisk/KSU + chroot Kali en `/data/local/nhsystem/kali-arm64`).
- ~5 GB libres en `/data` para los paquetes base que instala (`git`, `golang`, `python3-pip`, `pipx`, `docker.io`).

### Configuración inicial del manager de root

NetoolHunter ejecuta comandos en el chroot vía `nh -r` o `bootkali_bash` (o `chroot` directo como fallback). Para que el shell del app vea los binarios del módulo NetHunter, el manager de root tiene que aplicar el **mount namespace global + capabilities CAP_SYS_CHROOT**.

- **KernelSU Next**: KernelSU Next manager → Apps con superusuario → NetoolHunter → Perfil de Aplicación → tab "Plantilla" → seleccionar **`nethunter.root`** → forzar detención de la app y reabrir.
- **KernelSU clásico**: Mount Namespace Mode → Global Mount + capabilities CAP_SYS_CHROOT, CAP_SYS_ADMIN.
- **Magisk**: confirmar que NetoolHunter está autorizada en SuperUser y que NetHunter está instalado como módulo Magisk.

La propia app detecta tu manager y muestra los pasos exactos en pantalla si el chroot no responde.

## Instalación

### Vía APK release

Descarga el APK firmado desde la pestaña [Releases](../../releases). Instálalo:

```bash
adb install app-release.apk
```

Aplica la configuración del manager de root descrita arriba.

### Compilando desde código fuente

```bash
git clone https://github.com/edbastida/NetoolHunter.git
cd NetoolHunter
./gradlew assembleDebug          # APK debug en app/build/outputs/apk/debug/
./gradlew assembleRelease        # APK release (requiere keystore.properties — ver abajo)
```

Para `assembleRelease` necesitas un keystore propio. Genera uno y crea `keystore.properties` (gitignored):

```bash
mkdir -p keystore
keytool -genkey -v -keystore keystore/netoolhunter-release.jks \
  -alias netoolhunter -keyalg RSA -keysize 2048 -validity 10000

cp keystore.properties.template keystore.properties
# Edita keystore.properties con tu password
```

## Stack técnico

- Kotlin 2.0 + Jetpack Compose (BOM 2024.10)
- Material 3 + Navigation Compose
- DataStore Preferences + Coroutines/Flow
- Sin DI framework (constructor injection manual)
- Sin DB, sin red

## Arquitectura

```
domain/      Modelos puros (Tool, Category, Installer sealed, Repo, InstallEvent)
data/        ToolsCatalog (92 tools hardcoded), DefaultRepos, ReposRepository,
             InstalledRepository, PrerequisitesChecker, RootManagerDetector
shell/       ShellExecutor (Flow<InstallEvent>), KaliEntryPoint (resolución
             nh / bootkali / raw chroot), KaliCommand, RootChecker, TerminalBus,
             InstallForegroundService
ui/screens/  Pareja Screen.kt + ViewModel.kt por pantalla (Tools, Installed,
             Prerequisites, Repos, Terminal)
ui/components/   ToolCard, TerminalView, ConfirmDialog, …
ui/navigation/   Routes (sealed), BottomBar, NavHost
ui/theme/    Color (KaliBlue + dark forzado), Type, Theme
```

Toda comunicación entre `ShellExecutor` y la UI pasa por `TerminalBus` (singleton `MutableSharedFlow<InstallEvent>`). Las instalaciones largas viven en un Foreground Service para sobrevivir al lock screen.

## Reglas no negociables

1. Comandos del chroot SIEMPRE vía `ShellExecutor.execInKali` (resuelve el wrapper en runtime — `nh`, `bootkali_bash`, o `chroot` raw como fallback).
2. Antes de tocar `sources.list`, backup `.bak` automático.
3. Confirm dialog obligatorio antes de: aplicar repos, desinstalar tool, instalar prerequisitos.
4. Catálogo hardcoded. NO se carga desde JSON/red.
5. Sin tracking, analytics ni crash reporting de terceros. Privacidad total.
6. Material 3 + dark forzado.

## Catálogo

92 herramientas en 10 categorías:

| Categoría | Ejemplos |
|---|---|
| Reconocimiento | nmap, masscan, amass, theharvester |
| OSINT | sherlock, socialscan, holehe |
| Web | burpsuite, gobuster, ffuf, sqlmap, nikto |
| Wireless | aircrack-ng, kismet, wifite, reaver |
| Explotación | metasploit, exploitdb, set, beef-xss |
| Red / AD | impacket, crackmapexec, responder, bloodhound |
| Contraseñas | hydra, john, hashcat, medusa, crunch |
| Android / Reversing | apktool, jadx, frida, radare2, ghidra |
| Forense | volatility, autopsy, foremost, binwalk |
| Wordlists / Payloads | seclists, rockyou, payloadsallthethings |

Lista completa en `app/src/main/kotlin/com/netoolhunter/app/data/ToolsCatalog.kt`.

## Licencia

[GPL-3.0](LICENSE). Compatible con el ecosistema Kali (también GPL).

## Disclaimer

Esta herramienta está pensada para **pentesting autorizado**, CTFs, investigación de seguridad defensiva, y entornos educativos. El uso contra sistemas para los que no tengas permiso explícito es ilegal en la mayoría de jurisdicciones. El autor no se responsabiliza del mal uso.
