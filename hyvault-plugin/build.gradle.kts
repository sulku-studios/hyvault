import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin.Companion.shadowJar

plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.gradleup.shadow")
    kotlin("plugin.serialization")
}

description = "HyVault Core Plugin"

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":hyvault-api"))
    implementation("com.charleskorn.kaml:kaml:0.104.0")
    
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.9.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("com.charleskorn.kaml", "fi.sulku.hytale.libs.kaml")
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.shadowJar)
            groupId = project.group.toString()
            artifactId = "hyvault-plugin"
            version = project.version.toString()
        }
    }
}