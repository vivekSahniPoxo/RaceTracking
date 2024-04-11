package com.example.racetracking.race_type.viewModel

import com.example.racetracking.bpet.datamodel.AttandeeData
import com.example.racetracking.bpet.datamodel.AttandeeDetails

import com.example.racetracking.localdatabase.EventDao

class RaceRepository (private val eventDao: EventDao) {


    suspend fun addAttandeDetails(raceRegsModel: AttandeeData) {
        eventDao.addAttandeeDetails(raceRegsModel)
    }

    suspend fun addTempattanDetails(attandeeDetails: AttandeeDetails) {
        eventDao.addTempattanDetails(attandeeDetails)
    }

}