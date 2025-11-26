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

        // Repositorio MSAL
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("https://pkgs.dev.azure.com/microsoft/android/msal/_packaging/msal-android/maven/v1") }

        // Repositorio Duo-SDK-Feed
        maven {
            name = "Duo-SDK-Feed"
            url = uri("https://pkgs.dev.azure.com/MicrosoftDeviceSDK/DuoSDK-Public/_packaging/Duo-SDK-Feed/maven/v1")
        }
    }
}

rootProject.name = "AppMovil"
include(":app")
