package com.example

import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MigrationTest {

    @Test
    fun migrate2To3() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dbFile = context.getDatabasePath("test_migration.db")
        dbFile.parentFile?.mkdirs()
        dbFile.delete()
        
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbFile.name)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS `saved_try_on_results` (`id` TEXT NOT NULL, `userPhoto` TEXT NOT NULL, `outfitImage` TEXT NOT NULL, `resultImage` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `isFavourite` INTEGER NOT NULL, `productId` TEXT NOT NULL, `productName` TEXT NOT NULL, `productBrand` TEXT NOT NULL, `productPrice` REAL NOT NULL, `cardHeight` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `try_on_photos` (`id` TEXT NOT NULL, `uri` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `tracked_products` (`id` TEXT NOT NULL, `productImage` TEXT NOT NULL, `productName` TEXT NOT NULL, `merchant` TEXT NOT NULL, `currentPrice` REAL NOT NULL, `targetPrice` REAL, `productUrl` TEXT NOT NULL, `trackedAt` TEXT NOT NULL, `isTrackingEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `favourite_products` (`productId` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`productId`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `shared_products` (`id` TEXT NOT NULL, `canonicalProductId` TEXT NOT NULL, `merchantName` TEXT, `originalSharedUrl` TEXT NOT NULL, `productName` TEXT, `productImage` TEXT, `price` REAL, `resolvedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
            
        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        var db = helper.writableDatabase
        
        db.execSQL("INSERT INTO saved_try_on_results (id, userPhoto, outfitImage, resultImage, createdAt, isFavourite, productId, productName, productBrand, productPrice, cardHeight) VALUES ('test_id', 'photo', 'outfit', 'result', 'now', 0, 'prod', 'name', 'brand', 0.0, 200)")
        db.close()
        
        // Now run migration
        val migration = AppDatabase.MIGRATION_2_3
        val configuration2 = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbFile.name)
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {}
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    if (oldVersion == 2 && newVersion == 3) {
                        migration.migrate(db)
                    }
                }
            })
            .build()
            
        val helper2 = FrameworkSQLiteOpenHelperFactory().create(configuration2)
        val db3 = helper2.writableDatabase
        
        val cursor = db3.query("SELECT * FROM saved_try_on_results")
        assert(cursor.moveToFirst())
        assert(cursor.getString(cursor.getColumnIndex("id")) == "test_id")
        cursor.close()
        
        val cursor2 = db3.query("SELECT name FROM sqlite_master WHERE type='table' AND name='credit_transactions'")
        assert(cursor2.moveToFirst())
        cursor2.close()
        
        db3.close()
    }
}
