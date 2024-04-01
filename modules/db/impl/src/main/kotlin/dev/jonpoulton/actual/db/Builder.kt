package dev.jonpoulton.actual.db

import android.content.Context
import androidx.room.Room

// TODO: Handle downloading of database. Create from existing file?
fun buildDatabase(context: Context): ActualDatabase {
  return Room.databaseBuilder(context, ActualDatabase::class.java, "actual.db")
    .fallbackToDestructiveMigration()
    .build()
}
