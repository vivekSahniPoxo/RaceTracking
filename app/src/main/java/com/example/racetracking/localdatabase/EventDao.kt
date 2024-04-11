package com.example.racetracking.localdatabase

import androidx.room.*
import com.example.racetracking.bpet.datamodel.AttandeeData
import com.example.racetracking.bpet.datamodel.AttandeeDetails


import com.example.racetracking.bpet.datamodel.SubmitEventInRoomDB
import com.example.racetracking.race_type.data.RaceRegsModel


@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEvents(submitEventInRoomDB: SubmitEventInRoomDB)

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAttandeeDetails(raceRegsModel: AttandeeData)

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTempattanDetails(attandeeDetails: AttandeeDetails)

    @Query("SELECT * FROM Attandee_details")
    fun getAllAttandees(): List<AttandeeDetails>

    @Query("DELETE FROM Attandee_details")
    suspend fun deleteAttandeeDetailsTemp()


//    @Query("SELECT * FROM tableName")
//    fun getAllEventItems(): List<SubmitEventInRoomDB>
//
//    @Query("DELETE FROM tableName")
//    suspend fun deleteAllEventDetails()

    @Query("SELECT * FROM receDetails")
    fun getAllData(): List<AttandeeData>

    @Query("DELETE FROM receDetails")
    suspend fun deleteAttandeeDetails()


}