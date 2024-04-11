package com.example.racetracking.race_type.viewModel

import com.example.racetracking.bpet.datamodel.*

import com.example.racetracking.localdatabase.EventDao
import com.example.racetracking.ppt.data.PPTEventModel
import com.example.racetracking.sprint.data.CreateSprintResultModelItem

class RaceRepository (private val eventDao: EventDao) {

    suspend fun getAttandeeByChestNumber(chestNumber: String): RaceRegsModelItemRGS? {
        return eventDao.getAttandeeByChestNumber(chestNumber)
    }

    suspend fun getPPTEventDetailsReport(chestNumber: String): SubmitPPTEventLDB {
        return eventDao.getPPTEventDetailsReport(chestNumber)
    }


    suspend fun addAttandeDetails(raceRegsModel: AttandeeData) {
        eventDao.addAttandeeDetails(raceRegsModel)
    }

    suspend fun updateAttandeeData(raceRegsModel: AttandeeData) {
        eventDao.updateAttandeeData(raceRegsModel)
    }

    suspend fun addBPETEventSDetails(submitEventInRoomDB: SubmitEventInRoomDB) {
        eventDao.addEvents(submitEventInRoomDB)
    }

    suspend fun twoPointSevenMeterTurning(fiveMtrTemp: onlytwoPointSevenFiveMtrTemp) {
        eventDao.twoPointSevenMeterTurning(fiveMtrTemp)
    }

    suspend fun tempVRope(tempVRope:VRope){
        eventDao.tempVRope(tempVRope)
    }

    suspend fun tempMidOrTurningPoint(tempMidOrTurningPoint: tempMidOrTurningPoint){
        eventDao.tempMidOrTurningPoint(tempMidOrTurningPoint)
    }

    suspend fun temHRope(temHRope: HRope){
        eventDao.tempHRope(temHRope)
    }

    suspend fun addMidOrTurningPoint(updateMidTimeModelLocalDb: UpdateMidTimeModelLocalDb) {
        eventDao.addMidOrTurningPoint(updateMidTimeModelLocalDb)
    }

    suspend fun addTempattanDetails(attandeeDetails: AttandeeDetails) {
        eventDao.addTempattanDetails(attandeeDetails)
    }

    suspend fun raceSprintDetails(createSprintResultModelItem: CreateSprintResultModelInLocalDb) {
        eventDao.raceSprintDetails(createSprintResultModelItem)
    }

    suspend fun addBPETEvent(eventModelItem: EventModelItem){
        eventDao.addBPETEvents(eventModelItem)
    }

    suspend fun addPPTEvents(pptEventModel: PPTEventModelItem){
        eventDao.addPPTEvents(pptEventModel)
    }

    suspend fun addRaceType(raceType :RaceTypeDataModelItem){
        eventDao.addRaceType(raceType)
    }
    suspend fun addAttandeDetails(addAttandeDetails:RaceRegsModelItemRGS){
        eventDao.addAttandeDetails(addAttandeDetails)
    }

    suspend fun addSprintModel(sprintDataModelItem: SprintDataModelItem){
        eventDao.addSprintDataModelItem(sprintDataModelItem)
    }

    suspend fun addPPTEventSubmit(submitPPTEvent: SubmitPPTEventLDB){
        eventDao.addPPTEventSubmit(submitPPTEvent)
    }

}