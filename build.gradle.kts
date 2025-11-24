plugins {
    kotlin("jvm") version "2.2.21" apply false
    id("com.gradleup.shadow") version "9.0.0-beta15" apply false
    kotlin("plugin.serialization") version "2.2.21" apply false
}

allprojects {
    group = "fi.sulku.hytale"
    version = "1.10-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}