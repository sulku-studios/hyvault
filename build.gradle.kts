plugins {
    val kotlinVer = "2.3.0-RC"
    kotlin("jvm") version kotlinVer apply false
    id("com.gradleup.shadow") version "9.2.2" apply false
    kotlin("plugin.serialization") version kotlinVer apply false
}

allprojects {
    group = "fi.sulku.hytale"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}