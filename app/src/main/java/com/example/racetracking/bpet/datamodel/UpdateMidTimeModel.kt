package com.example.racetracking.bpet.datamodel


import com.google.gson.annotations.SerializedName


data class UpdateMidTimeModelItem(
        @SerializedName("chestNo")
        val chestNo: String,
        @SerializedName("midtime")
        val midtime: String
    )
