package com.example.racetracking.bpet.datamodel


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class UpdateMidTimeModelItem(
    @SerializedName("chestNo")
    val chestNo: String,
    @SerializedName("midtime")
    val midtime: String
)

@Entity(tableName = "update_mid_Or_turning_time")
data class UpdateMidTimeModelLocalDb(
    @PrimaryKey(autoGenerate = true)
         val id:Int,
        @SerializedName("chestNo")
        val chestNo: String,
        @SerializedName("midtime")
        val midtime: String
    )


@Entity(tableName = "cross_country")
data class CrossCountry(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    @SerializedName("chestNo")
    val chestNo: String,
    @SerializedName("midtime")
    val midtime: String
)
