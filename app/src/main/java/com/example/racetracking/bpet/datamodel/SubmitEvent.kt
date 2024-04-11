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

@Entity(tableName = "tableName")
data class SubmitEventInRoomDB( @PrimaryKey(autoGenerate = true)
    val id: Int,
    val eventId: Int,
    val chestNo: String,
    val isPassed: Int,
    val date: String
)

data class SubmitPPTEvent(
    val id: Int,
    val eventId: Int,
    val chestNo: String,
    val status: String,
    val date: String
)

@Entity(tableName = "receDetails",indices = [Index(value = ["chestNumber"], unique = true)])
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