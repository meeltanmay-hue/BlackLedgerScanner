package com.blackledger.scanner.repository

import com.blackledger.scanner.data.AllianceDao
import com.blackledger.scanner.data.AllianceEntity
import com.blackledger.scanner.data.PlayerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class DataRepository(private val allianceDao: AllianceDao) {
    suspend fun saveAllianceWithPlayers(
        kingdomId: String,
        allianceTag: String,
        allianceName: String?,
        members: List<Pair<String, String>>
    ): Boolean {
        val existing = allianceDao.findAlliance(kingdomId, allianceTag)
        if (existing != null) return false

        val alliance = AllianceEntity(
            kingdomId = kingdomId,
            allianceTag = allianceTag,
            allianceName = allianceName,
            memberCount = members.size
        )
        val allianceId = allianceDao.insertAlliance(alliance)

        members.forEach { (name, rank) ->
            val player = PlayerEntity(
                allianceId = allianceId,
                playerName = name,
                bannermanRank = rank
            )
            allianceDao.insertPlayer(player)
        }
        return true
    }

    fun getAllAlliances(): Flow<List<AllianceEntity>> = allianceDao.getAllAlliances()

    suspend fun getAllianceCount(): Int = allianceDao.getAllianceCount()
    suspend fun getPlayerCount(): Int = allianceDao.getPlayerCount()
    suspend fun getPlayersForAlliance(allianceId: Long): List<PlayerEntity> =
        allianceDao.getPlayersForAlliance(allianceId)

    suspend fun searchAlliances(query: String): List<AllianceEntity> {
        return allianceDao.getAllAlliances().firstOrNull()?.filter {
            it.allianceTag.contains(query, ignoreCase = true) ||
            it.kingdomId.contains(query, ignoreCase = true) ||
            (it.allianceName?.contains(query, ignoreCase = true) ?: false)
        } ?: emptyList()
    }
}
