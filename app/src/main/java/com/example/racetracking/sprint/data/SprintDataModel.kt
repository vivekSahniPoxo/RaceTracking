package com.example.racetracking.sprint.data


import com.google.gson.annotations.SerializedName

class SprintDataModel : ArrayList<SprintDataModel.SprintDataModelItem>(){
    data class SprintDataModelItem(
        @SerializedName("raceEventId")
        val raceEventId: Int,
        @SerializedName("sprintName")
        val sprintName: String
    )
}