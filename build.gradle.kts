plugins {
    val kotlinVersion = "2.3.0"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
}

group = "icu.takeneko"
version = "1.4.0"
