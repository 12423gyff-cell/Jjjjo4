package com.example.model

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.prayer.PrayerTimingEntity
import com.example.prayer.PrayerTimingDao

@Dao
interface ZikrDao {
    @Query("SELECT * FROM zikr_progress")
    fun getAllProgress(): Flow<List<ZikrProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ZikrProgress)

    @Query("DELETE FROM zikr_progress")
    suspend fun clearAllProgress()

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteZikr>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteZikr)

    @Delete
    suspend fun removeFavorite(favorite: FavoriteZikr)
}

@Database(entities = [ZikrProgress::class, FavoriteZikr::class, PrayerTimingEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun zikrDao(): ZikrDao
    abstract fun prayerTimingDao(): PrayerTimingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hisn_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
