pluginManagement {
    repositories {
        val mavenUrls = listOf(
                "https://maven.quiltmc.org/repository/release",
                "https://maven.fabricmc.net/"
        )

        for (url in mavenUrls) {
            maven(url = url)
        }

        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("versions.toml"))
        }
    }
}
