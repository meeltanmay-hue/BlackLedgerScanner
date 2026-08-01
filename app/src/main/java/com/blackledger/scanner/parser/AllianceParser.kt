package com.blackledger.scanner.parser

import kotlin.text.RegexOption

class AllianceParser {
    data class ParsedAlliance(
        val kingdomId: String,
        val allianceTag: String,
        val allianceName: String?,
        val members: List<Pair<String, String>>
    )

    fun parse(text: String): ParsedAlliance? {
        val kingdomRegex = Regex("""Kingdom\s*(\d+)""", RegexOption.IGNORE_CASE)
        val kingdomMatch = kingdomRegex.find(text)
        val kingdomId = kingdomMatch?.groupValues?.get(1) ?: return null

        val tagRegex = Regex("""\[?([A-Z]{2,6})\]?""")
        val tagMatch = tagRegex.find(text)
        val allianceTag = tagMatch?.groupValues?.get(1) ?: return null

        val nameRegex = Regex("""([A-Za-z\s]+)\s*\[?[A-Z]{2,6}\]?""")
        val nameMatch = nameRegex.find(text)
        val allianceName = nameMatch?.groupValues?.get(1)?.trim()

        val members = mutableListOf<Pair<String, String>>()
        val rankPattern = Regex("""Rank\s+([IVXLCDM]+)""", RegexOption.IGNORE_CASE)
        val lines = text.lines()
        var currentRank = ""
        for (line in lines) {
            val rankMatch = rankPattern.find(line)
            if (rankMatch != null) {
                currentRank = "Rank ${rankMatch.groupValues[1].uppercase()}"
                continue
            }
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("Kingdom") &&
                !trimmed.contains("Alliance") && !trimmed.contains("Member")) {
                if (currentRank.isNotEmpty()) {
                    members.add(trimmed to currentRank)
                }
            }
        }
        return ParsedAlliance(kingdomId, allianceTag, allianceName, members)
    }
}
