package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClockDao {
    // Alarms
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleAlarm(id: Long, isEnabled: Boolean)

    // World Cities
    @Query("SELECT * FROM world_cities ORDER BY cityName ASC")
    fun getAllWorldCities(): Flow<List<WorldCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorldCity(city: WorldCityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorldCities(cities: List<WorldCityEntity>)

    @Delete
    suspend fun deleteWorldCity(city: WorldCityEntity)

    // Secret Force Config
    @Query("SELECT * FROM secret_config WHERE id = 1")
    fun getSecretConfig(): Flow<SecretConfigEntity?>

    @Query("SELECT * FROM secret_config WHERE id = 1")
    suspend fun getSecretConfigOnce(): SecretConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSecretConfig(config: SecretConfigEntity)
}
