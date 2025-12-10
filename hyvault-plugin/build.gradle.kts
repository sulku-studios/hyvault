evaluationDependsOn(":hyvault-web")

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.serialization)
}

description = "HyVault Core Plugin"

repositories {
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib")) // Version from plugin
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.jdk8)
    implementation(libs.kotlinx.serialization.json) // cross sync msg
    implementation(libs.lettuce) // cross sync msg
    //
    implementation(project(":hyvault-api"))
    implementation(libs.kaml)

    compileOnly("org.spigotmc:spigot-api:1.21.5-R0.1-SNAPSHOT")
}

val webDist = project(":hyvault-web").tasks.named("wasmJsBrowserDistribution")

tasks.processResources {
    from(webDist) {
        into("web")
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        // Relocate dependencies to avoid conflicts
        relocate("com.charleskorn.kaml", "fi.sulku.hytale.libs.kaml")
        relocate("io.lettuce", "fi.sulku.hytale.libs.lettuce")
       // relocate("kotlinx.serialization", "fi.sulku.hytale.libs.kotlinx.serialization")
        //relocate("okio", "fi.sulku.hytale.libs.okio")
        //relocate("org.snakeyaml.engine", "fi.sulku.hytale.libs.snakeyaml")

        // Exclude unnecessary files
        exclude("META-INF/maven/**")
        exclude("META-INF/*.txt")
        exclude("META-INF/proguard/**")
        exclude("META-INF/com.android.tools/**")
        exclude("**/*.kotlin_builtins")

        // Minimize - only include used classes
       /* minimize {
            exclude(project(":hyvault-api"))
        }*/
    }

    build {
        dependsOn(shadowJar)
    }
}

afterEvaluate {
    publishing {
        publications.named<MavenPublication>("maven") {
            artifacts.clear()
            artifact(tasks.shadowJar)
        }
    }
}