package com.netanel.clockit.di

import android.content.Context
import androidx.room.Room
import com.netanel.clockit.data.ShiftRepository
import com.netanel.clockit.data.local.AppDatabase
import com.netanel.clockit.data.local.ShiftDao
import com.netanel.clockit.data.local.ShiftRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "clockit.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideShiftDao(db: AppDatabase): ShiftDao = db.shiftDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShiftRepository(
        impl: ShiftRepositoryImpl
    ): ShiftRepository
}
