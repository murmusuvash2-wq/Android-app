package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SavedTryOnResultEntity::class,
        TryOnPhotoEntity::class,
        TrackedProductEntity::class,
        FavouriteProductEntity::class,
        SharedProductEntity::class,
        CreditTransactionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedTryOnResultDao(): SavedTryOnResultDao
    abstract fun tryOnPhotoDao(): TryOnPhotoDao
    abstract fun trackedProductDao(): TrackedProductDao
    abstract fun favouriteProductDao(): FavouriteProductDao
    abstract fun sharedProductDao(): SharedProductDao
    abstract fun creditTransactionDao(): CreditTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

                val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `credit_transactions` (" +
                    "`operationId` TEXT NOT NULL, " +
                    "`amount` INTEGER NOT NULL, " +
                    "`state` TEXT NOT NULL, " +
                    "`source` TEXT NOT NULL, " +
                    "`timestamp` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`operationId`))"
                )
                
                // Add missing columns
                database.execSQL("ALTER TABLE `saved_try_on_results` ADD COLUMN `orderIndex` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `saved_try_on_results` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'usr_maya_01'")
                
                database.execSQL("ALTER TABLE `try_on_photos` ADD COLUMN `orderIndex` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `try_on_photos` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'usr_maya_01'")
                database.execSQL("ALTER TABLE `try_on_photos` ADD COLUMN `addedAt` INTEGER NOT NULL DEFAULT 0")
                
                database.execSQL("ALTER TABLE `tracked_products` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'usr_maya_01'")
                database.execSQL("ALTER TABLE `tracked_products` ADD COLUMN `orderIndex` INTEGER NOT NULL DEFAULT 0")
                
                database.execSQL("ALTER TABLE `shared_products` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'usr_maya_01'")
                
                database.execSQL("CREATE TABLE IF NOT EXISTS `favourite_products_new` (`productId` TEXT NOT NULL, `userId` TEXT NOT NULL DEFAULT 'usr_maya_01', `addedAt` INTEGER NOT NULL, PRIMARY KEY(`productId`, `userId`))")
                database.execSQL("INSERT INTO `favourite_products_new` (`productId`, `addedAt`) SELECT `productId`, `addedAt` FROM `favourite_products`")
                database.execSQL("DROP TABLE `favourite_products`")
                database.execSQL("ALTER TABLE `favourite_products_new` RENAME TO `favourite_products`")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tihin_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                .also { INSTANCE = it }
            }
        }

        fun resetForTesting(inMemoryDb: AppDatabase? = null) {
            INSTANCE = inMemoryDb
        }
    }
}
