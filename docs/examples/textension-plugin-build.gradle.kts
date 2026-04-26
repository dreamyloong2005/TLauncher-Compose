import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20"
}

group = "com.example.tlauncher"
version = "1.0.0"

val extensionId = "plugin.example.hello.windows"
val extensionDisplayName = "Example Plugin"
val extensionApiVersion = "1.0.0"
val minSdkApiVersion = 1
val targetSdkApiVersion = 1
val windowsRuntimeArtifact = "runtime/windows/$extensionId.jar"

repositories {
    mavenCentral()
}

kotlin {
    sourceSets.named("main") {
        kotlin.srcDir("src/commonMain/kotlin")
        kotlin.srcDir("src/windowsMain/kotlin")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly("com.dreamyloong.tlauncher:tlauncher-extension-api:$extensionApiVersion")
}

val runtimeJar = tasks.named<Jar>("jar") {
    archiveBaseName.set(extensionId)
    archiveVersion.set(version.toString())
}

val generatedManifest = layout.buildDirectory.file("generated/textension/manifest.json")
val packageRoot = layout.buildDirectory.dir("textension/package")

val generateTExtensionManifest = tasks.register("generateTExtensionManifest") {
    outputs.file(generatedManifest)

    doLast {
        val manifestFile = generatedManifest.get().asFile
        manifestFile.parentFile.mkdirs()
        manifestFile.writeText(
            """
            {
              "id": "$extensionId",
              "kind": "PLUGIN",
              "displayName": "$extensionDisplayName",
              "version": "$version",
              "apiVersion": "$extensionApiVersion",
              "supportedTargets": ["WINDOWS"],
              "capabilities": [
                "PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS"
              ],
              "compatibility": {
                "packageFormatVersion": 1,
                "minSdkApiVersion": $minSdkApiVersion,
                "targetSdkApiVersion": $targetSdkApiVersion
              },
              "entrypoints": {
                "WINDOWS": "com.example.tlauncher.ExamplePluginEntrypoint"
              },
              "runtimeArtifacts": {
                "WINDOWS": "$windowsRuntimeArtifact"
              }
            }
            """.trimIndent(),
        )
    }
}

val stageTExtensionPackage = tasks.register<Sync>("stageTExtensionPackage") {
    dependsOn(generateTExtensionManifest)
    dependsOn(runtimeJar)

    includeEmptyDirs = false
    into(packageRoot)
    from(generatedManifest)
    from(runtimeJar.flatMap { it.archiveFile }) {
        into(windowsRuntimeArtifact.substringBeforeLast('/'))
        rename { windowsRuntimeArtifact.substringAfterLast('/') }
    }
    from("src/windowsMain/assets") {
        into("resources")
    }
}

tasks.register<Zip>("packageTExtension") {
    group = "TExtension"
    description = "Packages the Windows example extension as a TExtension file."

    dependsOn(stageTExtensionPackage)
    archiveFileName.set("$extensionId-$version" + "TExtension")
    destinationDirectory.set(layout.buildDirectory.dir("dist"))
    includeEmptyDirs = false
    from(packageRoot)
}
