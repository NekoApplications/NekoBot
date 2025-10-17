import java.io.ByteArrayOutputStream
import org.gradle.api.Project

tasks.register("generateProperties") {
    doLast {
        generateProperties()
    }
}

fun getGitBranch(): String {
    val pb = ProcessBuilder("git", "symbolic-ref", "--short", "-q", "HEAD")
        .redirectErrorStream(true)
    val proc = pb.start()
    val out = proc.inputStream.bufferedReader().use { it.readText() }.trim()
    val exit = proc.waitFor()
    if (exit != 0) return "" // or throw/handle as you prefer
    return out
}

fun getCommitId(): String {
    val pb = ProcessBuilder("git", "rev-parse", "HEAD")
        .redirectErrorStream(true)
    val proc = pb.start()
    val out = proc.inputStream.bufferedReader().use { it.readText() }.trim()
    val exit = proc.waitFor()
    if (exit != 0) throw RuntimeException("git rev-parse failed")
    return out
}

fun generateProperties() {
    val propertiesFile = file(layout.projectDirectory.file("./src/main/resources/build.properties"))
    if (propertiesFile.exists()) {
        propertiesFile.delete()
    }
    propertiesFile.createNewFile()
    val m = mutableMapOf<String, String>()
    propertiesFile.printWriter().use { writer ->
        m += "group" to group.toString()
        m += "supported_languages" to project.properties["supported_languages"].toString()
        m += "default_language" to project.properties["default_language"].toString()
        m += "impl_platform" to (project.properties["impl_platform"]?.toString() ?: "unknown")
        m += "buildTime" to System.currentTimeMillis().toString()
        m += "branch" to getGitBranch()
        m += "commitId" to getCommitId()
        m += "coreVersion" to version.toString()
        m.toSortedMap().forEach {
            writer.println("${it.key} = ${it.value}")
        }
    }
}