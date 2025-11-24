plugins {
    kotlin("jvm") version "2.2.20"
    id("maven-publish")
    id("com.github.ben-manes.versions") version "0.53.0" // Checks for dependency updates

}

group = "fi.sulku.hytale"
version = "1.10-SNAPSHOT"
description = "Economy Api"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation("org.jetbrains:annotations:26.0.2-1")
    //todo bstats like plugin
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}

// Configure the publishing settings
publishing {
    publications {
        // Create a Maven publication named "maven" (you can choose the name)
        create<MavenPublication>("maven") {
            // Use the group and version defined above
            groupId = project.group as String
            // Set the artifactId. Defaults to project name if not set.
            artifactId = project.name // Or use the 'artifactId' variable if you defined one
            version = project.version as String

            // Include the main artifact (the compiled JAR)
            from(components["java"])

            // Include the sources JAR artifact (optional but recommended)

            // If you set up KDoc/Javadoc generation (e.g., using Dokka),
            // you would add its JAR artifact here too.
            // Example: artifact(tasks.named("dokkaHtmlJar"))
        }
    }
    repositories {
        // Define the repository to publish to - mavenLocal() points to ~/.m2/repository
        mavenLocal()
    }
}


