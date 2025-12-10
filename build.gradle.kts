import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    //todo temp here?
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

}

allprojects {
    group = "fi.sulku.hytale"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    if (name != "hyvault-web") {
        apply(plugin = "maven-publish")
        apply(plugin = "org.jetbrains.kotlin.jvm")
        configure<KotlinJvmProjectExtension> {
            jvmToolchain(21)
        }

        afterEvaluate {
            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        from(components["java"])
                    }
                }
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        // add("testImplementation", kotlin("test"))
    }

}