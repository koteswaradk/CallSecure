package com.akshaglobal.smartcallshield.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.akshaglobal.smartcallshield.data.dao.CallLogDao
import com.akshaglobal.smartcallshield.data.dao.ContactDao
import com.akshaglobal.smartcallshield.data.dao.ModeDao
import com.akshaglobal.smartcallshield.data.dao.SpamReportDao
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.model.ModeContactCrossRef
import com.akshaglobal.smartcallshield.data.model.ModeEntity
import com.akshaglobal.smartcallshield.data.model.SpamReportEntity

@Database(
    entities = [
        ContactEntity::class,
        CallLogEntity::class,
        SpamReportEntity::class,
        ModeEntity::class,
        ModeContactCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CallSecureDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callLogDao(): CallLogDao
    abstract fun spamReportDao(): SpamReportDao
    abstract fun modeDao(): ModeDao

    companion object {
        @Volatile
        private var INSTANCE: CallSecureDatabase? = null

        fun getDatabase(context: Context): CallSecureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallSecureDatabase::class.java,
                    "smart_call_shield.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

typealias SmartCallShieldDatabase = CallSecureDatabase
