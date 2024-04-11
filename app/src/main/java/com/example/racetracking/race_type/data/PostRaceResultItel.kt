package com.example.racetracking.race_type.data


import com.google.gson.annotations.SerializedName

data class PostRaceResultItelItem(
    val armyNumber: String,
    val chestNumber: String,
    val soldierType: String,
    val raceResultMasterId: Int,
    val startTime: String,
    val midPoint: String
)


//    data class PostRaceResultItelItem(
//        @SerializedName("active")
//        val active: Boolean,
//        @SerializedName("ageGroupMaster")
//        val ageGroupMaster: Int,
//        @SerializedName("ageGroupValue")
//        val ageGroupValue: String,
//        @SerializedName("armyNumber")
//        val armyNumber: String,
//        @SerializedName("chestNumber")
//        val chestNumber: String,
//        @SerializedName("company")
//        val company: Int,
//        @SerializedName("companyvalue")
//        val companyvalue: String,
//        @SerializedName("distance")
//        val distance: String,
//        @SerializedName("dob")
//        val dob: String,
//        @SerializedName("endTime")
//        val endTime: String,
//        @SerializedName("gender")
//        val gender: String,
//        @SerializedName("marks")
//        val marks: Int,
//        @SerializedName("midPoint")
//        val midPoint: String,
//        @SerializedName("name")
//        val name: String,
//        @SerializedName("posting")
//        val posting: String,
//        @SerializedName("raceResultMasterId")
//        val raceResultMasterId: Int,
//        @SerializedName("raceTypeMaster")
//        val raceTypeMaster: Int,
//        @SerializedName("raceTypeValue")
//        val raceTypeValue: String,
//        @SerializedName("rank")
//        val rank: Int,
//        @SerializedName("rankValue")
//        val rankValue: String,
//        @SerializedName("registrationId")
//        val registrationId: Int,
//        @SerializedName("resultCategory")
//        val resultCategory: String,
//        @SerializedName("soldierType")
//        val soldierType: String,
//        @SerializedName("startTime")
//        val startTime: String,
//        @SerializedName("unit")
//        val unit: Int,
//        @SerializedName("unitValue")
//        val unitValue: String
//    )
