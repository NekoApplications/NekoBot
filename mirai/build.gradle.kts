import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.gradleup.shadow") version "9.0.0-beta17"
}

group = "icu.takeneko"
version = "1.4.0"

repositories {
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    mavenCentral()
    gradlePluginPortal()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.fabricmc.net")
}

tasks.shadowJar {
    archiveBaseName = "nekobot-mirai"
    archiveClassifier = "shadow"
    exclude("/kotlin/**", "/kotlinx/**", "/net/mamoe/*", "/org/slf4j*", "/ch/qos*")
}

tasks.withType<JavaCompile> {
    this.sourceCompatibility = "17"
    this.targetCompatibility = "17"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly("top.mrxiaom.mirai:overflow-core-api:1.0.6")
    compileOnly("net.mamoe:mirai-console:2.16.0")
    compileOnly("net.mamoe:mirai-core:2.16.0")

    implementation(project(":base"))
}

apply(from = rootProject.file("buildSrc/shared.gradle.kts"))

tasks.getByName("processResources") {
    dependsOn("generateProperties")
}