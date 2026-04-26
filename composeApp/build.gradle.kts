import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose)
}

val tlauncherVersion = providers.gradleProperty("tlauncher.version").orElse("1.0.0")
val activityComposeVersion = libs.versions.androidx.activityCompose.get()
val composeMultiplatformVersion = libs.versions.compose.multiplatform.get()
val cauthVendorDir = rootProject.layout.projectDirectory.dir("vendor/cauth")
val cauthAndroidJarDir = cauthVendorDir.dir("android/libs")
val cauthAndroidJniLibsDir = cauthVendorDir.dir("android/jniLibs")
val cauthWindowsRuntimeDir = cauthVendorDir.dir("windows/x64")
val cauthWindowsResourceDir = layout.buildDirectory.dir("generated/cauth/windowsMain/resources")
val cauthAndroidJarNames = listOf(
    "cauth-android-core.jar",
    "cauth-android-steam-auth.jar",
    "cauth-android-steam-cloud.jar",
    "cauth-android-steam-depot.jar",
)
val cauthWindowsRuntimeNames = listOf(
    "cauth_core_ffi.dll",
    "cauth_steam_auth_ffi.dll",
    "cauth_steam_cloud_ffi.dll",
    "cauth_steam_depot_ffi.dll",
)

val syncCAuthWindowsRuntime by tasks.registering(Sync::class) {
    from(cauthWindowsRuntimeDir) {
        include(*cauthWindowsRuntimeNames.toTypedArray())
        into("cauth/windows/x64")
    }
    into(cauthWindowsResourceDir)
}

tasks.configureEach {
    if (name == "compressDebugAssets" || name == "packageDebug") {
        outputs.upToDateWhen { false }
    }
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}


kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("windows") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.components:components-resources:$composeMultiplatformVersion")
            implementation(libs.androidx.compose.runtime)
            implementation(libs.androidx.compose.runtime.saveable)
            implementation("org.jetbrains.compose.ui:ui:$composeMultiplatformVersion")
        }
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:$activityComposeVersion@aar")
            implementation("androidx.activity:activity:$activityComposeVersion@aar")
            implementation("androidx.fragment:fragment-ktx:1.8.9")
            implementation(libs.google.material)
            implementation(libs.okhttp)
            implementation(libs.kotlinx.coroutines.android)
            implementation(
                files(*cauthAndroidJarNames.map { jarName -> cauthAndroidJarDir.file(jarName) }.toTypedArray()),
            )
            implementation(project(":shared"))
        }
        val windowsMain by getting {
            resources.srcDir(cauthWindowsResourceDir)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.jna)
                implementation("org.json:json:20240303")
                implementation(project(":shared"))
            }
        }
        iosMain.dependencies {
            implementation(project(":shared"))
        }
    }
}

android {
    namespace = "com.dreamyloong.tlauncher"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.dreamyloong.tlauncher"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = tlauncherVersion.get()

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].assets.srcDir("src/androidMain/assets")
    sourceSets["main"].jniLibs.srcDir(cauthAndroidJniLibsDir)
}

tasks.matching { it.name == "windowsProcessResources" }.configureEach {
    dependsOn(syncCAuthWindowsRuntime)
}

compose.desktop {
    application {
        mainClass = "com.dreamyloong.tlauncher.windows.MainKt"
        jvmArgs += listOf("-Dtlauncher.version=${tlauncherVersion.get()}")

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi)
            packageName = "TLauncher"
            packageVersion = tlauncherVersion.get()

            windows {
                iconFile.set(project.file("src/windowsMain/resources/icons/tlauncher.ico"))
            }
        }
    }
}
