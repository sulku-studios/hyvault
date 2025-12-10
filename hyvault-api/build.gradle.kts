description = "HyVault Economy API"
plugins {
    alias(libs.plugins.kotlin.jvm)
}
dependencies {
    compileOnly(libs.coroutines.core)
    compileOnly(libs.coroutines.jdk8)
    implementation(libs.kotlin.logging.jvm)
}