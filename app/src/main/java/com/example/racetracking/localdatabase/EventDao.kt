package com.example.racetracking.localdatabase

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.racetracking.bpet.datamodel.*
import com.example.racetracking.ppt.data.PPTEventModel


import com.example.racetracking.race_type.data.RaceRegsModel
import com.example.racetracking.sprint.data.CreateSprintResultModelItem
import com.example.racetracking.sprint.data.SprintDataModel


@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun addEvents(submitEventInRoomDB: SubmitEventInRoomDB)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun twoPointSevenMeterTurning(onlytwoPointSevenFiveMtrTemp: onlytwoPointSevenFiveMtrTemp)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun tempVRope(vRope:VRope)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun tempMidOrTurningPoint(midOrTurningPoint: tempMidOrTurningPoint)

    @Query("SELECT * FROM temp_mid_turning_point")
    fun getTempMidOrturningPoint():List<tempMidOrTurningPoint>

    @Query("DELETE FROM temp_mid_turning_point")
    fun deletetempMidOrTurningPoint()

    @Query("SELECT * FROM race_attandee_details WHERE chestNumber = :chestNumber")
    suspend fun getAttandeeByChestNumber(chestNumber: String): RaceRegsModelItemRGS?

    @Query("SELECT * FROM ppt_event_db WHERE chestNo = :rfidNo")
    fun getPPTEventDetailsReport(rfidNo: String,): SubmitPPTEventLDB


    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun tempHRope(hRope: HRope)

    @Query("SELECT * FROM H_rope")
    fun getTempHRope():List<HRope>

    @Query("DELETE FROM H_rope")
    fun deleteTempHRope()

    @Query("SELECT * FROM temp_v_rope")
    fun getTempVRope():List<VRope>

    @Query("DELETE FROM temp_v_rope")
    fun delettempVRope()

    @Query("SELECT * FROM temp_two_point_sevent_mtr")
    fun getTempTwoPointSevenFiveMtr(): List<onlytwoPointSevenFiveMtrTemp>

    @Query("DELETE FROM temp_two_point_sevent_mtr")
    fun deletTempTwoPointSevenFiveMtr()

    @Insert (onConflict = OnConflictStrategy.NONE)
    suspend fun addAttandeeDetails(raceRegsModel: AttandeeData)

    @Transaction
    suspend fun updateAttandeeData(attandeeData: AttandeeData) {
        val existingData = getAttandeeDetailsByRfidNoofBPETEvent(attandeeData.chestNumber)

        if (existingData != null) {
            // Modify the specific field you want to update
            val updatedData = existingData.copy(startTime = attandeeData.startTime, midPoint = attandeeData.midPoint)

            // Insert the updated data
            addAttandeeDetails(updatedData)
        } else {
            // No existing data, insert the new data
            addAttandeeDetails(attandeeData)
        }
    }

    @Query("SELECT * FROM receDetails WHERE chestNumber = :rfidTag")
    fun getAttandeeDetailsByRfidNoofBPETEvent(rfidTag: String): AttandeeData

    @Query("SELECT * FROM receDetails WHERE chestNumber = :rfidTag")
    fun getAttandeeDetailsByRfidNoofPPTEvent(rfidTag: String): AttandeeData





    @Insert (onConflict = OnConflictStrategy.NONE)
    suspend fun addMidOrTurningPoint(updateMidTimeModelLocalDb: UpdateMidTimeModelLocalDb)

    @Insert (onConflict = OnConflictStrategy.NONE)
    suspend fun addTempattanDetails(attandeeDetails: AttandeeDetails)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun addBPETEvents(eventModelItem: EventModelItem)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun addPPTEvents(pptEventModel: PPTEventModelItem)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun addRaceType(raceType :RaceTypeDataModelItem)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun addAttandeDetails(attande:RaceRegsModelItemRGS)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun addSprintDataModelItem(sprintDataModelItem: SprintDataModelItem)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun addPPTEventSubmit(submitPPTEvent: SubmitPPTEventLDB)

    @Query("SELECT * FROM ppt_event_db")
    fun getAllPPTEventSubmit():List<SubmitPPTEventLDB>

    @Query("DELETE FROM ppt_event_db")
    fun deletPPTEventDetails()

    @Query("SELECT * FROM sprint_model")
    fun getAllSprintDataModel():List<SprintDataModelItem>

    @Query("DELETE FROM sprint_model")
    fun deleteSprintModel()

    @Query("DELETE FROM sprint_race_result")
    fun deleteSprintDetails()

    @Query("SELECT * FROM race_attandee_details")
    fun getAllAttandee(): List<RaceRegsModelItemRGS>

    @Query("DELETE FROM race_attandee_details")
    fun deletAttandees()

    @Query("SELECT * FROM BPET_Events")
    fun getAllBPETEventsDetails(): List<EventModelItem>

    @Query("SELECT * FROM ppt_events")
    fun getAllPPTEvent(): List<PPTEventModelItem>

    @Query("SELECT * FROM race_type")
    fun getAllRaceType(): List<RaceTypeDataModelItem>



    // @Query("SELECT * FROM race_attandee_details A LEFT JOIN receDetails B ON A.chestNumber = B.chestNumber WHERE B.chestNumber IS NULL AND B.raceResultMasterId = 1")
    @Query("SELECT A.* FROM race_attandee_details A LEFT JOIN receDetails B ON A.chestNumber = B.chestNumber WHERE B.chestNumber IS NULL OR B.raceResultMasterId!=1")
    //@Query("SELECT * FROM race_attandee_details")
    fun getAllRaceTypeBPET(): List<RaceRegsModelItemRGS>

    @Query("SELECT A.* FROM race_attandee_details A LEFT JOIN receDetails B ON A.chestNumber = B.chestNumber WHERE B.chestNumber IS NULL OR B.raceResultMasterId!=2")
    fun getAllRaceTypePPT(): List<RaceRegsModelItemRGS>

    @Query("DELETE FROM BPET_Events")
    suspend fun deleteBPETEvents()

    @Query("DELETE FROM ppt_events")
    suspend fun deletPPTEvent()

    @Query("DELETE FROM race_type")
    suspend fun deleteRaceType()

    @Query("SELECT * FROM Attandee_details")
    fun getAllAttandees(): List<AttandeeDetails>



    @Query("SELECT * FROM update_mid_Or_turning_time")
    fun getMidOrTurningPoint():List<UpdateMidTimeModelLocalDb>

    @Query("DELETE FROM Attandee_details")
    suspend fun deleteAttandeeDetailsTemp()

    @Insert (onConflict = OnConflictStrategy.NONE)
    suspend fun raceSprintDetails(createSprintResultModelItem: CreateSprintResultModelInLocalDb)

    @Query("SELECT * FROM sprint_race_result")
    fun getSprintDetails(): List<CreateSprintResultModelInLocalDb>


    @Query("SELECT * FROM bpet_event_details")
    fun getAllBPETEventDetails(): List<SubmitEventInRoomDB>

    @Query("DELETE FROM bpet_event_details")
    suspend fun deleteAllEventDetails()

    @Query("DELETE FROM update_mid_Or_turning_time")
    suspend fun deleteMidTime()

    @Query("SELECT * FROM receDetails")
    fun getAllData(): List<AttandeeData>

    @Query("DELETE FROM receDetails")
    suspend fun deleteAttandeeDetails()



    @Query("SELECT * FROM bpet_event_details WHERE chestNo = :chestNumber AND eventId = :eventId")
    fun getBPETEventDetailsByChestNumberAndEventId(chestNumber: String, eventId: Int): SubmitEventInRoomDB

    @Query("SELECT * FROM sprint_race_result WHERE rfidNo = :rfidNo AND raceEventId = :raceEventId")
    fun getSprintItemChestNumberAndEventID(rfidNo: String, raceEventId: Int): CreateSprintResultModelInLocalDb

    @Query("SELECT * FROM temp_mid_turning_point WHERE chestNo = :chestNo")
    fun getUpdateMidPointRfidNumber(chestNo: String): tempMidOrTurningPoint

    @Query("SELECT * FROM ppt_event_db WHERE chestNo = :rfidNo AND eventId = :eventId")
    fun getPPTEventDetails(rfidNo: String, eventId: Int): SubmitPPTEventLDB



    @Query("SELECT * FROM ppt_event_db")
    fun getAllRaceTypePPTEventDetails(): List<SubmitPPTEventLDB>

//    @Query("SELECT * FROM ppt_event_db WHERE chestNo = :rfidNo")
//    fun getPPTEventDetails(rfidNo: String ): SubmitPPTEventLDB


}


//}