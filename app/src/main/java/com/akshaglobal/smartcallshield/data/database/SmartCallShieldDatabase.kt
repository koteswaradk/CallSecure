package com.akshaglobal.smartcallshield.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.akshaglobal.smartcallshield.data.dao.CallLogDao
import com.akshaglobal.smartcallshield.data.dao.ContactDao
import com.akshaglobal.smartcallshield.data.dao.DrivingModeLogDao
import com.akshaglobal.smartcallshield.data.dao.SpamReportDao
import com.akshaglobal.smartcallshield.data.dao.ModeDao
import com.akshaglobal.smartcallshield.data.model.AppSettingsEntity
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.model.DrivingModeLogEntity
import com.akshaglobal.smartcallshield.data.model.SpamReportEntity
import com.akshaglobal.smartcallshield.data.model.ModeEntity
import com.akshaglobal.smartcallshield.data.model.ModeContactCrossRef

// Migration from version 1 to version 2
// Adds new tables for call modes feature: call_modes and mode_contact_cross_ref
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create call_modes table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `call_modes` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Create mode_contact_cross_ref table with foreign keys
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `mode_contact_cross_ref` (
                `modeId` INTEGER NOT NULL,
                `contactId` INTEGER NOT NULL,
                PRIMARY KEY(`modeId`, `contactId`),
                FOREIGN KEY(`modeId`) REFERENCES `call_modes`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // Create indices for better query performance
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_mode_contact_cross_ref_modeId` ON `mode_contact_cross_ref` (`modeId`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_mode_contact_cross_ref_contactId` ON `mode_contact_cross_ref` (`contactId`)"
        )
    }
}

@Database(
    entities = [
        ContactEntity::class,
        CallLogEntity::class,
        SpamReportEntity::class,
        DrivingModeLogEntity::class,
        AppSettingsEntity::class,
        ModeEntity::class,
        ModeContactCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SmartCallShieldDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callLogDao(): CallLogDao
    abstract fun spamReportDao(): SpamReportDao
    abstract fun drivingModeLogDao(): DrivingModeLogDao
    abstract fun modeDao(): ModeDao

    companion object {
        @Volatile
        private var INSTANCE: SmartCallShieldDatabase? = null

        fun getDatabase(context: Context): SmartCallShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartCallShieldDatabase::class.java,
                    "smartcallshield_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
