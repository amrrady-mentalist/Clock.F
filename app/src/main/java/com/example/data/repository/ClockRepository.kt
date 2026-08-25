package com.example.data.repository

import com.example.data.local.AlarmEntity
import com.example.data.local.ClockDao
import com.example.data.local.SecretConfigEntity
import com.example.data.local.WorldCityEntity
import kotlinx.coroutines.flow.Flow

class ClockRepository(private val dao: ClockDao) {
    // Alarms
    val allAlarms: Flow<List<AlarmEntity>> = dao.getAllAlarms()

    suspend fun getAlarmById(id: Long): AlarmEntity? = dao.getAlarmById(id)
    suspend fun insertAlarm(alarm: AlarmEntity): Long = dao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: AlarmEntity) = dao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: AlarmEntity) = dao.deleteAlarm(alarm)
    suspend fun toggleAlarm(id: Long, isEnabled: Boolean) = dao.toggleAlarm(id, isEnabled)

    // World Cities
    val allWorldCities: Flow<List<WorldCityEntity>> = dao.getAllWorldCities()
    suspend fun insertWorldCity(city: WorldCityEntity): Long = dao.insertWorldCity(city)
    suspend fun deleteWorldCity(city: WorldCityEntity) = dao.deleteWorldCity(city)

    // Secret Force Config
    val secretConfig: Flow<SecretConfigEntity?> = dao.getSecretConfig()
    suspend fun getSecretConfigOnce(): SecretConfigEntity? = dao.getSecretConfigOnce()
    suspend fun saveSecretConfig(config: SecretConfigEntity) = dao.saveSecretConfig(config)
}
