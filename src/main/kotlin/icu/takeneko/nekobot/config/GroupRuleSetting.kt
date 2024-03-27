package icu.takeneko.nekobot.config

import icu.takeneko.nekobot.command.CommandManager

object GroupRuleSetting {

    fun getRuleOrCreate(group: String) =
        config.groupRules[group] ?: config.groupRules.apply { this[group] = mutableListOf();saveConfig() }[group]!!

    fun commandEnabledFor(group: String, command: String) = command in getRuleOrCreate(group)

    fun botEnabledFor(group: String) = group in config.allowedGroup

    fun enableBotForGroup(group: String): Unit = if (group !in config.allowedGroup) {
        config.allowedGroup += group
        saveConfig()
    } else Unit

    fun enableCommandForGroup(group: String, command: String) {
        val rule = getRuleOrCreate(group)
        if (command !in rule && command in CommandManager.commands) {
            rule += command
        }
        config.groupRules[group] = rule
        saveConfig()
    }

    fun disableCommandForGroup(group: String, command: String) {
        val rule = getRuleOrCreate(group)
        if (command in rule && command in CommandManager.commands) {
            rule -= command
        }
        config.groupRules[group] = rule
        saveConfig()
    }

    fun disableBotForGroup(group: String) = if (group in config.allowedGroup) {
        config.allowedGroup -= group
        saveConfig()
    } else Unit
}