import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Zip
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    `maven-publish`
}

group = "com.dreamyloong.tlauncher"
version = "1.0.0"

base {
    archivesName.set("tlauncher-extension-api")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        mavenPublication {
            artifactId = "tlauncher-extension-api-android"
        }
    }

    jvm("windows") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        mavenPublication {
            artifactId = "tlauncher-extension-api-windows"
        }
    }

    iosX64 {
        mavenPublication {
            artifactId = "tlauncher-extension-api-iosx64"
        }
    }
    iosArm64 {
        mavenPublication {
            artifactId = "tlauncher-extension-api-iosarm64"
        }
    }
    iosSimulatorArm64 {
        mavenPublication {
            artifactId = "tlauncher-extension-api-iossimulatorarm64"
        }
    }
}

android {
    namespace = "com.dreamyloong.tlauncher.extension.sdk.api"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

val sdkMavenRepositoryDir = layout.buildDirectory.dir("sdk-maven-repository")
val sdkMavenArchiveBaseName = "tlauncher-extension-sdk-${project.version}-maven"

publishing {
    repositories {
        maven {
            name = "sdkMaven"
            url = uri(sdkMavenRepositoryDir)
        }
    }

    publications.withType<MavenPublication>().configureEach {
        artifactId = when (name) {
            "kotlinMultiplatform" -> "tlauncher-extension-api"
            "androidRelease" -> "tlauncher-extension-api-android"
            "windows" -> "tlauncher-extension-api-windows"
            "iosX64" -> "tlauncher-extension-api-iosx64"
            "iosArm64" -> "tlauncher-extension-api-iosarm64"
            "iosSimulatorArm64" -> "tlauncher-extension-api-iossimulatorarm64"
            else -> artifactId
        }
    }
}

val cleanSdkMavenRepository = tasks.register<Delete>("cleanSdkMavenRepository") {
    delete(sdkMavenRepositoryDir)
}

tasks.matching { task ->
    task.name.endsWith("PublicationToSdkMavenRepository")
}.configureEach {
    dependsOn(cleanSdkMavenRepository)
}

tasks.register<Zip>("packageExtensionSdkMaven") {
    group = "distribution"
    description = "Publishes the extension SDK to a staging Maven repository and zips it for release."
    dependsOn("publishAllPublicationsToSdkMavenRepository")

    archiveFileName.set("$sdkMavenArchiveBaseName.zip")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("dist"))
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    into(sdkMavenArchiveBaseName) {
        from(sdkMavenRepositoryDir)
    }
}
