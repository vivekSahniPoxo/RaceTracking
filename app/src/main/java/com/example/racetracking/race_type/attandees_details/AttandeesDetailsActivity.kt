package com.example.racetracking.race_type.attandees_details

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.R
import com.example.racetracking.bpet.clickklistnerinterface.onItemPositionListenr
import com.example.racetracking.bpet.datamodel.AttandeeData
import com.example.racetracking.databinding.ActivityAttandeesDetailsBinding
import com.example.racetracking.localdatabase.EventDataBase
import com.example.racetracking.race_type.RaceTypeActivity
import com.example.racetracking.race_type.adapter.EventAttandeeAdapter
import com.example.racetracking.race_type.data.PostRaceResultItelItem
import com.example.racetracking.race_type.data.RaceRegsModel
import com.google.android.material.snackbar.Snackbar

class AttandeesDetailsActivity : AppCompatActivity(),onItemPositionListenr {
    lateinit var binding:ActivityAttandeesDetailsBinding
    lateinit var eventAttandeeAdapter: EventAttandeeAdapter
    lateinit var attandeeList:kotlin.collections.ArrayList<RaceRegsModel.RaceRegsModelItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttandeesDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        attandeeList = arrayListOf()
        eventAttandeeAdapter = EventAttandeeAdapter(attandeeList,this)
        val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()


        binding.imBack.setOnClickListener {
            val intent = Intent(this, RaceTypeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        Thread {
            val data = database.EventDao().getAllAttandees()
            if (data.isNotEmpty()) {
                val uniqueSet = HashSet<String>() // A set to store unique items based on a key (e.g., armyNumber)

                data.forEach { getData ->
                    // Generate a unique key for each item
                    val key = getData.armyNumber + getData.chestNumber // You can customize this key based on your requirements

                    if (!uniqueSet.contains(key)) { // Check if the item is unique based on the key
                        attandeeList.add(
                            RaceRegsModel.RaceRegsModelItem(
                                true,
                                0,
                                "",
                                getData.armyNumber,
                                getData.chestNumber,
                                0,
                                getData.companyvalue,
                                getData.distance,
                                getData.dob,
                                "",
                                getData.gender,
                                0,
                                "",
                                getData.name,
                                getData.posting,
                                0,
                                0,
                                "",
                                0,
                                getData.rankValue,
                                0,
                                0,
                                getData.soldierType,
                                getData.startTime,
                                0,
                                getData.unitValue
                            )
                        )
                        uniqueSet.add(key) // Add the key to the set to mark it as processed
                    }
                }

                eventAttandeeAdapter = EventAttandeeAdapter(attandeeList, this)
                binding.listItem.adapter = eventAttandeeAdapter
                eventAttandeeAdapter.notifyDataSetChanged()
            } else {
                Snackbar.make(binding.root, "No Data Found", Snackbar.LENGTH_SHORT).show()
            }
        }.start()


    }

    override fun onItemClick(item: RaceRegsModel.RaceRegsModelItem, position: Int) {
        
    }
}