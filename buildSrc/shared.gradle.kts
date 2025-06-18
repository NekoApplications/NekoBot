import java.io.ByteArrayOutputStream

tasks.register("generateProperties") {
    doLast {
        generateProperties()
    }
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
        m += "buildTime" to System.currentTimeMillis().toString()
        m += "branch" to getGitBranch()
        m += "commitId" to getCommitId()
        m += "coreVersion" to version.toString()
        m.toSortedMap().forEach {
            writer.println("${it.key} = ${it.value}")
        }
    }
}