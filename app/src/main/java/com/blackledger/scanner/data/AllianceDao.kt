package com.blackledger.scanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AllianceDao {
    @Insert
    suspend fun insertAlliance(alliance: AllianceEntity): Long

    @Insert
    suspend fun insertPlayer(player: PlayerEntity)

    @Query("SELECT * FROM alliances WHERE kingdomId = :kingdomId AND allianceTag = :allianceTag")
    suspend fun findAlliance(kingdomId: String, allianceTag: String): AllianceEntity?

    @Query("SELECT * FROM alliances")
    fun getAllAlliances(): Flow<List<AllianceEntity>>

    @Query("SELECT * FROM players WHERE allianceId = :allianceId ORDER BY bannermanRank")
    suspend fun getPlayersForAlliance(allianceId: Long): List<PlayerEntity>

    @Query("SELECT COUNT(*) FROM alliances")
    suspend fun getAllianceCount(): Int

    @Query("SELECT COUNT(*) FROM players")
    suspend fun getPlayerCount(): Int
}
