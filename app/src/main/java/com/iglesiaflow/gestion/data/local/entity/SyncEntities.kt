package com.iglesiaflow.gestion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Marca temporal de la última descarga aplicada por colección. */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val collection: String,
    val lastPulledAt: Long = 0
)

/**
 * Lápida de un registro borrado localmente: permite propagar el borrado al
 * resto de dispositivos, ya que en la nube el documento sigue existiendo.
 */
@Entity(
    tableName = "sync_tombstones",
    indices = [Index(value = ["collection", "remoteId"], unique = true)]
)
data class TombstoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val collection: String,
    val remoteId: String,
    val deletedAt: Long = System.currentTimeMillis(),
    val pendingSync: Boolean = true
)
