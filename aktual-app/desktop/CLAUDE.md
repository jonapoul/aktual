# aktual-app:desktop

## Release builds

Release tasks (`proguardReleaseJars`, `runRelease`, `packageRelease*`) run ProGuard through the Compose plugin. Its ProGuard task doesn't support the configuration cache, and the cache can't be turned off while Isolated Projects is on, so disable both:

```bash
./gradlew :aktual-app:desktop:proguardReleaseJars --no-configuration-cache -Dorg.gradle.unsafe.isolated-projects=false
./gradlew :aktual-app:desktop:packageReleaseDistributionForCurrentOS --no-configuration-cache -Dorg.gradle.unsafe.isolated-projects=false
```

Without both flags the build fails with either "Invocation of 'Task.project' ... is unsupported with the configuration cache" or "Configuration Cache cannot be disabled when Isolated Projects is enabled". Expect it to take a few minutes.

- Rules: `proguard-rules.pro` plus the shared `../common-rules.pro`
- Output jars: `build/compose/tmp/main-release/proguard/`
- Mapping: `build/outputs/mapping.txt`
- Non-release tasks (`run`, `packageDistributionForCurrentOS`) skip ProGuard and need no extra flags
