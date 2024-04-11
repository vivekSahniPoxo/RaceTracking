package com.example.racetracking.localdatabase

import android.content.Context
import android.media.metrics.Event
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.racetracking.bpet.datamodel.*

import com.example.racetracking.race_type.data.RaceRegsModel


@Database(entities = [SubmitEventInRoomDB::class,AttandeeData::class,AttandeeDetails::class,RaceRegsModelItemRGS::class,EventModelItem::class,PPTEventModelItem::class,SprintDataModelItem::class,RaceTypeDataModelItem::class,UpdateMidTimeModelLocalDb::class,CreateSprintResultModelInLocalDb::class,SubmitPPTEventLDB::class,onlytwoPointSevenFiveMtrTemp::class,VRope::class,HRope::class,tempMidOrTurningPoint::class], version = 21, exportSchema = false)
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