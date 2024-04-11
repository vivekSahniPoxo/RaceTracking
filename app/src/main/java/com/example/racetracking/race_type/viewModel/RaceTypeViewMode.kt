package com.example.racetracking.race_type.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.racetracking.bpet.datamodel.AttandeeData
import com.example.racetracking.bpet.datamodel.AttandeeDetails

import com.example.racetracking.localdatabase.EventDataBase
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

    fun addTempattanDetails(attandeeDetails: AttandeeDetails){
        viewModelScope.launch(Dispatchers.Main) {
            repository.addTempattanDetails(attandeeDetails)
        }
    }


//    fun addAttandeDetails(attandeeData: AttandeeData, callback: DataInsertionCallback) {
//        // Insert the data into Room database
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // Perform the database insertion
//                repository.addAttandeDetails(attandeeData)
//                callback.onDataInserted()
//            } catch (e: Exception) {
//                callback.onError(e)
//            }
//        }
//    }
}