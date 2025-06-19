import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.gradleup.shadow") version "9.0.0-beta17"
}

group = "icu.takeneko"
version = "1.3.1"

repositories {
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    mavenCentral()
    gradlePluginPortal()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.fabricmc.net")
    maven("https://server.cjsah.net:1002/maven")
}

application {
    mainClass = "icu.takeneko.nekobot.HeyBoxBotKt"
}

tasks.shadowJar {
    archiveBaseName = "nekobot-heybox"
    eachFile {
        if (this.file.name == "logback.xml") {
            val text = this.file.readText()
            if (text.contains("net.cjsah"))
            this.exclude()
        }
    }
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
    implementation("net.cjsah.bot:HeyBoxBotConsole:1.1.3")
}

apply(from = rootProject.file("buildSrc/shared.gradle.kts"))

tasks.getByName("processResources") {
    dependsOn("generateProperties")
}