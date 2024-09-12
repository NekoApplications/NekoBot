import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly
import java.io.ByteArrayOutputStream

plugins {
    val kotlinVersion = "1.9.10"
    java
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    //id("com.github.gmazzo.buildconfig") version "3.1.0" //什么猪鼻插件
    //id("net.mamoe.mirai-console") version "2.16.0" //什么猪鼻插件
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "icu.takeneko"
version = "1.1.3"

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

//mirai {
//    noTestCore = true
//    setupConsoleTestRuntime {
//        classpath = classpath.filter {
//            !it.nameWithoutExtension.startsWith("mirai-core-jvm")
//        }
//    }
//}

dependencies {
    val overflowVersion = "2.16.0-695e4e1-SNAPSHOT"
    compileOnly("org.slf4j:slf4j-api:1.7.36")
    compileOnly("top.mrxiaom:overflow-core-api:$overflowVersion")
    //testConsoleRuntime("top.mrxiaom:overflow-core:$overflowVersion")
    compileOnly("net.mamoe:mirai-console:2.16.0")
    compileOnly("net.mamoe:mirai-core:2.16.0")

    implementation("ch.qos.logback:logback-core:1.2.11")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    implementation("com.google.code.gson:gson:2.9.0")
    implementation("it.unimi.dsi:fastutil-core:8.5.4")
    implementation("net.fabricmc:mapping-io:0.1.8")
    implementation("io.github.murzagalin:multiplatform-expressions-evaluator:0.15.0")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-json-jvm
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
}

tasks.withType<ShadowJar>{
    this.exclude("/kotlin*", "/net/mamoe/*", "/org/slf4j*", "/ch/qos*")
}

task("generateProperties") {
    doLast {
        generateProperties()
    }
}

tasks.getByName("processResources") {
    dependsOn("generateProperties")
}

fun getGitBranch(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "symbolic-ref", "--short", "-q", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString(Charsets.UTF_8).trim()
}

fun getCommitId(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString(Charsets.UTF_8).trim()
}

fun generateProperties() {
    val propertiesFile = file("./src/main/resources/build.properties")
    if (propertiesFile.exists()) {
        propertiesFile.delete()
    }
    propertiesFile.createNewFile()
    val m = mutableMapOf<String, String>()
    propertiesFile.printWriter().use { writer ->
        properties.forEach {
            val str = it.value.toString()
            if ("@" in str || "(" in str || ")" in str || "extension" in str || "null" == str || "\'" in str || "\\" in str || "/" in str) return@forEach
            if ("PROJECT" in str.toUpperCaseAsciiOnly() || "PROJECT" in it.key.toUpperCaseAsciiOnly() || " " in str) return@forEach
            if ("GRADLE" in it.key.toUpperCaseAsciiOnly() || "GRADLE" in str.toUpperCaseAsciiOnly() || "PROP" in it.key.toUpperCaseAsciiOnly()) return@forEach
            if ("." in it.key || "TEST" in it.key.toUpperCaseAsciiOnly()) return@forEach
            if (it.value.toString().length <= 2) return@forEach
            m += it.key to str
        }
        m += "buildTime" to System.currentTimeMillis().toString()
        m += "branch" to getGitBranch()
        m += "commitId" to getCommitId()
        m.toSortedMap().forEach {
            writer.println("${it.key} = ${it.value}")
        }
    }
}