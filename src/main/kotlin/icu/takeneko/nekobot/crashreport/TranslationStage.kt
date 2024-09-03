package icu.takeneko.nekobot.crashreport

enum class TranslationStage(val formatter: (CrashReportTranslationJob) -> String) {
    DETERMINE_SYMBOL({
        "Finding symbols: ${it.classSymbols.size + it.methodSymbols.size}"
    }),
    TRANSLATING({
        "Translating symbols (${it.translatedClassSymbols.size + it.translatedMethodSymbols.size}/${it.classSymbols.size + it.methodSymbols.size})"
    }),
    UPLOADING({
        "Uploading translated crash report to mclo.gs"
    }),
    DONE({
        "Finished: https://mclo.gs/${it.mcLogsId}"
    }),
    FAILED({
        "Failed"
    })
}