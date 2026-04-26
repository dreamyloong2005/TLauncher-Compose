import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlinSerialization)
    `maven-publish`
}

group = "com.dreamyloong.tlauncher"
version = "1.0.0"

val activityComposeVersion = libs.versions.androidx.activityCompose.get()
val composeMultiplatformVersion = libs.versions.compose.multiplatform.get()

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
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:$activityComposeVersion@aar")
            implementation("androidx.activity:activity:$activityComposeVersion@aar")
        }
        commonMain.dependencies {
            api(project(":extensionSdkApi"))
            implementation("org.jetbrains.compose.foundation:foundation:$composeMultiplatformVersion")
            implementation(libs.compose.material3)
            implementation(libs.androidx.compose.runtime)
            implementation(libs.androidx.compose.runtime.saveable)
            implementation("org.jetbrains.compose.ui:ui:$composeMultiplatformVersion")
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
        }
        val windowsMain by getting {
            dependencies {
                implementation(libs.jna)
                implementation(libs.jna.platform)
            }
        }
    }
}

android {
    namespace = "com.dreamyloong.tlauncher.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
