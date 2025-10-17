import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
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
    maven("https://maven.covers1624.net/")
}

//application {
//    mainClass = "icu.takeneko.nekobot.acidify.NekoBotAcidify"
//}

tasks.shadowJar {
    archiveBaseName = "nekobot-acidify"
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
    implementation("acidify:acidify-core")
    implementation("acidify:yogurt-qrcode")
}

apply(from = rootProject.file("buildSrc/shared.gradle.kts"))

tasks.getByName("processResources") {
    dependsOn("generateProperties")
}