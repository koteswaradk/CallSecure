package com.akshaglobal.smartcallshield.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.akshaglobal.smartcallshield.data.dao.CallLogDao
import com.akshaglobal.smartcallshield.data.dao.ContactDao
import com.akshaglobal.smartcallshield.data.dao.DrivingModeLogDao
import com.akshaglobal.smartcallshield.data.dao.SpamReportDao
import com.akshaglobal.smartcallshield.data.model.AppSettingsEntity
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.model.DrivingModeLogEntity
import com.akshaglobal.smartcallshield.data.model.SpamReportEntity

@Database(
    entities = [
        ContactEntity::class,
        CallLogEntity::class,
        SpamReportEntity::class,
        DrivingModeLogEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SmartCallShieldDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callLogDao(): CallLogDao
    abstract fun spamReportDao(): SpamReportDao
    abstract fun drivingModeLogDao(): DrivingModeLogDao

    companion object {
        @Volatile
        private var INSTANCE: SmartCallShieldDatabase? = null

        fun getDatabase(context: Context): SmartCallShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartCallShieldDatabase::class.java,
                    "smartcallshield_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

