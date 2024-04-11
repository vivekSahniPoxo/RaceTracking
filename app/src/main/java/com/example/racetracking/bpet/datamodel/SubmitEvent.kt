package com.example.racetracking.bpet.datamodel

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.racetracking.utils.Cons
import com.google.gson.annotations.SerializedName

data class SubmitEvent(
    val id: Int,
    val eventId: Int,
    val chestNo: String,
    var isPassed: Int,
    val date: String
)


@Entity(tableName = "bpet_event_details")
data class SubmitEventInRoomDB( @PrimaryKey(autoGenerate = true)
    val id: Int,
    val eventId: Int,
    val chestNo: String,
    val isPassed: Int,
    val date: String, val eventName:String)

@Entity(tableName = "temp_mid_turning_point")
data class tempMidOrTurningPoint(@PrimaryKey(autoGenerate = true)
   val id:Int, val chestNo: String,
   val midTime:String
)

@Entity(tableName = "temp_two_point_sevent_mtr")
data class onlytwoPointSevenFiveMtrTemp(@PrimaryKey(autoGenerate = true)
                           val id:Int,
                             val chestNo: String,
                           val passOrFail:String
)
//
@Entity(tableName = "temp_v_rope")
data class VRope(@PrimaryKey(autoGenerate = true)
                           val id:Int,
                           val chestNo:String,
                           val passOrFail:String
)
//
@Entity(tableName = "H_rope")
data class HRope(@PrimaryKey(autoGenerate = true)
                           val id:Int,
                            val chestNo: String,
                           val passOrFail:String
)

data class SubmitPPTEvent(
    val id: Int,
    val eventId: Int,
    val chestNo: String,
    val status: String,
    val date: String
)

@Entity(tableName = "ppt_event_db")
data class SubmitPPTEventLDB(@PrimaryKey(autoGenerate = true)
    val ldbID:Int,
    val id: Int,
    val eventId: Int,
    val chestNo: String,
    val status: String,
    val date: String,
    val eventName:String
)

@Entity(tableName = "receDetails")
data class AttandeeData(@PrimaryKey (autoGenerate = true)
    val localDb:Int,
    val id: Int,
    val armyNumber: String,
    val chestNumber: String,
    val soldierType: String,
    val raceResultMasterId: Int,
    val startTime: String,
    val midPoint: String
)

@Entity(tableName = "Attandee_details")
data class AttandeeDetails(
    @PrimaryKey(autoGenerate = true)
      val id: Int,
    @SerializedName("active")
    val armyNumber: String,
    @SerializedName("chestNumber")
    val chestNumber: String,
    @SerializedName("companyvalue")
    val companyvalue: String,
    @SerializedName("distance")
    val distance: String,
    @SerializedName("dob")
    val dob: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("rankValue")
    val rankValue: String,
    @SerializedName("soldierType")
    val soldierType: String,
    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("unitValue")
    val unitValue: String,
    @SerializedName("posting")
    val posting: String,

)


@Entity(tableName = "race_type")
data class RaceTypeDataModelItem(
    @PrimaryKey(autoGenerate = true)
      val idLDB: Int,
        @SerializedName("distance")
        val distance: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("raceType")
        val raceType: String
    )


@Entity(tableName = "BPET_Events")
data class EventModelItem(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    @SerializedName("eventId")
    val eventId: Int,
    @SerializedName("eventName")
    val eventName: String
)

@Entity(tableName = "sprint_model")
data class SprintDataModelItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("raceEventId")
    val raceEventId: Int,
    @SerializedName("sprintName")
    val sprintName: String
)



@Entity(tableName = "ppt_events")
data class PPTEventModelItem(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    @SerializedName("eventId")
    val eventId: Int,
    @SerializedName("eventName")
    val eventName: String
)

@Entity(tableName = "sprint_race_result")
data class CreateSprintResultModelInLocalDb(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    @SerializedName("armyNumber")
    val armyNumber: String,
    @SerializedName("raceEventId")
    val raceEventId: Int,
    @SerializedName("rfidNo")
    val rfidNo: String,
    @SerializedName("soldiertype")
    val soldiertype: String,
    @SerializedName("sprintRaceTypeId")
    val sprintRaceTypeId: Int,
    @SerializedName("starttime")
    val starttime: String
)


@Entity(tableName = "race_attandee_details")
data class RaceRegsModelItemRGS(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("active")
    val active: Boolean,
    @SerializedName("ageGroupMaster")
    val ageGroupMaster: Int?,
    @SerializedName("ageGroupValue")
    val ageGroupValue: String?,
    @SerializedName("armyNumber")
    val armyNumber: String?,
    @SerializedName("chestNumber")
    val chestNumber: String?,
    @SerializedName("company")
    val company: Int,
    @SerializedName("companyvalue")
    val companyvalue: String?,
    @SerializedName("distance")
    val distance: String?,
    @SerializedName("dob")
    val dob: String?,
    @SerializedName("endTime")
    val endTime: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("marks")
    val marks: Int?,
    @SerializedName("midPoint")
    val midPoint: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("posting")
    val posting: String?,
    @SerializedName("raceResultMasterId")
    val raceResultMasterId: Int?,
    @SerializedName("raceTypeMaster")
    val raceTypeMaster: Int?,
    @SerializedName("raceTypeValue")
    val raceTypeValue: String?,
    @SerializedName("rank")
    val rank: Int?,
    @SerializedName("rankValue")
    val rankValue: String?,
    @SerializedName("registrationId")
    val registrationId: Int?,
    @SerializedName("resultCategory")
    val resultCategory: String?,
    @SerializedName("soldierType")
    val soldierType: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("unit")
    val unit: Int,
    @SerializedName("unitValue")
    val unitValue: String
)
