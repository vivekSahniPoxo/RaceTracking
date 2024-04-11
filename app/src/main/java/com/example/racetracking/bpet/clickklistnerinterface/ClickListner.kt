package com.example.racetracking.bpet.clickklistnerinterface

import com.example.racetracking.bpet.datamodel.EventModel
import com.example.racetracking.race_type.data.RaceRegsModel

interface OnItemClickListener {
    fun onItemClick(item: EventModel.EventModelItem)
}

interface onItemPositionListenr {
    fun onItemClick(item: RaceRegsModel.RaceRegsModelItem,position:Int)
}