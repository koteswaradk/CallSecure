package com.akshaglobal.smartcallshield.di

import android.content.Context
import com.akshaglobal.smartcallshield.data.database.CallSecureDatabase
import com.akshaglobal.smartcallshield.data.dao.CallLogDao
import com.akshaglobal.smartcallshield.data.dao.ContactDao
import com.akshaglobal.smartcallshield.data.dao.SpamReportDao
import com.akshaglobal.smartcallshield.data.dao.ModeDao
import com.akshaglobal.smartcallshield.data.repository.ModeRepository
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.service.ai.SpamDetectionModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CallSecureDatabase {
        return CallSecureDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideCallLogDao(database: CallSecureDatabase): CallLogDao {
        return database.callLogDao()
    }

    @Singleton
    @Provides
    fun provideContactDao(database: CallSecureDatabase): ContactDao {
        return database.contactDao()
    }

    @Singleton
    @Provides
    fun provideSpamReportDao(database: CallSecureDatabase): SpamReportDao {
        return database.spamReportDao()
    }

    @Singleton
    @Provides
    fun provideModeDao(database: CallSecureDatabase): ModeDao {
        return database.modeDao()
    }

    @Singleton
    @Provides
    fun provideModeRepository(modeDao: ModeDao, contactDao: ContactDao): ModeRepository {
        return ModeRepository(modeDao, contactDao)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Singleton
    @Provides
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Singleton
    @Provides
    fun provideSpamDetectionModel(
        @ApplicationContext context: Context
    ): SpamDetectionModel {
        val model = SpamDetectionModel(context)
        model.initialize()
        return model
    }
}
