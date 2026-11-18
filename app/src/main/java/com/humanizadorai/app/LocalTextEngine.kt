package com.humanizadorai.app

import java.util.Locale

/**
 * Motor lingüístico local basado en reglas. No usa modelos de IA, red ni servicios externos.
 * Las transformaciones son conservadoras y deben revisarse por el usuario.
 */
object LocalTextEngine {
    private val replacements = listOf(
        Regex("\\bdebido a que\\b", RegexOption.IGNORE_CASE) to "porque",
        Regex("\\ba pesar del hecho de que\\b", RegexOption.IGNORE_CASE) to "aunque",
        Regex("\\bcon el objetivo de\\b", RegexOption.IGNORE_CASE) to "para",
        Regex("\\bcon la finalidad de\\b", RegexOption.IGNORE_CASE) to "para",
        Regex("\\ben la actualidad\\b", RegexOption.IGNORE_CASE) to "hoy en día",
        Regex("\\bde manera significativa\\b", RegexOption.IGNORE_CASE) to "notablemente",
        Regex("\\bllevar a cabo\\b", RegexOption.IGNORE_CASE) to "realizar",
        Regex("\\bpor otra parte\\b", RegexOption.IGNORE_CASE) to "además",
        Regex("\\ben conclusión\\b", RegexOption.IGNORE_CASE) to "en resumen",
        Regex("\\bdebido al hecho de que\\b", RegexOption.IGNORE_CASE) to "porque",
        Regex("\\buna gran cantidad de\\b", RegexOption.IGNORE_CASE) to "muchos",
        Regex("\\bcon respecto a\\b", RegexOption.IGNORE_CASE) to "sobre",
        Regex("\\bcon relación a\\b", RegexOption.IGNORE_CASE) to "sobre",
        Regex("\\bde forma adecuada\\b", RegexOption.IGNORE_CASE) to "correctamente",
        Regex("\\bpor medio de\\b", RegexOption.IGNORE_CASE) to "mediante"
    )

    fun rewrite(source: String, style: String, personalSample: String = ""): String {
        require(source.isNotBlank()) { "Escribe o importa un texto." }
        require(source.length <= 100_000) { "El texto supera el límite local de 100,000 caracteres." }

        val normalized = source.replace("\r\n", "\n").replace('\r', '\n')
        val result = normalized.lines().joinToString("\n") { paragraph ->
            var line = paragraph.trim().replace(Regex("[\t ]+"), " ")
            replacements.forEach { (pattern, replacement) ->
                line = pattern.replace(line) { match -> preserveCase(match.value, replacement) }
            }
            line = when (style) {
                "Sencillo y claro" -> simplifyConnectors(line)
                "Profesional" -> line.replace(Regex("\\bplaticar\\b", RegexOption.IGNORE_CASE), "conversar")
                "Académico" -> line.replace(Regex("\\bpero\\b", RegexOption.IGNORE_CASE), "sin embargo")
                else -> line
            }
            line.replace(Regex(" +([,.;:!?])"), "$1")
                .replace(Regex("([,;:])(?=\\S)"), "$1 ")
        }
        // La muestra se conserva en la interfaz, pero no se imita automáticamente
        // para evitar atribuir al usuario expresiones que no son suyas.
        return result.trim()
    }

    private fun simplifyConnectors(text: String): String =
        text.replace(Regex("\\bno obstante\\b", RegexOption.IGNORE_CASE), "pero")
            .replace(Regex("\\bpor consiguiente\\b", RegexOption.IGNORE_CASE), "por eso")
            .replace(Regex("\\basimismo\\b", RegexOption.IGNORE_CASE), "también")

    private fun preserveCase(original: String, replacement: String): String =
        if (original.firstOrNull()?.isUpperCase() == true)
            replacement.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "MX")) else it.toString() }
        else replacement
}
