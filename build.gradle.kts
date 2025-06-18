plugins {
    val kotlinVersion = "2.1.0"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
}

group = "icu.takeneko"
version = "1.2.1"
