package com.blackledger.scanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alliances")
data class AllianceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kingdomId: String,
    val allianceTag: String,
    val allianceName: String?,
    val memberCount: Int,
    val capturedTimestamp: Long = System.currentTimeMillis()
)
