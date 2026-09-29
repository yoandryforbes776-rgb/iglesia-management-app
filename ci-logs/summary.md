# Build 13 · 2026-09-29T21:34:41Z · success

## Firebase
Firebase: archivo aplicado (749 bytes)
- project_id: iglesia-flow
- package_name(s): com.iglesiaflow.gestion
- firebase_url (Realtime Database): https://iglesia-flow-default-rtdb.firebaseio.com

## Errores detectados

## Cola del log
==> ci-logs/debug.log <==
Downloading https://services.gradle.org/distributions/gradle-8.9-bin.zip
............10%.............20%.............30%.............40%.............50%.............60%.............70%.............80%.............90%.............100%
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.9/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :app:generateDebugBuildConfig
> Task :app:checkDebugAarMetadata
> Task :app:generateDebugResValues
> Task :app:processDebugGoogleServices
> Task :app:mapDebugSourceSetPaths
> Task :app:generateDebugResources
> Task :app:packageDebugResources
> Task :app:mergeDebugResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug
> Task :app:parseDebugLocalResources
> Task :app:processDebugMainManifest
> Task :app:processDebugManifest
> Task :app:processDebugManifestForPackage
> Task :app:javaPreCompileDebug
> Task :app:mergeDebugShaders
> Task :app:compileDebugShaders NO-SOURCE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets
> Task :app:compressDebugAssets
> Task :app:processDebugResources
> Task :app:l8DexDesugarLibDebug
> Task :app:desugarDebugFileDependencies
> Task :app:mergeDebugStartupProfile
> Task :app:mergeDebugJniLibFolders
> Task :app:checkDebugDuplicateClasses
> Task :app:mergeDebugNativeLibs
> Task :app:kspDebugKotlin
> Task :app:mergeExtDexDebug
> Task :app:mergeLibDexDebug

> Task :app:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libdatastore_shared_counter.so, libsqlcipher.so.

> Task :app:validateSigningDebug
> Task :app:writeDebugAppMetadata
> Task :app:writeDebugSigningConfigVersions

> Task :app:compileDebugKotlin
w: file:///home/runner/work/iglesia-management-app/iglesia-management-app/app/src/main/java/com/iglesiaflow/gestion/ui/components/FormComponents.kt:106:18 'fun Modifier.menuAnchor(): Modifier' is deprecated. Use overload that takes MenuAnchorType and enabled parameters.
w: file:///home/runner/work/iglesia-management-app/iglesia-management-app/app/src/main/java/com/iglesiaflow/gestion/ui/navigation/Destinations.kt:30:57 'val Icons.Filled.FactCheck: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Filled.FactCheck.
w: file:///home/runner/work/iglesia-management-app/iglesia-management-app/app/src/main/java/com/iglesiaflow/gestion/ui/screens/dashboard/DashboardScreen.kt:97:41 'val Icons.Filled.TrendingUp: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Filled.TrendingUp.

> Task :app:compileDebugJavaWithJavac
> Task :app:hiltAggregateDepsDebug
> Task :app:hiltJavaCompileDebug
> Task :app:processDebugJavaRes
> Task :app:transformDebugClassesWithAsm
> Task :app:mergeDebugJavaResource
> Task :app:dexBuilderDebug
> Task :app:mergeDebugGlobalSynthetics
> Task :app:mergeProjectDexDebug
> Task :app:packageDebug
> Task :app:createDebugApkListingFileRedirect
> Task :app:assembleDebug
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run_5-1790717373538.json

BUILD SUCCESSFUL in 2m 50s
44 actionable tasks: 44 executed

==> ci-logs/release.log <==
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.9/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:mergeReleaseJniLibFolders
> Task :app:mergeReleaseNativeLibs
> Task :app:mergeReleaseArtProfile

> Task :app:stripReleaseDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libdatastore_shared_counter.so, libsqlcipher.so.

> Task :app:extractReleaseNativeSymbolTables
> Task :app:mergeReleaseNativeDebugMetadata NO-SOURCE
> Task :app:expandReleaseL8ArtProfileWildcards
> Task :app:buildKotlinToolingMetadata
> Task :app:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :app:checkReleaseDuplicateClasses
> Task :app:generateReleaseBuildConfig
> Task :app:generateReleaseResValues FROM-CACHE
> Task :app:processReleaseGoogleServices FROM-CACHE
> Task :app:checkReleaseAarMetadata
> Task :app:mapReleaseSourceSetPaths
> Task :app:generateReleaseResources
> Task :app:packageReleaseResources
> Task :app:parseReleaseLocalResources FROM-CACHE
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease FROM-CACHE
> Task :app:processReleaseMainManifest
> Task :app:processReleaseManifest
> Task :app:javaPreCompileRelease FROM-CACHE
> Task :app:extractProguardFiles
> Task :app:processReleaseManifestForPackage
> Task :app:mergeReleaseStartupProfile
> Task :app:mergeReleaseShaders
> Task :app:compileReleaseShaders NO-SOURCE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
> Task :app:compressReleaseAssets
> Task :app:extractReleaseVersionControlInfo
> Task :app:processApplicationManifestReleaseForBundle
> Task :app:collectReleaseDependencies
> Task :app:mergeReleaseResources
> Task :app:sdkReleaseDependencyData
> Task :app:validateSigningRelease
> Task :app:writeReleaseAppMetadata
> Task :app:writeReleaseSigningConfigVersions
> Task :app:processReleaseResources
> Task :app:bundleReleaseResources
> Task :app:kspReleaseKotlin

> Task :app:compileReleaseKotlin
w: file:///home/runner/work/iglesia-management-app/iglesia-management-app/app/src/main/java/com/iglesiaflow/gestion/ui/components/FormComponents.kt:106:18 'fun Modifier.menuAnchor(): Modifier' is deprecated. Use overload that takes MenuAnchorType and enabled parameters.
w: file:///home/runner/work/iglesia-management-app/iglesia-management-app/app/src/main/java/com/iglesiaflow/gestion/ui/navigation/Destinations.kt:30:57 'val Icons.Filled.FactCheck: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Filled.FactCheck.
w: file:///home/runner/work/iglesia-management-app/iglesia-management-app/app/src/main/java/com/iglesiaflow/gestion/ui/screens/dashboard/DashboardScreen.kt:97:41 'val Icons.Filled.TrendingUp: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Filled.TrendingUp.

> Task :app:compileReleaseJavaWithJavac
> Task :app:hiltAggregateDepsRelease FROM-CACHE
> Task :app:hiltJavaCompileRelease
> Task :app:processReleaseJavaRes
> Task :app:transformReleaseClassesWithAsm
> Task :app:mergeReleaseJavaResource
> Task :app:mergeReleaseGeneratedProguardFiles
> Task :app:expandReleaseArtProfileWildcards
> Task :app:minifyReleaseWithR8

> Task :app:l8DexDesugarLibRelease
Info: Proguard configuration rule does not match anything: `-keepclassmembers class j$.util.concurrent.ConcurrentHashMap$TreeBin {
  int lockState;
}`
Info: Proguard configuration rule does not match anything: `-keepclassmembers class j$.util.concurrent.ConcurrentHashMap {
  int sizeCtl;
  int transferIndex;
  long baseCount;
  int cellsBusy;
}`
Info: Proguard configuration rule does not match anything: `-keepclassmembers class j$.util.concurrent.ConcurrentHashMap$CounterCell {
  long value;
}`
Info: Proguard configuration rule does not match anything: `-keepclassmembers enum * {
  public static **[] values();
  public static ** valueOf(java.lang.String);
  public static final synthetic <fields>;
}`
Info: Proguard configuration rule does not match anything: `-keepclassmembers class j$.util.IntSummaryStatistics {
  long count;
  long sum;
  int min;
  int max;
}`
Info: Proguard configuration rule does not match anything: `-keepclassmembers class j$.util.LongSummaryStatistics {
  long count;
  long sum;
  long min;
  long max;
}`
Info: Proguard configuration rule does not match anything: `-keepclassmembers class j$.util.DoubleSummaryStatistics {
  long count;
  double sum;
  double min;
  double max;
}`

> Task :app:compileReleaseArtProfile
> Task :app:shrinkReleaseRes
> Task :app:optimizeReleaseResources
> Task :app:packageRelease
> Task :app:createReleaseApkListingFileRedirect
> Task :app:assembleRelease
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run_6-1790717543419.json

BUILD SUCCESSFUL in 2m 12s
54 actionable tasks: 48 executed, 6 from cache
