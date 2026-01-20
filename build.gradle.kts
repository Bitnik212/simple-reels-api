plugins {
    kotlin("jvm") version "2.2.21"
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "moe.bitt.reels.api"
version = "0.0.1"

application {
    mainClass = "moe.bitt.ApplicationKt"
}

repositories {
    google()
    mavenCentral()
    maven("https://gitlab.com/api/v4/projects/69984153/packages/maven")
}

dependencies {
    implementation(project(":proxy-api"))
    implementation("io.ktor:ktor-server-default-headers")
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation("io.bitnik212:reels-downloader:0.1.0")
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)

    implementation("software.amazon.awssdk:s3:2.25.50")
    implementation("software.amazon.awssdk:netty-nio-client:2.25.50")

    // чтобы использовать SdkPublisher удобнее
    implementation("io.projectreactor:reactor-core:3.6.3")

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
