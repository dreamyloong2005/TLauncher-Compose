plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinCompose) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

subprojects {
    configurations.configureEach {
        if (name.endsWith("ResolvableDependenciesMetadata") || name.endsWith("CompileKlibraries")) {
            exclude(group = "org.jetbrains.compose.annotation-internal", module = "annotation")
            exclude(group = "org.jetbrains.compose.collection-internal", module = "collection")
            exclude(group = "org.jetbrains.compose.runtime")
            exclude(group = "org.jetbrains.androidx.lifecycle")
            exclude(group = "org.jetbrains.androidx.savedstate")
        }
    }
}

tasks.register("packageExtensionSdkMaven") {
    group = "distribution"
    description = "Builds the distributable Maven repository zip for extension SDK developers."
    dependsOn(":extensionSdkApi:packageExtensionSdkMaven")
}

