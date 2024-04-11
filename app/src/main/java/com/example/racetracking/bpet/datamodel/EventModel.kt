package com.example.racetracking.bpet.datamodel


import com.google.gson.annotations.SerializedName

class EventModel : ArrayList<EventModel.EventModelItem>(){
    data class EventModelItem(
        @SerializedName("eventId")
        val eventId: Int,
        @SerializedName("eventName")
        val eventName: String
    )
}