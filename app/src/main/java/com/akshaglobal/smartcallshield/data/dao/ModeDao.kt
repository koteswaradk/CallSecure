package com.akshaglobal.smartcallshield.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.akshaglobal.smartcallshield.data.model.ModeContactCrossRef
import com.akshaglobal.smartcallshield.data.model.ModeEntity
import com.akshaglobal.smartcallshield.data.model.ModeWithContacts
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMode(mode: ModeEntity): Long

    @Query("SELECT * FROM call_modes WHERE id = :id")
    fun getModeById(id: Long): Flow<ModeEntity?>

    @Query("SELECT * FROM call_modes ORDER BY name ASC")
    fun getAllModes(): Flow<List<ModeEntity>>

    @Query("SELECT * FROM call_modes WHERE isActive = 1 LIMIT 1")
    fun getActiveMode(): Flow<ModeEntity?>

    @Transaction
    @Query("SELECT * FROM call_modes WHERE id = :modeId")
    fun getModeWithContacts(modeId: Long): Flow<ModeWithContacts?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModeContactCrossRef(crossRef: ModeContactCrossRef)

    @Query("DELETE FROM mode_contact_cross_ref WHERE modeId = :modeId AND contactId = :contactId")
    suspend fun removeModeContactCrossRef(modeId: Long, contactId: Long)

    @Query("UPDATE call_modes SET isActive = CASE WHEN id = :modeId THEN 1 ELSE 0 END")
    suspend fun setActiveMode(modeId: Long)
}

