package icu.takeneko.nekobot.command.minecraft

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.mcversion.MinecraftVersion
import icu.takeneko.nekobot.message.MessageResponseCreationScope
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

class VersionAgeCommand : Command() {
    override val commandPrefix: String
        get() = "howold"

    override val helpMessage: String
        get() = "howold <version>"

    override fun handle(commandMessage: CommandMessage): MessageResponseCreationScope {
        return commandMessage.createResponse {
            if (commandMessage.args.isEmpty()) {
                +helpMessage
                return@createResponse
            }
            val version = MinecraftVersion[commandMessage[0]!!] ?: run {
                +"Invalid version ${commandMessage[0]}"
                return@createResponse
            }
            val time = try {
                DateTimeFormatter.ISO_DATE_TIME.parse(version.releaseTime)
            } catch (e: DateTimeParseException) {
                +"Invalid release time: ${version.releaseTime}, ${e.message}"
                return@createResponse
            }
            val dateTime = LocalDateTime.from(time).toInstant(ZoneOffset.UTC).toEpochMilli()
            val age = System.currentTimeMillis() - dateTime
            val temporal = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(age), ZoneOffset.UTC)
            +"Minecraft ${version.id} is ${formatTime(temporal)} old."
            +"It was released on ${version.releaseTime}"
        }

    }

    fun formatTime(time: LocalDateTime): String {
        return buildString {
            val year = time.get(ChronoField.YEAR) - 1970
            val month = time.get(ChronoField.MONTH_OF_YEAR) - 1
            val day = time.get(ChronoField.DAY_OF_MONTH) - 1
            val hour = time.get(ChronoField.HOUR_OF_DAY)
            val minute = time.get(ChronoField.MINUTE_OF_HOUR)
            val second = time.get(ChronoField.SECOND_OF_MINUTE)
            val millisecond = time.get(ChronoField.MILLI_OF_SECOND)
            if (year != 0) {
                append("$year year${if (year == 1) "" else "s"}")
            }
            if (month != 0) {
                append(" $month month${if(month == 1) "" else "s"}")
            }
            if (day != 0) {
                append(" $day day${if(day == 1) "" else "s"}")
            }
            if (hour != 0) {
                append(" $hour hour${if(hour == 1) "" else "s"}")
            }
            if (minute != 0) {
                append(" $minute minute${if(minute == 1) "" else "s"}")
            }
            if (second != 0) {
                append(" $second second${if(second == 1) "" else "s"}")
            }
            if (millisecond != 0) {
                append(" $millisecond millisecond${if(millisecond == 1) "" else "s"}")
            }
        }
    }

}
