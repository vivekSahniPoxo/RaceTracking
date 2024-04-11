package com.example.racetracking.bpet.datamodel

import android.os.Parcelable


//data class Rfid(val srNo:Int, val rfid:String, var isPassed:String)

data class Rfid(
    val srNo: Int,
    val rfid: String,
     var isPassed: String
) {
    fun getIsPassed(): String {
        return isPassed
    }

    fun setIsPassed(value: String) {
        isPassed = value
    }
}


