package com.example.racetracking.ppt.data


import com.google.gson.annotations.SerializedName

class PPTEventModel : ArrayList<PPTEventModel.PPTEventModelItem>(){
    data class PPTEventModelItem(
        @SerializedName("chestNo")
        val chestNo: String,
        @SerializedName("date")
        val date: String,
        @SerializedName("eventId")
        val eventId: Int,
        @SerializedName("id")
        val id: Int,
        @SerializedName("status")
        var status: String
    ){

    fun getIsStatus(): String {
        return status
    }

    fun setIsStatus(value: String) {
        status = value
    }}

}