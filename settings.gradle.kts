pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "launcher-compose"

include(":composeApp")
include(":shared")
include(":extensionSdkApi")

project(":extensionSdkApi").projectDir = file("extensionSdkApi")
