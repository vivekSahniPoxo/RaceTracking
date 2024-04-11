package com.example.racetracking.localdatabase

import android.content.Context
import android.media.metrics.Event
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.racetracking.bpet.datamodel.AttandeeData
import com.example.racetracking.bpet.datamodel.AttandeeDetails

import com.example.racetracking.bpet.datamodel.SubmitEventInRoomDB


@Database(entities = [SubmitEventInRoomDB::class,AttandeeData::class,AttandeeDetails::class], version = 9, exportSchema = false)
abstract class EventDataBase : RoomDatabase() {

    abstract fun EventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: EventDataBase? = null

        fun getDatabase(context: Context): EventDataBase {
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDataBase::class.java,
                    "ArmyEventDataBase")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }


}