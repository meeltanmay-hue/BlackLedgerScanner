package com.blackledger.scanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "players",
    foreignKeys = [ForeignKey(
        entity = AllianceEntity::class,
        parentColumns = ["id"],
        childColumns = ["allianceId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val allianceId: Long,
    val playerName: String,
    val bannermanRank: String,
    val power: Long? = null,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)
