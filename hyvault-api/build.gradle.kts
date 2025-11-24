plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

description = "HyVault Economy API"

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.9.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "hyvault-api"
            version = project.version.toString()
        }
    }
}