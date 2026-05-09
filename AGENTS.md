# WeKit — Agent Guide

## Build

```bash
./gradlew :app:assembleDebug   # debug (uses same signing as release)
./gradlew :app:assembleRelease # release (R8 proguard, shrinkResources)
```

- JDK 21 required, set via `gradle.properties` `org.gradle.jvmargs`
- Rust native lib auto-compiles during build (targets: `app/src/main/rust/wekit-native`). Requires:
  Rust toolchain + Android NDK targets + NDK. `configureCargo` task auto-generates `.cargo/config.toml`
  from NDK.
- AGP 9, Gradle version catalog in `gradle/libs.versions.toml`

## Project Structure

- `app/` — main Android module, entrypoints, hooks, UI, native Rust lib
- `libs/common/annotation-scanner/` — KSP annotation processor (`@HookItem` scanner)
- `libs/common/libxposed-api/` — compileOnly LibXposed API interface stubs (compileOnly since they are provided by user's Xposed framework)
- `libs/common/stubs/` — compileOnly stubs for WeChat and Android hidden classes
- `libs/external/comptime-kt/` — submodule: compile-time reflection utility
- `buildSrc/` — custom Gradle tasks: `GenerateMethodHashesTask` (`IResolvesDex` `resolveDex` method MD5 cache), `ConfigureCargoTask` (Rust NDK linker config)

## Entry Points & Architecture

- Xposed entry: `io.github.libxposed.api.XposedModule` (libxposed 101 & 100) or legacy Xposed API (51+). Entry classes in `loader/entry/lsp101/`, `loader/entry/lsp100/`, `loader/entry/common/`.
- Frida inject entry: `loader/entry/frida/FridaInjectEntry` + `frida-inject.js`
- Unified flow: `UnifiedEntryPoint.entry()` → `StartupAgent.startup()` → `WeLauncher.init()`
- Hook items annotated with `@HookItem(path, description)`, auto-discovered by KSP annotation scanner at compile time
- Base classes: `SwitchHookItem` (toggle on/off), `ClickableHookItem` (toggle on/off with click to configure), `ApiHookItem` (always-on), `BaseHookItem` (abstract base, do not use directly)
- DEX analysis via DexKit with `IResolvesDex` interface; method resolve body MD5-hashed for cache (
  `GenerateMethodHashesTask`)
- DEX-resolved targets DSL: `val methodTarget by dexMethod()` `val classTarget by dexClass()` delegate → `methodTarget.hookBefore { ... }`, `val method: Method = methodTarget.method`, `val clazz = classTarget.clazz`
- UI: Jetpack Compose + Material 3, dialogs written using `showComposeDialog` and `AlertDialogContent`
- Config: MMKV via `WePrefs`
- Logging: via `WeLogger`

## Key Conventions

- Package namespace: `dev.ujhhgtg.wekit`
- Min SDK 29, target SDK 37, compile SDK 37
- Target: WeChat `com.tencent.mm`, versions 8.0.65–8.0.71. Version info in `HostInfo`
- Process targeting via `TargetProcesses`: override `startup()` to check
  `TargetProcesses.isInMain` / `TargetProcesses.currentType`. Default: main process only.
- Multi-process config sync via MMKV (cross-process safe)
- No unit tests — manual testing on real WeChat only
- Do NOT unconditionally catch-all in `onEnable()` / `hookBefore()` / `hookAfter()` — implementation already handles and logs
- KavaRef for reflection — unlike Legacy Xposed API (`de.robv.android.xposed.*`), the library KavaRef does NOT auto-cache methods (except constructors used in KavaRef extension method `Class<T : Any>.createInstance()`), manually cache frequently-called `Method`s & `Constructor`s & `Field`s
- Use the library `KavaRef` for Java reflection if possible. Use `KavaRefUtils` as an entry point to it for clearer semantics. KavaRef
- If `JsApiExposer` (`hooks/items/scripting_js/JsApiExposer.kt`) is modified, keep `globals.d.ts` in
  the same directory in sync — it's the TypeScript type declaration for the JS scripting API

## CI

- GitHub Actions: builds on push/PR to `master` (skips non-code changes)
- Artifacts automatically published to "CI" release + Telegram channel
- Workflow caches: Gradle caches + Rust (`~/.cargo` + `target/`) + Gradle build cache
- Keystore: base64-decoded from `KEYSTORE_BASE64` repository secret
