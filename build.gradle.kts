import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly
import java.io.ByteArrayOutputStream

plugins {
    val kotlinVersion = "1.9.10"
    java
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.github.gmazzo.buildconfig") version "3.1.0"
    id("net.mamoe.mirai-console") version "2.16.0"
}

group = "icu.takeneko"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.fabricmc.net")
}

java.sourceCompatibility = JavaVersion.VERSION_17

kotlin {
    jvmToolchain(17)
}

buildConfig {
    className("BuildConstants")
    packageName("icu.takeneko.nekobot")
    useKotlinOutput()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
}

mirai {
    noTestCore = true
    setupConsoleTestRuntime {
        classpath = classpath.filter {
            !it.nameWithoutExtension.startsWith("mirai-core-jvm")
        }
    }
}

dependencies {
    val overflowVersion = "2.16.0-695e4e1-SNAPSHOT"
    compileOnly("top.mrxiaom:overflow-core-api:$overflowVersion")
    testConsoleRuntime("top.mrxiaom:overflow-core:$overflowVersion")
    implementation("it.unimi.dsi:fastutil-core:8.5.4")
    implementation("net.fabricmc:mapping-io:0.1.8")
    implementation("io.github.murzagalin:multiplatform-expressions-evaluator:0.15.0")
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