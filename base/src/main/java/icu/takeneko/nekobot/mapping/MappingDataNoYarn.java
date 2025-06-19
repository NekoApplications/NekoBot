package icu.takeneko.nekobot.mapping;

import net.fabricmc.mappingio.tree.MappingTree;

public class MappingDataNoYarn extends MappingData {
    public MappingDataNoYarn(String mcVersion, String mcpVersion, MappingTree mappingTree) {
        super(mcVersion, "", "", mcpVersion, mappingTree, false);
    }
}
