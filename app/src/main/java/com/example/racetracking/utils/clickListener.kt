package com.example.racetracking.utils

import com.example.racetracking.bpet.datamodel.EventModel

interface ClickListener {
    fun onItemClick(item: EventModel.EventModelItem)
}
