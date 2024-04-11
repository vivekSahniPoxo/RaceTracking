package com.example.racetracking.reports

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.R
import com.example.racetracking.bpet.datamodel.HRope
import com.example.racetracking.bpet.datamodel.SubmitPPTEvent
import com.example.racetracking.bpet.datamodel.VRope
import com.example.racetracking.databinding.ActivityReportsBinding
import com.example.racetracking.localdatabase.EventDataBase
import com.example.racetracking.race_type.adapter.EventAttandeeAdapter
import com.example.racetracking.race_type.data.PostRaceResultItelItem
import com.example.racetracking.race_type.data.RaceRegsModel
import com.example.racetracking.race_type.viewModel.RaceRepository
import com.example.racetracking.reports.ppt_report.PPTReportAdapter
import com.example.racetracking.utils.Cons
import com.example.racetracking.utils.convertToFormattedDate
import com.example.racetracking.utils.convertToFormattedTime
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_reports.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.filterList
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class ReportsActivity : AppCompatActivity() {
    lateinit var binding:ActivityReportsBinding
    var selected = ""
    var selectedItemPositon = 0
    var userTypeList = arrayListOf<String>()
    var pptEventList = arrayListOf<String>()
     var positionsFromSpinner = 0

    var masterList: MutableList<ReportDataModel> = mutableListOf()

    lateinit var eventAttandeeAdapter:ReportAdapter
    lateinit var pptEventAdapter:PPTReportAdapter
    lateinit var reportDataModel: ArrayList<ReportDataModel>
    lateinit var pptReportDataModel: kotlin.collections.ArrayList<pptReportDataModel>
    lateinit var tempReportDataModel:ArrayList<ReportDataModel>
    lateinit var  mList:ArrayList<ReportDataModel>
    lateinit var ppReportList:ArrayList<pptReportDataModel>
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        reportDataModel = arrayListOf()
        tempReportDataModel = arrayListOf()
        pptReportDataModel = arrayListOf()
        mList = arrayListOf()
        ppReportList = arrayListOf()
        eventAttandeeAdapter = ReportAdapter(mList)
        pptEventAdapter = PPTReportAdapter(ppReportList)

        userTypeList.add("Select Event Type")
        userTypeList.add("BPET")
        userTypeList.add("PPT")

        pptEventList.add(Cons.SELECTPPTEVENT)
        pptEventList.add(Cons.FIVEMETERSHUTTLE)
        pptEventList.add(Cons.CHINUPS)
        pptEventList.add(Cons.TOETOUCH)
        pptEventList.add(Cons.SIXTYMETERSPRINT)

        pptEventList.add(Cons.HUNDREDTYMETERSPRINT)
        pptEventList.add(Cons.THREEHUNDRED)
        selectPPTEvent()

        selectUserType()


        binding.imBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }



        binding.btnSelectDate.setOnClickListener {
            if (selectedItemPositon==0){
                showToast("Please select event")
            } else {

                showDatePickerDialog()
            }
        }




        binding.fiveKmTurning.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (selectedItemPositon == 1) {
                    val database = Room.databaseBuilder(
                        applicationContext,
                        EventDataBase::class.java,
                        "ArmyEventDataBase"
                    ).build()
                    lifecycleScope.launch {
                        val data = withContext(Dispatchers.IO) {
                            database.EventDao().getMidOrTurningPoint()
                        }

                        data.forEach {result ->
                            val updatedList = reportDataModel.map { item ->
                                if (result.chestNo.trim() == item.chestNumber.trim()) {
                                    val inputDateString = result.midtime
                                    val formattedTime = inputDateString.convertToFormattedTime("yyyy-MM-dd'T'HH:mm:ss.SSS", "h:mm:ss.SSS a")
                                    item.copy(FiveKMTurning = formattedTime)

                                } else {
                                    // Keep other items unchanged
                                    item
                                }

                            }
                            eventAttandeeAdapter.updateList(updatedList)
                            binding.listItem.adapter = eventAttandeeAdapter
                        }


                    }


                } else if (selectedItemPositon == 2) {
                    if (reportDataModel.isNotEmpty()) {
                        reportDataModel.clear()
                    }
                    val database = Room.databaseBuilder(
                        applicationContext,
                        EventDataBase::class.java,
                        "ArmyEventDataBase"
                    ).build()
                    lifecycleScope.launch {
                        val getAllPPTDetails = withContext(Dispatchers.IO) {
                            database.EventDao().getAllPPTEventSubmit()

                        }
                        getAllPPTDetails.forEach {
                            reportDataModel.add(ReportDataModel(it.chestNo, "", "", "", "","","","","","","","",""))
                        }
                    }

                    eventAttandeeAdapter = ReportAdapter(reportDataModel)
                    binding.listItem.adapter = eventAttandeeAdapter
                }

            } else {
                if (selectedItemPositon == 1) {
                    val database = Room.databaseBuilder(
                        applicationContext,
                        EventDataBase::class.java,
                        "ArmyEventDataBase"
                    ).build()
                    lifecycleScope.launch {
                        val data = withContext(Dispatchers.IO) {
                            database.EventDao().getMidOrTurningPoint()
                        }
                        data.forEach {result ->
                            val updatedList = reportDataModel.map { item ->
                                if (result.chestNo.trim() == item.chestNumber.trim()) {
                                    item.copy(FiveKMTurning = "NA")
                                } else {
                                    // Keep other items unchanged
                                    item
                                }

                            }

                            eventAttandeeAdapter.updateList(updatedList)
                            binding.listItem.adapter = eventAttandeeAdapter
                        }
                    }


                }
            }


        }

        binding.twoPointSevenFive.setOnCheckedChangeListener { buttonView, isChecked ->

            // Handle the CheckBox state change
            if (isChecked) {
                if (selectedItemPositon == 1) {
//                            if (reportDataModel.isNotEmpty()){
//                                reportDataModel.clear()
//                            }
                    val database = Room.databaseBuilder(
                        applicationContext,
                        EventDataBase::class.java,
                        "ArmyEventDataBase"
                    ).build()
                    lifecycleScope.launch {
                        val data = withContext(Dispatchers.IO) {
                            database.EventDao().getTempTwoPointSevenFiveMtr()
                        }


                        data.forEach { result ->
                            val updatedList = reportDataModel.map { item ->
                                if (result.chestNo.trim() == item.chestNumber.trim()) {
                                    item.copy(twoPointMtrDitch = result.passOrFail)
                                } else {
                                    // Keep other items unchanged
                                    item
                                }
                            }


                            eventAttandeeAdapter.updateList(updatedList)
                               binding.listItem.adapter = eventAttandeeAdapter

                            }
                        }
                    //}


                } else if (positionsFromSpinner == 2) {
//                            if (reportDataModel.isNotEmpty()){
//                                reportDataModel.clear()
//                            }
                    val database = Room.databaseBuilder(
                        applicationContext,
                        EventDataBase::class.java,
                        "ArmyEventDataBase"
                    ).build()
                    lifecycleScope.launch {
                        val getAllPPTDetails = withContext(Dispatchers.IO) {
                            database.EventDao().getAllPPTEventSubmit()
                        }
                        getAllPPTDetails.forEach {
                            reportDataModel.add(ReportDataModel(it.chestNo, "", "", "", "","","","","","","","",""))
                        }
                    }
                    eventAttandeeAdapter = ReportAdapter(reportDataModel)
                    binding.listItem.adapter = eventAttandeeAdapter
                }

            } else {
                if (selectedItemPositon==1){
                    val database = Room.databaseBuilder(
                        applicationContext,
                        EventDataBase::class.java,
                        "ArmyEventDataBase"
                    ).build()
                    lifecycleScope.launch {
                        val data = withContext(Dispatchers.IO) {
                            database.EventDao().getTempTwoPointSevenFiveMtr()
                        }


                        data.forEach { result ->
                            val updatedList = reportDataModel.map { item ->
                                if (result.chestNo.trim() == item.chestNumber.trim()) {
                                    item.copy(twoPointMtrDitch = "NA")
                                } else {
                                    // Keep other items unchanged
                                    item
                                }
                            }

                            eventAttandeeAdapter.updateList(updatedList)
                            binding.listItem.adapter = eventAttandeeAdapter

                        }
                    }
                }
            }


        }

        binding.checkVHope.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    if (selectedItemPositon == 1) {
                        val database = Room.databaseBuilder(
                            applicationContext,
                            EventDataBase::class.java,
                            "ArmyEventDataBase"
                        ).build()
                        lifecycleScope.launch {
                            val data = withContext(Dispatchers.IO) {
                                database.EventDao().getTempVRope()

                            }
                            data.forEach { result ->
                                val updatedList = reportDataModel.map { item ->
                                       // if (result.chestNo.trim() == item.chestNumber.trim()) {

                                            item.copy(vRope = result.passOrFail, chestNumber = item.chestNumber)
                                        //} else {
                                            // Keep other items unchanged
                                        //    item
                                        //}
                                    }
                                Log.d("VRope",result.passOrFail)
                                   eventAttandeeAdapter.updateList(updatedList)
                                    //eventAttandeeAdapter = ReportAdapter(updatedList as MutableList<ReportDataModel>)
                                    binding.listItem.adapter = eventAttandeeAdapter

                               // }
                            }
                        }


                    }
                } else {
                    if (selectedItemPositon==1){
                        val database = Room.databaseBuilder(
                            applicationContext,
                            EventDataBase::class.java,
                            "ArmyEventDataBase"
                        ).build()
                        lifecycleScope.launch {
                            val data = withContext(Dispatchers.IO) {
                                database.EventDao().getTempVRope()
                            }


                            data.forEach { result ->
                                val updatedList = reportDataModel.map { item ->
                                    if (result.chestNo.trim() == item.chestNumber.trim()) {
                                        item.copy(vRope = "")

                                    } else {
                                        // Keep other items unchanged
                                        item
                                    }
                                }

                                eventAttandeeAdapter.updateList(updatedList)
                                binding.listItem.adapter = eventAttandeeAdapter

                            }
                        }
                    }
                }




            binding.checkHRope.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    if (selectedItemPositon == 1) {
                        val database = Room.databaseBuilder(
                            applicationContext,
                            EventDataBase::class.java,
                            "ArmyEventDataBase"
                        ).build()
                        lifecycleScope.launch {
                            val data = withContext(Dispatchers.IO) {
                                database.EventDao().getTempHRope()

                            }
                            data.forEach { result ->
                                val updatedList = reportDataModel.map { item ->
                                    if (result.chestNo.trim() == item.chestNumber.trim()) {
                                        item.copy(HRope = result.passOrFail)
                                    } else {
                                        // Keep other items unchanged
                                        item
                                    }
                                }

                                eventAttandeeAdapter.updateList(updatedList)
                                //eventAttandeeAdapter = ReportAdapter(updatedList as MutableList<ReportDataModel>)
                                binding.listItem.adapter = eventAttandeeAdapter

                                // }
                            }
                        }


                    }
                } else {
                    if (selectedItemPositon==1){
                        val database = Room.databaseBuilder(
                            applicationContext,
                            EventDataBase::class.java,
                            "ArmyEventDataBase"
                        ).build()
                        lifecycleScope.launch {
                            val data = withContext(Dispatchers.IO) {
                                database.EventDao().getTempHRope()
                            }


                            data.forEach { result ->

                                val updatedList = reportDataModel.map { item ->
                                    if (result.chestNo.trim() == item.chestNumber.trim()) {
                                        item.copy(HRope = "")
                                    } else {
                                        // Keep other items unchanged
                                        item
                                    }
                                }

                                eventAttandeeAdapter.updateList(updatedList)
                                binding.listItem.adapter = eventAttandeeAdapter

                            }
                        }
                    }
                }


            }


            //eventAttandeeAdapter = ReportAdapter(reportDataModel)


        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"

                binding.btnSelectDate.text = selectedDate

                if (selectedItemPositon == 1) {
                    val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()
                    lifecycleScope.launch {
                        val data = withContext(Dispatchers.IO) {
                            database.EventDao().getAllBPETEventDetails()
                        }

                        val tempList = arrayListOf<String>()
                        val filteredList = mutableListOf<ReportDataModel>()

                        data.forEach {
                            if (!tempList.contains(it.chestNo)) {
                                val dateTimeString = it.date
                                val formattedDate = dateTimeString.convertToFormattedDate()

                                if (formattedDate == selectedDate) {
                                    val attandeeDetailsDao = database.EventDao()
                                    val repository = RaceRepository(attandeeDetailsDao)
                                    val attandeeDetails = repository.getAttandeeByChestNumber(it.chestNo)

                                    if (attandeeDetails != null) {
                                        val dateOfBirth = attandeeDetails.dob.toString()
                                        val age = calculateAge(dateOfBirth) ?: "N/A"

                                        filteredList.add(
                                            ReportDataModel(
                                                it.chestNo,
                                                "NA",
                                                "NA",
                                                "NA",
                                                "NA",
                                                attandeeDetails.name.toString(),
                                                attandeeDetails.rankValue.toString(),
                                                attandeeDetails.armyNumber.toString(),
                                                age.toString(),
                                                attandeeDetails.soldierType.toString(),
                                                attandeeDetails.companyvalue.toString(),
                                                attandeeDetails.gender.toString(),
                                                attandeeDetails.posting.toString()
                                            )
                                        )
                                    }

                                    tempList.add(it.chestNo)
                                } else {
                                    Snackbar.make(binding.root, "No Data Found", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }

                        withContext(Dispatchers.Main) {
                            // Clear the existing list and add the filtered items
                            reportDataModel.clear()
                            reportDataModel.addAll(filteredList)

                            // Notify the adapter that the data has changed
                            //eventAttandeeAdapter.updateList(reportDataModel)

                            eventAttandeeAdapter = ReportAdapter(reportDataModel)
//
                            binding.listItem.adapter = eventAttandeeAdapter
                                eventAttandeeAdapter.notifyDataSetChanged()
                        }
                    }
                } else if (selectedItemPositon == 2) {
                    val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()
                    lifecycleScope.launch {
                        val getAllPPTDetails = withContext(Dispatchers.IO) {
                            database.EventDao().getAllPPTEventSubmit()
                        }

                        val tempPPTList = arrayListOf<String>()
                        val pptFilteredList = mutableListOf<pptReportDataModel>()

                        getAllPPTDetails.forEach {
//                            if (!tempPPTList.contains(it.chestNo)) {
//                                tempPPTList.add(it.chestNo)
                                val dateTimeString = it.date
                                val formattedDate = dateTimeString.convertToFormattedDate()

                                if (formattedDate == selectedDate) {
                                    val attandeeDetailsDao = database.EventDao()
                                    val repository = RaceRepository(attandeeDetailsDao)
                                    val attandeeDetails = repository.getAttandeeByChestNumber(it.chestNo)

                                    if (attandeeDetails != null) {
                                        pptFilteredList.add(
                                            pptReportDataModel(
                                                it.chestNo,
                                                it.status,
                                                attandeeDetails.name.toString(),
                                                attandeeDetails.rankValue.toString(),
                                                attandeeDetails.armyNumber.toString(),
                                                it.eventName.toString(),
                                                calculateAge(attandeeDetails.dob.toString()).toString() ?: "N/A",
                                                attandeeDetails.soldierType.toString(),
                                                attandeeDetails.companyvalue.toString(),
                                                attandeeDetails.gender.toString(),
                                                attandeeDetails.posting.toString()
                                            )
                                        )
                                    }
                                } else {
                                    Snackbar.make(binding.root, "No Data Found", Snackbar.LENGTH_SHORT).show()
                                }
                            //}
                        }

                        withContext(Dispatchers.Main) {
                            // Clear the existing list and add the filtered items
                            pptReportDataModel.clear()
                            pptReportDataModel.addAll(pptFilteredList)

                            // Notify the adapter that the data has changed
//                            pptEventAdapter.updateList(pptReportDataModel)

                            pptEventAdapter = PPTReportAdapter(pptReportDataModel)
                               binding.listItem.adapter = pptEventAdapter
                            pptEventAdapter.notifyDataSetChanged()
                        }
                    }
                }
            },
            currentYear,
            currentMonth,
            currentDay
        )

        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }



    private fun selectUserType(){

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, androidx.appcompat.R.layout.select_dialog_item_material, userTypeList ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(position, convertView, parent) as TextView

                positionsFromSpinner = position

                if (position == binding.spType.selectedItemPosition && position != 0) {
                    view.setTextColor(Color.parseColor("#000000"))
                }
                if (position == 0) {
                    view.setTextColor(Color.parseColor("#999999"))
                }

                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        binding.spType.adapter = adapter

        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                parent.getItemAtPosition(position).toString()
                 selectedItemPositon =   binding.spType.getItemIdAtPosition(position).toInt()
                if (parent.getItemAtPosition(position).toString() != "Select Event Type") {

                    selected = binding.spType.getItemIdAtPosition(position).toString()


                    //Toast.makeText(this@ReportsActivity, "position" + sel, Toast.LENGTH_SHORT).show()
                }



                 if (selectedItemPositon==1){
                     binding.tvChestNo.text = "Chest No"
                     binding.tvArmyNumber.text = "Army No"
                     binding.tvName.text = "Name"
                     binding.tvRank.text = "Rank"
                     binding.tv5kmTurningPoint.text = "Mid/Turning Point"
                     binding.tvTwoPointFiveMtr.text = "2.75 Mtr Ditch"
                     binding.tvHRope.text = "H-Rope"
                     binding.tvVRope.text = "V-Rope"
                     if (pptReportDataModel.isNotEmpty()){
                         pptReportDataModel.clear()
                        // pptEventAdapter.updateList(pptReportDataModel)
                         pptEventAdapter.notifyDataSetChanged()
                         binding.btnSelectDate.text = ""
                     }
                     binding.cardSelectEvents.isVisible = true
                     binding.cardSelectPptEvent.isVisible = false

                 } else if (selectedItemPositon==2){
                     binding.tvChestNo.text = "Chest No"
                     binding.tvArmyNumber.text = "Army No"
                     binding.tvName.text = "Name"
                     binding.tvRank.text = "Rank"
                     binding.tv5kmTurningPoint.text = "Status"
                     binding.tvTwoPointFiveMtr.text = "Event Name"
                     binding.tvHRope.text = ""
                     binding.tvVRope.text = ""
                     if (reportDataModel.isNotEmpty()){
                         reportDataModel.clear()
                         eventAttandeeAdapter.updateList(reportDataModel)
                         binding.btnSelectDate.text = ""
                     }



                     binding.cardSelectEvents.isVisible = false
                     binding.cardSelectPptEvent.isVisible = true

                 }



            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun calculateAge(dateOfBirth: String?): Int? {
        if (dateOfBirth.isNullOrEmpty()) {
            // Handle the case where dateOfBirth is null or empty
            return null
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        return try {
            val dob = dateFormat.parse(dateOfBirth)
            val today = Calendar.getInstance().time
            val diff = today.time - dob.time
            val age = diff / (1000L * 60 * 60 * 24 * 365) // milliseconds to years conversion
            age.toInt()
        } catch (e: ParseException) {
            e.printStackTrace()
            // Handle the parsing exception as needed
            null
        }
    }


    private fun selectPPTEvent(){

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, androidx.appcompat.R.layout.select_dialog_item_material, pptEventList ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(position, convertView, parent) as TextView

                positionsFromSpinner = position

                if (position == binding.selectPptEvent.selectedItemPosition && position != 0) {
                    view.setTextColor(Color.parseColor("#000000"))
                }
                if (position == 0) {
                    view.setTextColor(Color.parseColor("#999999"))
                }

                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        binding.selectPptEvent.adapter = adapter

        binding.selectPptEvent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SuspiciousIndentation")
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStatus = parent.getItemAtPosition(position).toString()

                if (selectedStatus != "Select Event Type") {
                   // pptEventAdapter.filterByStatus(selectedStatus)


                  val  filteredList = pptReportDataModel.filter { it.EventName == selectedStatus }
                        .plus(pptReportDataModel.filter { it.EventName != selectedStatus })
                     pptEventAdapter.eventNameFromActivity = selectedStatus
                    pptEventAdapter.updateList(updatedList = filteredList,binding.loader)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }


}