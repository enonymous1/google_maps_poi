package com.enonymous1.googlemapspoi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {
    @Query("SELECT * FROM pois ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Poi>>

    @Query("SELECT * FROM pois WHERE id = :id")
    suspend fun getById(id: Long): Poi?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(poi: Poi): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(pois: List<Poi>)

    @Delete
    suspend fun delete(poi: Poi)
}
