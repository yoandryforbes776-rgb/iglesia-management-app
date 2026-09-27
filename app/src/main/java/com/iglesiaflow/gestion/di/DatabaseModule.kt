package com.iglesiaflow.gestion.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.iglesiaflow.gestion.core.security.CryptoManager
import com.iglesiaflow.gestion.data.local.AppDatabase
import com.iglesiaflow.gestion.data.local.dao.AdminDao
import com.iglesiaflow.gestion.data.local.dao.CommunicationDao
import com.iglesiaflow.gestion.data.local.dao.EventDao
import com.iglesiaflow.gestion.data.local.dao.FinanceDao
import com.iglesiaflow.gestion.data.local.dao.GroupDao
import com.iglesiaflow.gestion.data.local.dao.MemberDao
import com.iglesiaflow.gestion.data.local.dao.VolunteerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): AppDatabase {
        val builder = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()

        // Cifrado en reposo con SQLCipher. Si el dispositivo no puede cargar las
        // librerías nativas se continúa sin cifrado para no bloquear la app.
        val factory = createCipherFactory(context, cryptoManager)
        if (factory != null) builder.openHelperFactory(factory)

        return builder.build()
    }

    private fun createCipherFactory(
        context: Context,
        cryptoManager: CryptoManager
    ): SupportSQLiteOpenHelper.Factory? = try {
        SQLiteDatabase.loadLibs(context)
        SupportFactory(cryptoManager.databasePassphrase().toByteArray())
    } catch (error: Throwable) {
        Log.w("DatabaseModule", "SQLCipher no disponible, usando base de datos sin cifrar", error)
        null
    }

    @Provides fun provideMemberDao(db: AppDatabase): MemberDao = db.memberDao()
    @Provides fun provideFinanceDao(db: AppDatabase): FinanceDao = db.financeDao()
    @Provides fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()
    @Provides fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()
    @Provides fun provideVolunteerDao(db: AppDatabase): VolunteerDao = db.volunteerDao()
    @Provides fun provideCommunicationDao(db: AppDatabase): CommunicationDao = db.communicationDao()
    @Provides fun provideAdminDao(db: AppDatabase): AdminDao = db.adminDao()
}
