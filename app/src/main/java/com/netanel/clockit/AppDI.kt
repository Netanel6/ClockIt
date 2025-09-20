package com.netanel.clockit

import android.content.Context
import androidx.room.Room
import com.netanel.clockit.data.ShiftRepository

import com.netanel.clockit.data.local.AppDatabase
import com.netanel.clockit.data.local.ShiftRepositoryImpl

object AppDI {
    fun provideDb(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "clockit.db")
            .fallbackToDestructiveMigration()   // ← פיתוח
            .build()

    fun provideRepo(db: AppDatabase): ShiftRepository =
        ShiftRepositoryImpl(db.shiftDao())
}
