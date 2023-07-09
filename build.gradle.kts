plugins {
	id("org.quiltmc.loom") version "1.2.+"
	id("io.github.p03w.machete") version "1.+"
	id("org.cadixdev.licenser") version "0.6.+"
}

apply(from = "https://raw.githubusercontent.com/JamCoreModding/Gronk/quilt/publishing.gradle.kts")
apply(from = "https://raw.githubusercontent.com/JamCoreModding/Gronk/quilt/misc.gradle.kts")

val mod_version: String by project

group = "io.github.jamalam360"
version = mod_version

repositories {
	val mavenUrls =
			mapOf(
					Pair("https://maven.terraformersmc.com/releases", listOf("com.terraformersmc")),
					Pair("https://api.modrinth.com/maven", listOf("maven.modrinth")),
			)

	for (mavenPair in mavenUrls) {
		maven {
			url = uri(mavenPair.key)
			content {
				for (group in mavenPair.value) {
					includeGroup(group)
				}
			}
		}
	}
}

dependencies {
	minecraft(libs.minecraft)
	mappings(variantOf(libs.quilt.mappings) { classifier("intermediary-v2") })

	modImplementation(libs.bundles.quilt)
	modLocalRuntime(libs.bundles.runtime)
}

sourceSets {
	val main = this.getByName("main")

	create("testmod") {
		this.compileClasspath += main.compileClasspath
		this.compileClasspath += main.output
		this.runtimeClasspath += main.runtimeClasspath
		this.runtimeClasspath += main.output
	}
}

loom {
	runtimeOnlyLog4j.set(true)

	runs {
		create("testClient") {
			client()
			name("Testmod Client")
			source(sourceSets.getByName("testmod"))
			runDir("run/test-client")
			property("quilt.recipe.dump", "true")
		}

		create("testServer") {
			server()
			name("Testmod Server")
			source(sourceSets.getByName("testmod"))
			runDir("run/test-server")
			property("quilt.recipe.dump", "true")
		}

		getByName("client") {
			runDir("run/client")
		}

		getByName("server") {
			runDir("run/server")
		}
	}
}
