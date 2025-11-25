plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.serialization)
}

description = "HyVault Core Plugin"

dependencies {
    implementation(project(":hyvault-api"))
    implementation(libs.kaml)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.jdk8)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        
        // Relocate dependencies to avoid conflicts
        relocate("com.charleskorn.kaml", "fi.sulku.hytale.libs.kaml")
        relocate("kotlinx.serialization", "fi.sulku.hytale.libs.kotlinx.serialization")
        relocate("okio", "fi.sulku.hytale.libs.okio")
        relocate("org.snakeyaml.engine", "fi.sulku.hytale.libs.snakeyaml")
        
        // Exclude unnecessary files
        exclude("META-INF/maven/**")
        exclude("META-INF/*.txt")
        exclude("META-INF/proguard/**")
        exclude("META-INF/com.android.tools/**")
        exclude("**/*.kotlin_builtins")
        
        // Minimize - only include used classes
        minimize()
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