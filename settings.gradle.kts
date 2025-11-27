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
        jcenter()
        maven { url = uri("https://repo1.maven.org/maven2/") } // Maven Central
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") } // releases de OSS
        maven { url = uri("https://dl.bintray.com/linkedin/maven") } // <-- Litr de LinkedIn
        maven { url = uri("https://jitpack.io") } // para cualquier otro artifact de JitPack
    }
}

rootProject.name = "PAD-2025-26-PicShield"
include(":app")
