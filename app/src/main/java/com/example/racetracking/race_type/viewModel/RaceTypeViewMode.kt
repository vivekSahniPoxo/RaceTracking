package com.example.racetracking.race_type.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.racetracking.bpet.datamodel.*

import com.example.racetracking.localdatabase.EventDataBase
import com.example.racetracking.ppt.data.PPTEventModel
import com.example.racetracking.sprint.data.CreateSprintResultModelItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RaceTypeViewMode(application: Application): AndroidViewModel(application) {
    interface DataInsertionCallback {
        fun onDataInserted()
        fun onError(exception: Exception)
    }
    private var repository: RaceRepository

    init {
        val userDao = EventDataBase.getDatabase(application).EventDao()
        repository = RaceRepository(userDao)
    }

    fun addAttandeDetails(raceRegsModel: AttandeeData){
        viewModelScope.launch(Dispatchers.Main) {
            repository.addAttandeDetails(raceRegsModel)
        }
    }

    fun updateAttandeeData(raceRegsModel: AttandeeData){
        viewModelScope.launch(Dispatchers.Main) {
            repository.updateAttandeeData(raceRegsModel)
        }
    }

    fun addBPETEventsDetails(submitEventInRoomDB: SubmitEventInRoomDB){
        viewModelScope.launch {
            repository.addBPETEventSDetails(submitEventInRoomDB)
        }
    }

    fun twoPointSevenMeterTurning(fiveMtrTemp: onlytwoPointSevenFiveMtrTemp){
        viewModelScope.launch {
            repository.twoPointSevenMeterTurning(fiveMtrTemp)
        }
    }

    fun tempVRope(tempVRope:VRope){
        viewModelScope.launch {
            repository.tempVRope(tempVRope)
        }
    }

    fun tempMidOrTurningPoint(tempMidOrTurningPoint: tempMidOrTurningPoint){
        viewModelScope.launch {
            repository.tempMidOrTurningPoint(tempMidOrTurningPoint)
        }
    }

    fun tempHRope(tempHRope: HRope){
        viewModelScope.launch {
            repository.temHRope(tempHRope)
        }
    }

    fun addMidOrTurningPoint(updateMidTimeModelLocalDb: UpdateMidTimeModelLocalDb){
        viewModelScope.launch(Dispatchers.Main) {
            repository.addMidOrTurningPoint(updateMidTimeModelLocalDb)
        }
    }
    fun addTempattanDetails(attandeeDetails: AttandeeDetails){
        viewModelScope.launch(Dispatchers.Main) {
            repository.addTempattanDetails(attandeeDetails)
        }
    }

    fun raceSprintDetails(createSprintResultModelItem: CreateSprintResultModelInLocalDb){
        viewModelScope.launch(Dispatchers.Main) {
            repository.raceSprintDetails(createSprintResultModelItem)
        }
    }

    fun addBPETEvent(eventModelItem: EventModelItem){
        viewModelScope.launch {
            repository.addBPETEvent(eventModelItem)
        }
    }

    fun addPPTEvent(pptEventModel: PPTEventModelItem){
        viewModelScope.launch {
            repository.addPPTEvents(pptEventModel)
        }
    }

    fun addRaceType(raceType :RaceTypeDataModelItem){
        viewModelScope.launch {
            repository.addRaceType(raceType)
        }
    }

    fun addAttandeDetails(attandees:RaceRegsModelItemRGS){
        viewModelScope.launch {
            repository.addAttandeDetails(attandees)
        }
    }

     fun addSprintModel(sprintDataModelItem: SprintDataModelItem){
        viewModelScope.launch {
            repository.addSprintModel(sprintDataModelItem)
        }
    }

    fun addPPTEventSubmit(submitPPTEvent: SubmitPPTEventLDB){
        viewModelScope.launch {
            repository.addPPTEventSubmit(submitPPTEvent)
        }
    }



}