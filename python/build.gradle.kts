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
    maven("https://server.cjsah.net:1002/maven")
}

tasks.shadowJar {
    archiveBaseName = "nekobot-python-embedded"
}

tasks.withType<JavaCompile> {
    this.sourceCompatibility = "21"
    this.targetCompatibility = "21"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":base"))
}

apply(from = rootProject.file("buildSrc/shared.gradle.kts"))

tasks.getByName("processResources") {
    dependsOn("generateProperties")
}