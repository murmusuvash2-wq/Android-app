package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import androidx.room.Room
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RealMigrationTest {
    @Test
    fun testRealMigration() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dbFile = context.getDatabasePath("tihin_database")
        dbFile.parentFile?.mkdirs()
        dbFile.delete()

        // Create version 2
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbFile.name)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS `saved_try_on_results` (`id` TEXT NOT NULL, `userPhoto` TEXT NOT NULL, `outfitImage` TEXT NOT NULL, `resultImage` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `isFavourite` INTEGER NOT NULL, `productId` TEXT NOT NULL, `productName` TEXT NOT NULL, `productBrand` TEXT NOT NULL, `productPrice` REAL NOT NULL, `cardHeight` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `try_on_photos` (`id` TEXT NOT NULL, `uri` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `tracked_products` (`id` TEXT NOT NULL, `productImage` TEXT NOT NULL, `productName` TEXT NOT NULL, `merchant` TEXT NOT NULL, `currentPrice` REAL NOT NULL, `targetPrice` REAL, `productUrl` TEXT NOT NULL, `trackedAt` TEXT NOT NULL, `isTrackingEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `favourite_products` (`productId` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`productId`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `shared_products` (`id` TEXT NOT NULL, `canonicalProductId` TEXT NOT NULL, `merchantName` TEXT, `originalSharedUrl` TEXT NOT NULL, `productName` TEXT, `productImage` TEXT, `price` REAL, `resolvedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
                    db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fake_hash')")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
            
        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        helper.writableDatabase.close()

        // Now open with Room to trigger validation
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "tihin_database")
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()
        db.query("SELECT * FROM saved_try_on_results", null).close()
    }
}
