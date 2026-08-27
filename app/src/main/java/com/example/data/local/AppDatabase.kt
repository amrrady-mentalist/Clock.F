package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [AlarmEntity::class, WorldCityEntity::class, SecretConfigEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clockDao(): ClockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clock_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.clockDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: ClockDao) {
                // Initial Default Alarms
                dao.insertAlarm(
                    AlarmEntity(
                        hour = 7,
                        minute = 0,
                        isPm = false,
                        label = "Wake up",
                        daysOfWeek = "1,2,3,4,5",
                        isEnabled = true
                    )
                )
                dao.insertAlarm(
                    AlarmEntity(
                        hour = 8,
                        minute = 30,
                        isPm = false,
                        label = "Work / Study",
                        daysOfWeek = "1,2,3,4,5",
                        isEnabled = false
                    )
                )

                // Initial World Clocks
                val defaultCities = listOf(
                    WorldCityEntity(cityName = "London", country = "United Kingdom", timeZoneId = "Europe/London"),
                    WorldCityEntity(cityName = "New York", country = "United States", timeZoneId = "America/New_York"),
                    WorldCityEntity(cityName = "Tokyo", country = "Japan", timeZoneId = "Asia/Tokyo"),
                    WorldCityEntity(cityName = "Paris", country = "France", timeZoneId = "Europe/Paris"),
                    WorldCityEntity(cityName = "Sydney", country = "Australia", timeZoneId = "Australia/Sydney")
                )
                dao.insertWorldCities(defaultCities)

                // Initial Secret Force Config
                dao.saveSecretConfig(
                    SecretConfigEntity(
                        id = 1,
                        isForceEnabled = true, // enabled for user to experience immediately or customize
                        forcedHour = 7,
                        forcedMinute = 42,
                        forcedIsPm = false,
                        forceMode = "MAGNETIC",
                        secretPin = "1234",
                        isPinRequired = false,
                        hapticFeedback = true,
                        secretTriggerGesture = "LONG_PRESS_HEADER"
                    )
                )
            }
        }
    }
}
