package icu.takeneko.nekobot.command.minecraft

import icu.takeneko.nekobot.mapping.MappingData
import icu.takeneko.nekobot.mapping.MappingRepository
import icu.takeneko.nekobot.mcversion.McVersionRepo
import java.nio.file.Path

private var prepared = false
val mappingRepository = MappingRepository(Path.of("./data"))
val versionRepository = McVersionRepo()
val namespaces = mutableListOf("official","intermediary","yarn","mojmap","srg","mcp")
fun mappingPrepare(){
    if (!prepared)return

    prepared = true
}

fun getMappingData(repo: MappingRepository, mcVersion: String): MappingData? {
    return repo.getMappingData(mcVersion)
}
