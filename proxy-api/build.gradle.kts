plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

group = "net.proxyline"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.bundles.ktor.client)
    testImplementation(kotlin("test"))
    // SLF4J binding for tests to avoid NOP logger warning
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.16")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}