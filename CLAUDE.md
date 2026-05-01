# NetoolHunter

Gestor visual Android para herramientas de pentesting en Kali NetHunter chroot.

## Commands

- `./gradlew assembleDebug` — Build APK debug
- `./gradlew assembleRelease` — Build APK release (sin firmar)
- `./gradlew installDebug` — Build + install en device conectado
- `./gradlew test` — Unit tests (JVM)
- `./gradlew lint` — Android Lint
- `adb logcat -s NetoolHunter` — Ver logs runtime

## Tech Stack

Kotlin 2.0 + Jetpack Compose (BOM 2024.10) + Material 3 + Navigation Compose + DataStore + Coroutines/Flow. minSdk 26, targetSdk 34. Sin DI framework, sin DB, sin red.

## Architecture

### Estructura
- `domain/` — Modelos puros (Tool, Category, Installer sealed, Repo, InstallEvent). Sin deps Android.
- `data/` — ToolsCatalog (hardcoded), DefaultRepos, ReposRepository, InstalledRepository, PrerequisitesChecker.
- `shell/` — ShellExecutor (Flow<InstallEvent>), KaliCommand (Installer→String), RootChecker, TerminalBus, InstallForegroundService.
- `ui/screens/<screen>/` — Pareja Screen.kt + ViewModel.kt por pantalla.
- `ui/components/` — Componentes reutilizables (ToolCard, TerminalView, etc.).
- `ui/navigation/` — Routes (sealed), BottomBar, NavHost.
- `ui/theme/` — Color, Type, Theme.

### Data flow
```
ToolCard.onInstall(tool)
  → ToolsViewModel.install(tool)
    → InstallForegroundService.start(KaliCommand.install(tool.installer))
      → ShellExecutor.execInKali(cmd) emite Flow<InstallEvent>
        → TerminalBus.emit(event)
          → TerminalViewModel colecta y actualiza state.lines
            → TerminalView lo renderiza
```

`TerminalBus` es un `object` singleton con `MutableSharedFlow<InstallEvent>`. Es el único state global. Todo lo demás vive en ViewModels.

### Key patterns
- Un ViewModel por pantalla, expone `StateFlow<UiState>`.
- Compose recolecta con `collectAsStateWithLifecycle()`.
- Comandos shell siempre via `KaliCommand.install/uninstall/isInstalled` — nunca strings inline.
- `ShellExecutor.execInKali` envuelve con `nh -r '...'`. Nunca llamar `exec` directo para comandos del chroot.
- Detección de instalado se hace **on-demand** en InstalledScreen, no se cachea entre arranques.

## Code Organization Rules

1. **Una clase por archivo.** Max 300 líneas. Si supera, extraer.
2. **Package alias:** importa absoluto `com.netoolhunter.app.*`. No barrel exports.
3. **Composables stateless.** Estado en ViewModel, pasa down como parámetros, callbacks up.
4. **Sin `LiveData`.** Solo `StateFlow`/`SharedFlow`.
5. **Sin Hilt/Koin.** Constructor injection manual desde `MainActivity`/`NetoolHunterApp`.
6. **Strings en `strings.xml`** — sin hardcodear en composables (excepto emojis/símbolos).
7. **Operaciones largas en `viewModelScope` con `Dispatchers.IO`.**

## Design System

### Colors
- Primary: `#367BF0` (KaliBlue)
- Background: `#0E0E10`
- Surface: `#1A1A1F`
- Text primary: `#FFFFFF`
- Text secondary: `#B3B3B8`
- Border: `#2E2E36`
- Terminal stdout: `#00FF41` (verde fosforito)
- Terminal stderr: `#FF3B30`
- Success: `#34C759`, Error: `#FF3B30`, Warning: `#FF9500`

### Typography
- UI: Roboto (default Android)
- Terminal: `FontFamily.Monospace` (fallback). Para usar JetBrains Mono real, dropear `jetbrains_mono.ttf` en `app/src/main/res/font/` y cambiar `TerminalFont` en `ui/theme/Type.kt`.
- Sizes: 11/13/16/24/32 sp

### Style
- Border radius: 12dp cards, 8dp botones, 20dp chips, 0dp terminal
- Spacing base: 4dp; usar 4/8/12/16/24/32
- Sin shadows. Border 1dp en cards.
- Iconos en cards = emojis grandes (no Material Icons)
- Edge-to-edge, dark forzado (sin light theme)

## Environment Variables

Ninguna. App 100% offline.

## Reglas No Negociables

1. **Todos los comandos del chroot van por `nh -r`** vía `ShellExecutor.execInKali`. Nunca `exec` directo a apt/pip/etc.
2. **Antes de escribir `sources.list`, hacer backup a `.bak`.** El método `applyAndStream()` ya lo hace; no lo bypasses.
3. **Confirm dialog obligatorio** antes de: aplicar cambios de repos, desinstalar tool, instalar prerequisitos.
4. **TerminalBus es la única forma de mostrar output.** No leer streams desde Composables.
5. **Catálogo vive en `app/src/main/assets/catalog.json`** (bundleado al APK) y opcionalmente en `filesDir/catalog.json` (descargado por el user). `CatalogRepository` carga el descargado si existe, si no el bundleado. La única operación de red de toda la app es `CatalogRepository.updateFromUrl()`, que se invoca **solo a petición del user** (botón "Buscar actualizaciones" en ToolsScreen) con confirmación previa. Validamos parseando antes de persistir — un download corrupto no reemplaza la cache. Para añadir tools: editar `catalog.json` en raíz del repo, commit, push; los users avanzados le dan al botón y ya. Para que llegue bundleado al siguiente APK, el `assets/catalog.json` se regenera con `cp catalog.json app/src/main/assets/catalog.json` antes de cada release.
6. **No cachear el estado "instalado".** Siempre re-escanear cuando se entra a InstalledScreen.
7. **Foreground Service obligatorio para instalaciones.** Si no, el OS mata el proceso al lock screen.
8. **Sin tracking, analytics, crash reporting de terceros.** App offline-first; única operación de red opcional es la actualización del catálogo a petición explícita del usuario. Privacidad total: nada se envía a ningún sitio.
9. **Material 3 + dark forzado.** No `dynamicColor`, no light theme.
10. **minSdk 26 firme.** No usar APIs de SDK >34. No desugaring exótico.

## Notas operativas

- El catálogo contiene **92 herramientas** (no 95 como decía el header del blueprint — las tablas reales son la fuente de verdad). Vive en `catalog.json` (raíz del repo + `app/src/main/assets/`).
- El icono de launcher es un placeholder VectorDrawable. Para el icono final del blueprint hay que crear un asset PNG.
- `gradle-wrapper.jar` no está versionado. Tras clonar/abrir el proyecto la primera vez, ejecutar `gradle wrapper --gradle-version 8.7` o dejar que Android Studio lo regenere al hacer Sync.
- La fuente del terminal es `FontFamily.Monospace`. Para usar JetBrains Mono, dropear el `.ttf` en `res/font/` y cambiar el TODO en `ui/theme/Type.kt`.
