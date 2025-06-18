import java.io.ByteArrayOutputStream

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "icu.takeneko"
version = "1.2.1"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.fabricmc.net")
}

tasks.withType<JavaCompile>{
    this.sourceCompatibility = "17"
    this.targetCompatibility = "17"
}

kotlin {
    jvmToolchain(17)
}

val ktor_version: String by project

dependencies {
    api("ch.qos.logback:logback-core:1.2.11")
    api("org.slf4j:slf4j-api:1.7.36")
    api("ch.qos.logback:logback-classic:1.2.11")

    api("com.google.code.gson:gson:2.9.0")
    api("it.unimi.dsi:fastutil-core:8.5.4")
    api("net.fabricmc:mapping-io:0.1.8")
    api("io.github.murzagalin:multiplatform-expressions-evaluator:0.15.0")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-json-jvm
    api("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    api("io.ktor:ktor-client-core:$ktor_version")
    api("io.ktor:ktor-client-cio:$ktor_version")
    api("io.ktor:ktor-client-content-negotiation:$ktor_version")
    api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
}

