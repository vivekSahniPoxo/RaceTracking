package com.example.racetracking.Dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.SoundPool
import android.os.*
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.racetracking.R
import com.example.racetracking.bpet.BPETActivity
import com.example.racetracking.bpet.datamodel.*
import com.example.racetracking.crosscountry.AllCrossCountryEventsActivity
import com.example.racetracking.databinding.ActivityHomeBinding
import com.example.racetracking.localdatabase.EventDataBase
import com.example.racetracking.ppt.PActivity
import com.example.racetracking.sprint.race_sprint_activity.HundredMeterRaceActivity
import com.example.racetracking.race_type.RaceTypeActivity
import com.example.racetracking.race_type.data.PostRaceResultItelItem
import com.example.racetracking.race_type.data.RaceRegsModel
import com.example.racetracking.race_type.data.RaceTypeDataModel
import com.example.racetracking.race_type.viewModel.RaceTypeViewMode
import com.example.racetracking.reports.ReportsActivity
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.setting.SettingActivity
import com.example.racetracking.sprint.data.CreateSprintResultModelItem
import com.example.racetracking.sprint.data.SprintDataModel
import com.example.racetracking.utils.App
import com.example.racetracking.utils.Cons
import com.example.racetracking.utils.sharPref.SharePref
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.quite_dialog.view.*
import kotlinx.android.synthetic.main.remove_item_dialog.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    lateinit var binding:ActivityHomeBinding
    lateinit var progressDialog: ProgressDialog
    private var soundId = 0
    private var soundPool: SoundPool? = null
    val postBpetEventDetails = ArrayList<SubmitEvent>()
    lateinit var submitItemUpdatemidTime:ArrayList<UpdateMidTimeModelItem>
    lateinit var posdtRaceResultList:ArrayList<PostRaceResultItelItem>
    private val eventsViewModel: RaceTypeViewMode by viewModels()
    lateinit var postCreateSprintList:ArrayList<CreateSprintResultModelItem>
    lateinit var pptEventSubmit:ArrayList<SubmitPPTEvent>
    private var countdownTimer: CountDownTimer? = null
    private var alertDialog: AlertDialog? = null
    val postRaceResultList = ArrayList<PostRaceResultItelItem>()
    val pptEventSubmitOnServer = mutableListOf<SubmitPPTEvent>()
    val postCreateSprintListOnServer = mutableListOf<CreateSprintResultModelItem>()
    val submitItemUpdatemidTimeOnServer = mutableListOf<UpdateMidTimeModelItem>()
    val postBpetEventDetailsOnServer = mutableListOf<SubmitEvent>()

    lateinit var sharePref: SharePref

    companion object {
        private const val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 123
    }
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharePref = SharePref()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // Permission granted, proceed with writing to external storage
            } else {
                // Request the permission
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        } else {
            // For versions below Android 11, handle accordingly
        }


        try {
            sharePref = SharePref()
            val savedBaseUrl = sharePref.getData("baseUrl")
            if (savedBaseUrl != null && savedBaseUrl.isNotEmpty()) {
                Cons.BASE_URL = savedBaseUrl
                Log.d("baseURL", savedBaseUrl)
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }
        progressDialog = ProgressDialog(this)
        submitItemUpdatemidTime = arrayListOf()
        posdtRaceResultList = arrayListOf()
        postCreateSprintList = arrayListOf()
        pptEventSubmit = arrayListOf()
        val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()


       binding.imSync.setOnClickListener {
           getEventData()
           getPPTEvents()
           getAttandies()
           getRaceType()
           getSprint()
       }


        binding.btnSynceData.setOnClickListener {
            val delayMillis = 10000L
            Handler().postDelayed({
                progressDialog.dismiss()
            }, delayMillis)

            lifecycleScope.launch {
                try {
                    // Fetch and process data
                    val data = withContext(Dispatchers.IO) {
                        database.EventDao().getAllData()
                    }

                    if (data.isNotEmpty()){
                        val postRaceResultList = data.map {
                            PostRaceResultItelItem(it.armyNumber, it.chestNumber, it.soldierType, it.raceResultMasterId, it.startTime, it.midPoint)
                        }

                        if (postRaceResultList.isNotEmpty()) {
                            Toast.makeText(this@HomeActivity, "Total Submit Race Start: ${postRaceResultList.size}", Toast.LENGTH_SHORT).show()
                            postRaceResult(postRaceResultList as ArrayList<PostRaceResultItelItem>)
                        } else {

                            Snackbar.make(binding.root, "No data available for Race Start", Snackbar.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                    } else {
                        Snackbar.make(binding.root, "No data available for Race Start", Snackbar.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                } catch (e: Exception) {
                    Log.d("raceTytpeResultEmpty", e.toString())
                    progressDialog.dismiss()
                }

                try {
                    // Fetch and process BPETEventDetails
                    val BPETEventDetails = withContext(Dispatchers.IO) {
                        database.EventDao().getAllBPETEventDetails()
                    }

                    if (BPETEventDetails.isNotEmpty()) {
                        val postBpetEventDetails = BPETEventDetails.map {
                            SubmitEvent(0, it.eventId, it.chestNo, it.isPassed, it.date)
                        }

                        if (postBpetEventDetails.isNotEmpty()) {
                            Toast.makeText(this@HomeActivity, "Total Submit BPET Event: ${postBpetEventDetails.size}", Toast.LENGTH_SHORT).show()
                            submitEvent(postBpetEventDetails as ArrayList<SubmitEvent>)
                        } else {
                            progressDialog.dismiss()
                            Snackbar.make(binding.root, "No data available for BPET Event", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        progressDialog.dismiss()
                        Snackbar.make(binding.root, "No data available for BPET Event", Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.d("bpeEventDetails", e.toString())
                    progressDialog.dismiss()
                }

                try {
                    // Fetch and process UpdateMidTimeModelItem
                    val getUpdateMidTimeModelItem = withContext(Dispatchers.IO) {
                        database.EventDao().getMidOrTurningPoint()
                    }

                    if (getUpdateMidTimeModelItem.isNotEmpty()) {
                        val submitItemUpdatemidTime = getUpdateMidTimeModelItem.map {
                            UpdateMidTimeModelItem(it.chestNo, it.midtime)
                        }

                        if (submitItemUpdatemidTime.isNotEmpty()) {
                            Toast.makeText(this@HomeActivity, "Total Submit Mid/Turning Point: ${submitItemUpdatemidTime.size}", Toast.LENGTH_SHORT).show()
                            submitEventUpdateMidTime(submitItemUpdatemidTime as ArrayList<UpdateMidTimeModelItem>)
                        } else {
                            progressDialog.dismiss()
                            Snackbar.make(binding.root, "No data available for Mid/Turning Point", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        progressDialog.dismiss()
                        Snackbar.make(binding.root, "No data available for Mid/Turning Point", Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.d("updateMidTime", e.toString())
                    progressDialog.dismiss()
                }

//                try {
//                    // Fetch and process SprintDetails
//                    val getAllSprintDetails = withContext(Dispatchers.IO) {
//                        database.EventDao().getSprintDetails()
//                    }
//
//                    if (getAllSprintDetails.isNotEmpty()) {
//                        val postCreateSprintList = getAllSprintDetails.map {
//                            CreateSprintResultModelItem(it.armyNumber, it.raceEventId, it.rfidNo, it.soldiertype, it.sprintRaceTypeId, it.starttime)
//                        }
//
//                        if (postCreateSprintList.isNotEmpty()) {
//                            Toast.makeText(this@HomeActivity, "Total Submit Sprint Result: ${postCreateSprintList.size}", Toast.LENGTH_SHORT).show()
//                            createSprintResult(postCreateSprintList as ArrayList<CreateSprintResultModelItem>)
//                        } else {
//                            progressDialog.dismiss()
//                            Snackbar.make(binding.root, "No data available for Sprint Result", Snackbar.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        progressDialog.dismiss()
//                        Snackbar.make(binding.root, "No data available for Sprint Result", Snackbar.LENGTH_SHORT).show()
//                    }
//                } catch (e: Exception) {
//                    Log.d("sprintResult", e.toString())
//                    progressDialog.dismiss()
//                }

                try {
                    // Fetch and process PPT Event details
                    val getAllPPTDetails = withContext(Dispatchers.IO) {
                        database.EventDao().getAllPPTEventSubmit()
                    }

                    if (getAllPPTDetails.isNotEmpty()) {
                        val pptEventSubmit = getAllPPTDetails.map {
                            SubmitPPTEvent(it.id, it.eventId, it.chestNo, it.status, it.date)
                        }

                        if (pptEventSubmit.isNotEmpty()) {
                            Toast.makeText(this@HomeActivity, "Total Submit PPT Event: ${pptEventSubmit.size}", Toast.LENGTH_SHORT).show()
                            submitPPTEvent(pptEventSubmit as ArrayList<SubmitPPTEvent>)
                        } else {
                            progressDialog.dismiss()
                            Snackbar.make(binding.root, "No data available for PPT Events", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        progressDialog.dismiss()
                        Snackbar.make(binding.root, "No data available for Submit", Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.d("pptEventDetails", e.toString())
                    progressDialog.dismiss()
                }
            }
        }




        binding.raceSprint.setOnClickListener {
            val intent = Intent(this, HundredMeterRaceActivity::class.java)
            startActivity(intent)
        }


        binding.mCrossCountryCard.setOnClickListener {
            val intent = Intent(this, AllCrossCountryEventsActivity::class.java)
            startActivity(intent)
        }

        binding.mReportCard.setOnClickListener {
            val intent = Intent(this, ReportsActivity::class.java)
            startActivity(intent)
        }




            binding.cardBpet.setOnClickListener {
                val intent = Intent(this, BPETActivity::class.java)
                startActivity(intent)
            }

            binding.cardPpt.setOnClickListener {
                val intent = Intent(this, PActivity::class.java)
                startActivity(intent)
            }

            binding.raceType.setOnClickListener {
                val intent = Intent(this, RaceTypeActivity::class.java)
                startActivity(intent)
            }

            binding.cardReports.setOnClickListener {
                val intent = Intent(this, ReportsActivity::class.java)
                startActivity(intent)
            }

        binding.cardSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }


        }




    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPPTEvents() {
        if (!App.get().isConnected()) {
            Snackbar.make(binding.root, "No Internet", Snackbar.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        } else {
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().getPPTEvent().enqueue(object : Callback<EventModel> {
            override fun onResponse(call: Call<EventModel>, response: Response<EventModel>) {

                if (response.code() == 200) {
                    val database =
                        Room.databaseBuilder(this@HomeActivity, EventDataBase::class.java, "ArmyEventDataBase").build()

                    lifecycleScope.launch {
                        try {
                            // Delete existing data before adding new data
                            withContext(Dispatchers.IO) {
                                database.EventDao().deletPPTEvent()
                                database.EventDao().deletTempTwoPointSevenFiveMtr()
                                database.EventDao().delettempVRope()
                                database.EventDao().deleteTempHRope()
                                database.EventDao().deletetempMidOrTurningPoint()
                            }

                            // Add new data to the local database
                            response.body()?.forEach {
                                eventsViewModel.addPPTEvent(PPTEventModelItem(0, it.eventId, it.eventName))
                            }
                        } catch (e: Exception) {
                            Log.d("pptEvntNull", e.toString())
                        } finally {
                            // Dismiss the progress dialog after all data is added
                            progressDialog.dismiss()
                        }
                    }

                } else if (response.code() == 404 || response.code() == 400 || response.code() == 500) {
                    Snackbar.make(binding.root, response.body().toString(), Snackbar.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<EventModel>, t: Throwable) {
                Toast.makeText(this@HomeActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }






    @RequiresApi(Build.VERSION_CODES.M)
    private fun getEventData() {
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            Snackbar.make(binding.root, "No Internet", Snackbar.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        } else {
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().getEventS().enqueue(object : Callback<EventModel> {
            override fun onResponse(call: Call<EventModel>, response: Response<EventModel>) {

                if (response.code() == 200) {
                    val database = Room.databaseBuilder(
                        this@HomeActivity,
                        EventDataBase::class.java,
                        "ArmyEventDataBase"
                    ).build()

                    lifecycleScope.launch {
                        try {
                            // Delete existing data before adding new data
                            withContext(Dispatchers.IO) {
                                database.EventDao().deleteBPETEvents()
                            }

                            // Add new data to the local database
                            response.body()?.forEach {
                                eventsViewModel.addBPETEvent(EventModelItem(0, it.eventId, it.eventName))
                            }
                        } catch (e: Exception) {
                            Log.d("ExceptionPPTEvent", e.toString())
                        } finally {
                            // Dismiss the progress dialog after all data is added
                            progressDialog.dismiss()

                        }
                    }

                } else if (response.code() == 404 || response.code() == 400 || response.code() == 500) {
                    Snackbar.make(binding.root, response.body().toString(), Snackbar.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<EventModel>, t: Throwable) {
                Toast.makeText(this@HomeActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun postRaceResult(postRaceResult:ArrayList<PostRaceResultItelItem>){
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            Snackbar.make(binding.root,"No Internet", Snackbar.LENGTH_SHORT).show()
            return
        } else{
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().postRaceResult(postRaceResult).enqueue(object : Callback<String> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code()==200){
//                    Toast.makeText(this@HomeActivity, "Total Submit Race Start: ${postRaceResultList.size}", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    if (response.body()==null){
                        progressDialog.dismiss()
                        //submitEvent(postBpetEventDetails)
                    }
                  //  Toast.makeText(this@HomeActivity, "Total Submit Race Start: ${postRaceResultList.size}", Toast.LENGTH_SHORT).show()
                    postRaceResultList.clear()
                    val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()

                    lifecycleScope.launch {
                        try {
//                            val BPETEventDetails = database.EventDao().getAllBPETEventDetails()
//                            val getUpdateMidTimeModelItem = database.EventDao().getMidOrTurningPoint()
//                            val getAllSprintDetails = database.EventDao().getSprintDetails()
//                            val getAllPPTDetails = database.EventDao().getAllPPTEventSubmit()
//
//                            val postBpetEventDetails = mutableListOf<SubmitEvent>()
//                            val submitItemUpdatemidTime = mutableListOf<UpdateMidTimeModelItem>()
//                            val postCreateSprintList = mutableListOf<CreateSprintResultModelItem>()
//                            val pptEventSubmit = mutableListOf<SubmitPPTEvent>()
//
//                            BPETEventDetails.forEach {
//                                postBpetEventDetails.add(SubmitEvent(0, it.eventId, it.chestNo, it.isPassed, it.date))
//                            }
//
//                            getUpdateMidTimeModelItem.forEach {
//                                submitItemUpdatemidTime.add(UpdateMidTimeModelItem(it.chestNo, it.midtime))
//                            }
//
//                            getAllSprintDetails.forEach {
//                                postCreateSprintList.add(
//                                    CreateSprintResultModelItem(
//                                        it.armyNumber,
//                                        it.raceEventId,
//                                        it.rfidNo,
//                                        it.soldiertype,
//                                        it.sprintRaceTypeId,
//                                        it.starttime
//                                    )
//                                )
//                            }
//
//                            getAllPPTDetails.forEach {
//                                pptEventSubmit.add(SubmitPPTEvent(it.id, it.eventId, it.chestNo, it.status, it.date))
//                            }
//
//                            // Check and submit data if not empty
//                            if (postBpetEventDetails.isNotEmpty()) {
//                                submitEvent(postBpetEventDetails as ArrayList<SubmitEvent>)
//                            } else if (submitItemUpdatemidTime.isNotEmpty()) {
//                                submitEventUpdateMidTime(submitItemUpdatemidTime as ArrayList<UpdateMidTimeModelItem>)
//                            } else if (postCreateSprintList.isNotEmpty()) {
//                                createSprintResult(postCreateSprintList as ArrayList<CreateSprintResultModelItem>)
//                            } else if (pptEventSubmit.isNotEmpty()) {
//                                submitPPTEvent(pptEventSubmit as ArrayList<SubmitPPTEvent>)
//                            }

                            // Delete attandee details in a background thread
                            withContext(Dispatchers.IO) {
                                database.EventDao().deleteAttandeeDetails()
                            }

                        } catch (e: Exception) {
                            // Handle exceptions, such as database errors
                        }
                    }

                    //Toast.makeText(this@HomeActivity,response.body().toString(),Toast.LENGTH_SHORT).show()

                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@HomeActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        try {
            soundPool?.release()
        } catch (e:Exception){

        }

    }







            override fun onBackPressed() {
                val dialogView = layoutInflater.inflate(R.layout.quite_dialog, null)
                val builder = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)

                val dialog = builder.create()

                dialogView.btn_yes.setOnClickListener {
                    val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()
                    GlobalScope.launch {
                        val data = database.EventDao().getAllAttandees()
                        if (data.isNotEmpty()) {
                            database.EventDao().deleteAttandeeDetailsTemp()
                        }
                    }
                    dialog.dismiss()
                    finish() // Exit the app
                }

                dialogView.btn_no.setOnClickListener {
                    dialog.dismiss()
                    // Handle any other action you want
                }

                dialog.show()
            }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun submitEvent(submitEvent: ArrayList<SubmitEvent>){
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            Snackbar.make(binding.root,"No Internet", Snackbar.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        } else{
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().submitEvent(submitEvent).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                   // Toast.makeText(this@HomeActivity, "Total Submit BPET Event: ${postBpetEventDetailsOnServer.size}", Toast.LENGTH_SHORT).show()
                    postBpetEventDetailsOnServer.clear()
                    if (submitItemUpdatemidTime.isNotEmpty()) {
                        //submitEventUpdateMidTime(submitItemUpdatemidTime)
                    }
                    val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()

                    GlobalScope.launch {
                        database.EventDao().deleteAllEventDetails()

                    }

//                    val getUpdateMidTimeModelItem = database.EventDao().getMidOrTurningPoint() // Replace with your specific DAO and query
//                    getUpdateMidTimeModelItem.forEach {
//                        submitItemUpdatemidTime.add(UpdateMidTimeModelItem(it.chestNo,it.midtime))
//                    }
//                    if (submitItemUpdatemidTime.isNotEmpty()) {
//                        runOnUiThread {
//                            submitEventUpdateMidTime(submitItemUpdatemidTime)
//                        }
//                    }


                    Toast.makeText(this@HomeActivity,response.body().toString(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@HomeActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun submitEventUpdateMidTime(updateMidTimeActivity: ArrayList<UpdateMidTimeModelItem>){
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            Snackbar.make(binding.root,"No Internet", Snackbar.LENGTH_SHORT).show()
            return
        } else{
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().updateMidTime(updateMidTimeActivity).enqueue(object :
            Callback<String> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                    //Toast.makeText(this@HomeActivity, "Total Submit Mid/Turning Point: ${submitItemUpdatemidTimeOnServer.size}", Toast.LENGTH_SHORT).show()
                    submitItemUpdatemidTimeOnServer.clear()
                    val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()
                    binding.btnSynceData.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    database.EventDao().deleteMidTime()
                                }

                                if (postCreateSprintList.isNotEmpty()) {
                                    createSprintResult(postCreateSprintList)
                                } else {
                                    val getAllSprintDetails = withContext(Dispatchers.IO) {
                                        database.EventDao().getSprintDetails()
                                    }
                                    val postCreateSprintList = mutableListOf<CreateSprintResultModelItem>()

                                    getAllSprintDetails.forEach {
                                        postCreateSprintList.add(CreateSprintResultModelItem(it.armyNumber, it.raceEventId, it.rfidNo, it.soldiertype, it.sprintRaceTypeId, it.starttime))
                                    }

                                    if (postCreateSprintList.isNotEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            createSprintResult(postCreateSprintList as ArrayList<CreateSprintResultModelItem>)
                                        }
                                    } else {
                                        val getAllSprintDetails = withContext(Dispatchers.IO) {
                                            database.EventDao().getAllPPTEventSubmit()
                                        }
                                        val pptEventSubmit = mutableListOf<SubmitPPTEvent>()

                                        getAllSprintDetails.forEach {
                                            pptEventSubmit.add(SubmitPPTEvent(it.id, it.eventId, it.chestNo, it.status, it.date))
                                        }

                                        if (pptEventSubmit.isNotEmpty()) {
                                            withContext(Dispatchers.Main) {
                                                submitPPTEvent(pptEventSubmit as ArrayList<SubmitPPTEvent>)
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // Handle exceptions, such as database errors
                            }
                        }
                    }


                   // Toast.makeText(this@HomeActivity,response.body().toString(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@HomeActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun getRaceType() {
        if (!App.get().isConnected()) {
            Snackbar.make(binding.root, "No Internet", Snackbar.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        } else {
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().getRaceType().enqueue(object :
            Callback<RaceTypeDataModel> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<RaceTypeDataModel>, response: Response<RaceTypeDataModel>) {

                if (response.code() == 200) {
                    val database =
                        Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()

                    lifecycleScope.launch {
                        try {
                            // Delete existing data before adding new data
                            withContext(Dispatchers.IO) {
                                database.EventDao().deleteRaceType()
                            }

                            // Add new data to the local database
                            response.body()?.forEach {
                                eventsViewModel.addRaceType(
                                    RaceTypeDataModelItem(0, it.distance, it.id, it.raceType)
                                )
                            }
                        } catch (e: Exception) {
                            Log.d("ExceptionRaceType", e.toString())
                        } finally {
                            // Dismiss the progress dialog after all data is added
                            progressDialog.dismiss()
                        }
                    }
                } else if (response.code() == 404 || response.code() == 400 || response.code() == 500) {
                    Snackbar.make(binding.root, response.body().toString(), Snackbar.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<RaceTypeDataModel>, t: Throwable) {
                Toast.makeText(this@HomeActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }




    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAttandies() {
        if (!App.get().isConnected()) {
            Snackbar.make(binding.root, "No Internet", Snackbar.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        } else {
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().getEventAttandee().enqueue(object :
            Callback<RaceRegsModel> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<RaceRegsModel>, response: Response<RaceRegsModel>) {

                if (response.code() == 200) {
                    val database =
                        Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()

                    lifecycleScope.launch {
                        try {
                            // Delete existing data before adding new data
                            withContext(Dispatchers.IO) {
                                database.EventDao().deletAttandees()
                            }

                            // Add new data to the local database
                            response.body()?.forEach {
                                eventsViewModel.addAttandeDetails(
                                    RaceRegsModelItemRGS(
                                        0, it.active, it.ageGroupMaster, it.ageGroupValue, it.armyNumber,
                                        it.chestNumber, it.company, it.companyvalue, it.distance.toString(),
                                        it.dob, it.endTime, it.gender, it.marks, it.midPoint, it.name,
                                        it.posting, it.raceResultMasterId, it.raceTypeMaster, it.raceTypeValue,
                                        it.rank, it.rankValue, it.registrationId, it.resultCategory.toString(),
                                        it.soldierType, it.startTime, it.unit, it.unitValue
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.d("ExceptionAttandies", e.toString())
                        } finally {
                            // Dismiss the progress dialog after all data is added
                            progressDialog.dismiss()
                        }
                    }
                } else if (response.code() == 404 || response.code() == 400 || response.code() == 500) {
                    Snackbar.make(binding.root, response.body().toString(), Snackbar.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<RaceRegsModel>, t: Throwable) {
                Toast.makeText(this@HomeActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun getSprint() {
        if (!App.get().isConnected()) {
            Snackbar.make(binding.root, "No Internet", Snackbar.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        } else {
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().getSprintData().enqueue(object :
            Callback<SprintDataModel> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<SprintDataModel>, response: Response<SprintDataModel>) {

                if (response.code() == 200) {
                    val database =
                        Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()

                    lifecycleScope.launch {
                        try {
                            // Delete existing data before adding new data
                            withContext(Dispatchers.IO) {
                                database.EventDao().deleteSprintModel()
                            }

                            // Add new data to the local database
                            response.body()?.forEach {
                                eventsViewModel.addSprintModel(
                                    SprintDataModelItem(0, it.raceEventId, it.sprintName)
                                )
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } finally {
                            // Dismiss the progress dialog after all data is added
                            progressDialog.dismiss()
                            showWaitDialog()
                        }
                    }
                } else if (response.code() == 404 || response.code() == 400 || response.code() == 500) {
                    progressDialog.dismiss()
                    Snackbar.make(binding.root, response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SprintDataModel>, t: Throwable) {
                Toast.makeText(this@HomeActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun createSprintResult(createSprintResultModelItem: ArrayList<CreateSprintResultModelItem>){
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            Snackbar.make(binding.root,"No Internet", Snackbar.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        } else{
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().createSprintResult(createSprintResultModelItem).enqueue(object :
            Callback<String> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                    //Toast.makeText(this@HomeActivity, "Total Submit Sprint Result: ${postCreateSprintListOnServer.size}", Toast.LENGTH_SHORT).show()
                    postCreateSprintListOnServer.clear()
                    val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()

                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val data = database.EventDao().getSprintDetails()

                            if (data.isNotEmpty()) {
                                withContext(Dispatchers.IO) {
                                    database.EventDao().deleteSprintDetails()
                                }
                            }


                        } catch (e: Exception) {
                            // Handle exceptions, such as database errors
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    lifecycleScope.launch {
                        try {
                            val getAllSprintDetails = withContext(Dispatchers.IO) {
                                database.EventDao().getSprintDetails()
                            }

                                val getAllPPTDetails = withContext(Dispatchers.IO) {
                                    database.EventDao().getAllPPTEventSubmit()
                                }

                                val pptEventSubmit = mutableListOf<SubmitPPTEvent>()

                                getAllPPTDetails.forEach {
                                    pptEventSubmit.add(SubmitPPTEvent(it.id, it.eventId, it.chestNo, it.status, it.date))
                                }

                                if (pptEventSubmit.isNotEmpty()) {
                                    withContext(Dispatchers.Main) {
                                        submitPPTEvent(pptEventSubmit as ArrayList<SubmitPPTEvent>)
                                    }
                                }

                        } catch (e: Exception) {
                            // Handle exceptions, such as database errors
                        }
                    }



                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@HomeActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun submitPPTEvent(submitEvent: ArrayList<SubmitPPTEvent>){
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            Snackbar.make(binding.root,"No Internet", Snackbar.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        } else{
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().createPPTEventResult(submitEvent).enqueue(object :
            Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code()==200){
                    progressDialog.dismiss()
//                    Toast.makeText(this@HomeActivity, "Total Submit PPT Event: ${pptEventSubmitOnServer.size}", Toast.LENGTH_SHORT).show()
                    pptEventSubmitOnServer.clear()
                    val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()

                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val data = database.EventDao().getAllPPTEventSubmit()

                            if (data.isNotEmpty()) {
                                withContext(Dispatchers.IO) {
                                    database.EventDao().deletPPTEventDetails()
                                }
                            }


                        } catch (e: Exception) {
                            // Handle exceptions, such as database errors
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@HomeActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }



    private fun startCountdownTimer() {
        countdownTimer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the UI with the remaining seconds in the dialog
                alertDialog?.setMessage("Please wait... ${millisUntilFinished / 1000} seconds")
            }

            override fun onFinish() {
                // Timer finished, dismiss the dialog and perform the action
                alertDialog?.dismiss()

            }
        }

        // Start the countdown timer
        countdownTimer?.start()
    }

    private fun showWaitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Please wait... 15 seconds")
        builder.setCancelable(false)

        alertDialog = builder.create()

        // Show the dialog
        alertDialog?.show()

        // Start the countdown timer before calling getSprint()
        startCountdownTimer()
    }



}



