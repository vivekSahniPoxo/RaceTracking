package com.example.racetracking.sprint.data


import com.google.gson.annotations.SerializedName

//class CreateSprintResultModel : ArrayList<CreateSprintResultModelItem>(){
    data class CreateSprintResultModelItem(
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
//}