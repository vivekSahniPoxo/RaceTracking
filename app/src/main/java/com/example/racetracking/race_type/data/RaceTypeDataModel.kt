package com.example.racetracking.race_type.data


import com.google.gson.annotations.SerializedName

class RaceTypeDataModel : ArrayList<RaceTypeDataModel.RaceTypeDataModelItem>(){
    data class RaceTypeDataModelItem(
        @SerializedName("distance")
        val distance: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("raceType")
        val raceType: String
    )
}