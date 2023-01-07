rootProject.name = "Media player"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://jcenter.bintray.com")
    }
}

include(":app_phone")
include(":app_tv")
include(":lib_communications")
include(":lib_vfs")
