package icu.takeneko.nekobot.crashreport

enum class TranslationStage(val formatter: (CrashReportTranslationJob) -> String){
        DETERMINE_SYMBOL({
            "Finding symbols: ${it.symbols.size}"
        }), TRANSLATING({
            "Translating symbols (${it.translatedSymbols.size}/${it.symbols.size})"
        }), UPLOADING({
            "Uploading translated crash report to mclo.gs"
        })
    }