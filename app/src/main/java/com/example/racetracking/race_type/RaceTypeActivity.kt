package com.example.racetracking.race_type


import android.annotation.SuppressLint
import android.app.Dialog

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.MainActivity
import com.example.racetracking.R
import com.example.racetracking.bpet.clickklistnerinterface.onItemPositionListenr
import com.example.racetracking.bpet.datamodel.AttandeeData

import com.example.racetracking.databinding.ActivityRaceTypeBinding
import com.example.racetracking.localdatabase.EventDataBase
import com.example.racetracking.race_type.adapter.EventAttandeeAdapter
import com.example.racetracking.race_type.adapter.RaceTypeAdapter
import com.example.racetracking.race_type.data.RaceRegsModel
import com.example.racetracking.race_type.data.RaceTypeDataModel
import com.example.racetracking.race_type.viewModel.RaceTypeViewMode
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.utils.App
import com.example.racetracking.utils.PopItemAnimator
import com.example.racetracking.utils.showCustomToast
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.racetracking.bpet.adapter.BPETAdapter
import com.example.racetracking.bpet.datamodel.AttandeeDetails
import com.example.racetracking.bpet.datamodel.EventModel
import com.example.racetracking.bpet.datamodel.RaceRegsModelItemRGS
import com.example.racetracking.race_type.attandees_details.AttandeesDetailsActivity
import com.example.racetracking.sprint.data.SprintDataModel
import com.example.racetracking.utils.Cons
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_race_type.*
import kotlinx.android.synthetic.main.quite_dialog.view.*
import kotlinx.coroutines.*
import java.time.LocalDate
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class RaceTypeActivity : AppCompatActivity(),TextToSpeech.OnInitListener,onItemPositionListenr {
    private var doubleBackToExitPressedOnce = false
    var startTime = ""
    private var mediaPlayer: MediaPlayer?=null
    var eventType_str = ""

    lateinit var binding:ActivityRaceTypeBinding
    lateinit var progressDialog: ProgressDialog
    lateinit var raceTypeAdapter: RaceTypeAdapter
    var isCountDownTimer = false
    lateinit var temList:ArrayList<String>
    val temp = arrayListOf<String>()
    private var isDialogShowing = false
    lateinit var mList: ArrayList<RaceTypeDataModel.RaceTypeDataModelItem>
    lateinit var attandeeList:kotlin.collections.ArrayList<RaceRegsModel.RaceRegsModelItem>
    lateinit var TempAttandeeList:kotlin.collections.ArrayList<RaceRegsModel.RaceRegsModelItem>
    lateinit var dialog:Dialog
    private var scanningJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val countdownNumbers = arrayOf("One", "Two", "Three", "Go")
    private var currentCount = 0
    private val countdownHandler = CountdownHandler()
    var localeDateTime = ""

    private val handlerthread = Handler(Looper.getMainLooper())
   // private var timer: CountDownTimer? = null


    lateinit var eventAttandeeAdapter:EventAttandeeAdapter
    var sRaceTypeId = 0
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var  handler: Handler
    private var soundId = 0
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0
    lateinit var tempList:ArrayList<String>
    var rfidNo = ""
   // private lateinit var textToSpeech: TextToSpeech
    var textToSpeech: TextToSpeech? = null
    lateinit var totalAttandeEList:kotlin.collections.ArrayList<String>
    lateinit var totalBatcNoList:kotlin.collections.ArrayList<String>
    private var timer: CountDownTimer? = null
    lateinit var localBDList:kotlin.collections.ArrayList<String>
    private val RaceTypeViewModel: RaceTypeViewMode by viewModels()
    var currentBatch = 0

    val allSyncRFidList = arrayListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityRaceTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler()
        mList = arrayListOf()
        tempList = arrayListOf()
        attandeeList = arrayListOf()
        totalAttandeEList = arrayListOf()
        totalBatcNoList = arrayListOf()
        temList = arrayListOf()
        try {
            iuhfService = UHFManager.getUHFService(this)
        }catch (e:Exception){
            Log.d("exception",e.toString())
        }
        textToSpeech = TextToSpeech(this, this)
        dialog = Dialog(this)
        TempAttandeeList = arrayListOf()
        localBDList = arrayListOf()

       // val database = Room.databaseBuilder(this, EventDataBase::class.java, "EventDataBase").build()
        eventAttandeeAdapter = EventAttandeeAdapter(attandeeList,this)
        initializeTextToSpeech()

       val database = Room.databaseBuilder(this@RaceTypeActivity, EventDataBase::class.java, "ArmyEventDataBase").build()

        lifecycleScope.launch {
            val sprintData = withContext(Dispatchers.IO) { database.EventDao().getAllRaceType() }
//            val attandeeData = withContext(Dispatchers.IO) { database.EventDao().getAllAttandee() }
//            val isAllreadyAttended = withContext(Dispatchers.IO) { database.EventDao().getAllData() }
            sprintData.forEach {
                mList.add(RaceTypeDataModel.RaceTypeDataModelItem(it.distance, it.id, it.raceType))
            }
        }
//
//
//                attandeeData.forEach {item->
//                    //if (isAllreadyAttended?.none { it.chestNumber == item.chestNumber && it.id == item.eventId } == true) {
//                        TempAttandeeList.add(
//                            RaceRegsModel.RaceRegsModelItem(
//                                item.active,
//                                item.ageGroupMaster,
//                                item.ageGroupValue,
//                                item.armyNumber,
//                                item.chestNumber,
//                                item.company,
//                                item.companyvalue,
//                                item.distance,
//                                item.dob,
//                                item.endTime,
//                                item.gender,
//                                item.marks,
//                                item.midPoint,
//                                item.name,
//                                item.posting,
//                                item.raceResultMasterId,
//                                item.raceTypeMaster,
//                                item.raceTypeValue,
//                                item.rank,
//                                item.rankValue,
//                                item.registrationId,
//                                item.resultCategory,
//                                item.soldierType,
//                                item.startTime,
//                                item.unit,
//                                item.unitValue
//                            )
//                        )
//                        tempList.add(item.chestNumber.toString())
//                    }
//                }
//            val gson = Gson()
//            val jsonArrayString = gson.toJson(TempAttandeeList)
//            Log.d("json",jsonArrayString)
//
//        }

        binding.btnStartAttendance.setOnClickListener {

            if (TempAttandeeList.isEmpty()){
                Snackbar.make(binding.root,"No Data Found",Snackbar.LENGTH_SHORT).show()
            } else {
//                val dataBase = EventDataBase.getDatabase(this@RaceTypeActivity)
//                val eventDao = dataBase.EventDao()
                if (binding.btnStart.isEnabled==false){
                    binding.btnStart.isEnabled = true
                    binding.btnStart.setBackgroundColor(ContextCompat.getColor(this, R.color.soft_green))
                    binding.btnStart.setTextColor(Color.WHITE)
                }
                if (!isInventoryRunning && !isCountDownTimer) {

                    if (binding.spType.selectedItemPosition==0){
                        Snackbar.make(binding.root,"Please select race type",Snackbar.LENGTH_SHORT).show()
                    } else {
                        startSearching()
                        binding.btnStartAttendance.text = Cons.Stop
                        binding.btnStartAttendance.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                        binding.btnStartAttendance.setTextColor(Color.WHITE)
                    }

                    scanningJob = coroutineScope.launch(Dispatchers.IO) {
                        iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun getInventoryData(var1: SpdInventoryData) {


                                runOnUiThread(Runnable {
                                    val rfidNo = var1.getEpc()
                                    temp.add(rfidNo)
                                    binding.count.text = temp.size.toString()
                                    temp.clear()
                                })

                                coroutineScope.launch{

                                    runOnUiThread(Runnable {
                                      val rfidNo =   var1.getEpc()
                                       handleUIItems(rfidNo)
                                    })
                                }

                            }

                            override fun onInventoryStatus(status: Int) {
                                threadpooling(status)
                            }
                            // Log.d("statusCode",status.toString())

                        })
                    }
                } else {
                    stopSearching()
//                    binding.btnStartAttendance.text = Cons.STARTATTENDANCE
//                    binding.btnStartAttendance.setBackgroundColor(ContextCompat.getColor(this, R.color.forest_green))
//                    binding.btnStartAttendance.setTextColor(Color.WHITE)
                    scanningJob?.cancel()
                }
            }
        }

        binding.cardBpet2.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, AttandeesDetailsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        mList.add(RaceTypeDataModel.RaceTypeDataModelItem("knck", 0, "Choose RaceTy.."))
        raceTypeAdapter = RaceTypeAdapter(this,mList)
        //getRaceType()

        binding.btnStart.setOnClickListener {
            if (attandeeList.isNotEmpty()) {


                // if (binding.btnStart.text=="Start") {
                //  binding.btnStart.text = "Stop"

                binding.spType.isEnabled = false

                if (isInventoryRunning) {
                    stopSearching()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   // countDownTimer()
                    countDownTimerWithRaw(this)
                }

                binding.btnStart.isEnabled = false
                binding.btnStart.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_color))
                binding.btnStart.setTextColor(Color.WHITE)


            } else{
                Snackbar.make(binding.root,"No attendees Found",Snackbar.LENGTH_SHORT).show()
        }

//        else if (binding.btnStart.text=="Stop"){
//                binding.btnStart.text = "Start"
//            }


        }

        binding.imBack.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, HomeActivity::class.java)
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }


        binding.btnSave.setOnClickListener {
            if (isInventoryRunning==true){
                stopSearching()
            }


            if (attandeeList.isNotEmpty()) {

                try {
                    for (getData in attandeeList) {
                        if (getData.startTime.isNotEmpty()) {
                            startTime = getData.startTime
                            val chestNumber = getData.chestNumber.toString()
                            val soldierType = getData.soldierType.toString()
                            // val startTime = getData.startTime.toString()

                            val midPoint = getData.midPoint ?: ""
                            // Handle null midPoint
                            val armyNumber = getData.armyNumber.toString()

                            try {
                                RaceTypeViewModel.addAttandeDetails(
                                    AttandeeData(
                                        0,
                                        sRaceTypeId,
                                        armyNumber,
                                        chestNumber,
                                        soldierType,
                                        sRaceTypeId,
                                        localeDateTime,
                                        midPoint
                                    )
                                )
                            } catch (e: Exception) {
                               // Log.d("ExceptionError", e.toString())

                            }


                            RaceTypeViewModel.addTempattanDetails(
                                AttandeeDetails(
                                    0,
                                    armyNumber,
                                    chestNumber,
                                    getData.companyvalue.toString(),
                                    getData.distance.toString(),
                                    getData.dob.toString(),
                                    getData.gender.toString(),
                                    getData.name.toString(),
                                    getData.raceTypeValue.toString(),
                                    getData.soldierType.toString(),
                                    getData.startTime.toString(),
                                    getData.unitValue.toString(),
                                    getData.posting.toString()
                                )
                            )
                            //Log.d("masterID", localeDateTime.toString())


                            localBDList.add(getData.chestNumber.toString())
                            binding.spType.isEnabled = true
                            if (binding.btnStart.text == "Stop") {
                                binding.btnStart.text = "Start"
                            }
                            // }

                            binding.btnStart.isEnabled = true
                            binding.spType.isEnabled = true
                            binding.btnStart.setBackgroundColor(ContextCompat.getColor(this, R.color.forest_green))
                            binding.btnStart.setTextColor(Color.WHITE)

                            totalBatcNoList.clear()
                            binding.totalBatchNo.text = ""
                            //attandeeList.clear()
                            runOnUiThread {
                                if (localBDList.size == attandeeList.size) {
                                    eventAttandeeAdapter.clear()
                                    localBDList.clear()
                                    attandeeList.clear()
                                    eventAttandeeAdapter.notifyDataSetChanged()
                                }
                            }

                        } else {
                            Snackbar.make(binding.root, "Please start race", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.d("exception", e.toString())
                }
                if (startTime.isNotEmpty()) {

                    currentBatch++
                    binding.tvCurrentBatch.text = currentBatch.toString()
                    startTime = ""

                    getSpTYpeData()
                    attandeeList.clear()
                    //getDataFromBD(sRaceTypeId)

                }



//                totalBatcNoList.clear()
//                binding.totalBatchNo.text = ""
//                //attandeeList.clear()
//                runOnUiThread {
//                    if (localBDList.size == attandeeList.size) {
//                        eventAttandeeAdapter.clear()
//                        localBDList.clear()
//                        attandeeList.clear()
//                        eventAttandeeAdapter.notifyDataSetChanged()
//                    }
//                }


            } else{
                Snackbar.make(binding.root,"No Attendee Found",Toast.LENGTH_SHORT).show()
            }
        }





        //var attandeeData = arrayListOf<RaceRegsModelItemRGS>()
        binding.listItem.adapter = eventAttandeeAdapter

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mList.map { it.raceType })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spType.adapter = raceTypeAdapter
        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                tempList.clear()
                attandeeList.clear()
                eventAttandeeAdapter.notifyDataSetChanged()
                val department = mList[position]
                sRaceTypeId = department.id

                getDataFromBD(sRaceTypeId)


//                val database = Room.databaseBuilder(
//                    this@RaceTypeActivity,
//                    EventDataBase::class.java,
//                    "ArmyEventDataBase"
//                ).build()
//
//var attandeeData= listOf<RaceRegsModelItemRGS>()
//                lifecycleScope.launch {
//                    if (sRaceTypeId == 1) {
//                        withContext(Dispatchers.IO) {
//                            attandeeData = database.EventDao().getAllRaceTypeBPET()
//                            Log.d("DataSize", attandeeData.toString())
//                          //  finaldata=attandeeData2
//                        }
//                    }
//
//                   else if (sRaceTypeId == 2) {
//
//                            withContext(Dispatchers.IO) {
//                                attandeeData = database.EventDao()
//                                    .getAllRaceTypePPT()
//                            }
//                            Log.d("DataSize", attandeeData.size.toString())
//                        }
//
//                    // Process attandeeData
//                    attandeeData.forEach {
//                        TempAttandeeList.add(
//                            RaceRegsModel.RaceRegsModelItem(
//                                it.active,
//                                it.ageGroupMaster,
//                                it.ageGroupValue,
//                                it.armyNumber,
//                                it.chestNumber,
//                                it.company,
//                                it.companyvalue,
//                                it.distance,
//                                it.dob,
//                                it.endTime,
//                                it.gender,
//                                it.marks,
//                                it.midPoint,
//                                it.name,
//                                it.posting,
//                                it.raceResultMasterId,
//                                it.raceTypeMaster,
//                                it.raceTypeValue,
//                                it.rank,
//                                it.rankValue,
//                                it.registrationId,
//                                it.resultCategory,
//                                it.soldierType,
//                                it.startTime,
//                                it.unit,
//                                it.unitValue
//                            )
//                        )
//                        tempList.add(it.chestNumber.toString())
//                    }
//            }
        }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }


        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                val child = binding.listItem.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    val position = binding.listItem.getChildAdapterPosition(child)
                    showYesNoDialog(position)
                    // 'position' now contains the position of the long-pressed item
                    // Use it as needed
                }
            }
        })

        binding.listItem.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(e)
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        })





    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun getRaceType(){
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            Snackbar.make(binding.root,"No Internet", Snackbar.LENGTH_SHORT).show()
            return
        } else{
//            progressDialog = ProgressDialog(this)
//            progressDialog.setMessage("Please wait...")
//            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
//            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().getRaceType().enqueue(object :
            Callback<RaceTypeDataModel> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<RaceTypeDataModel>, response: Response<RaceTypeDataModel>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                   // mList.add(RaceTypeDataModel.RaceTypeDataModelItem("iiiiiii", 0, "Choose RaceType"))
                    response.body()?.forEach {
                        mList.add(RaceTypeDataModel.RaceTypeDataModelItem(it.distance, it.id, it.raceType))
                    }

                        raceTypeAdapter  = RaceTypeAdapter(this@RaceTypeActivity,mList as kotlin.collections.ArrayList<RaceTypeDataModel.RaceTypeDataModelItem>)
                        binding.spType.adapter = raceTypeAdapter

                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RaceTypeDataModel>, t: Throwable) {
                Toast.makeText(this@RaceTypeActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAttandies(){
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

        RetrofitClient.getResponseFromApi().getEventAttandee().enqueue(object :
            Callback<RaceRegsModel> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<RaceRegsModel>, response: Response<RaceRegsModel>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                    response.body()?.forEach {
                        TempAttandeeList.add(RaceRegsModel.RaceRegsModelItem(it.active,it.ageGroupMaster,it.ageGroupValue,it.armyNumber,it.chestNumber,it.company,it.companyvalue,it.distance,it.dob,it.endTime,it.gender,it.marks,it.midPoint,it.name,it.posting,it.raceResultMasterId,it.raceTypeMaster,it.raceTypeValue,it.rank,it.rankValue,it.registrationId,it.resultCategory,it.soldierType,it.startTime,it.unit,it.unitValue))
                    }
//                    eventAttandeeAdapter  = EventAttandeeAdapter(response.body()!!)
//                    binding.listItem.adapter = eventAttandeeAdapter

                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RaceRegsModel>, t: Throwable) {
                Toast.makeText(this@RaceTypeActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }


    private fun startCountDown() {
        val countdownList = listOf("one", "two", "three", "go")

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Speech started
            }

            override fun onDone(utteranceId: String?) {
                // Speech completed
                if (utteranceId?.contains("go", ignoreCase = true) == true) {
                    // Call your method here when "go" is spoken
                    startSearching()
                }
            }

            override fun onError(utteranceId: String?) {
                // Speech error
            }
        })

        for (countdown in countdownList) {
            textToSpeech?.speak(countdown, TextToSpeech.QUEUE_FLUSH, null, countdown)
            // You can add a delay between speech if needed
            Thread.sleep(1000) // Sleep for 1 second
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun onGoSpeechCompleted() {
       // startSearching()

        val currentDateTime = LocalDateTime.now()
        localeDateTime = formatLocalDateTime(currentDateTime)
        val newStartTime = getCurrentTime()
        for (i in attandeeList.indices) {
            val currentItem = attandeeList[i]
            if (currentItem.startTime.isNullOrBlank()) {
                // Update the startTime only if it's null or an empty string
                val newItem = currentItem.copy(startTime = newStartTime)
                attandeeList[i] = newItem




            }
        }


        eventAttandeeAdapter = EventAttandeeAdapter(attandeeList,this@RaceTypeActivity)
        binding.listItem.adapter = eventAttandeeAdapter
        eventAttandeeAdapter.notifyDataSetChanged()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val formattedDate = localDateTime.atOffset(ZoneOffset.UTC).format(formatter)
        return formattedDate
    }

//    private fun countDownTimer(){
//         timer = object : CountDownTimer(4000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                val secondsLeft = millisUntilFinished / 1000
//                announceTime(secondsLeft.toInt().toString())
//                //Toast(this@RaceTypeActivity).showCustomToast (secondsLeft.toString(), this@RaceTypeActivity)
//                //isCountDownTimer = true
//                //timerPop(secondsLeft.toInt().toString())
//            }
//
//            override fun onFinish() {
//                announceTime("Go..")
//                //Toast(this@RaceTypeActivity).showCustomToast ("Go...", this@RaceTypeActivity)
//                //isCountDownTimer = false
//                eventAttandeeAdapter = EventAttandeeAdapter(attandeeList,this@RaceTypeActivity)
//                binding.listItem.adapter = eventAttandeeAdapter
//                dialog.dismiss()
//
//                startSearching()
//            }
//        }
////        initializeTextToSpeech()
//        (timer as CountDownTimer).start()
//    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.setSpeechRate(1.0f)
            } else {
              // Log.d("TextToSpeech","initialization failed")
            }
        }
    }


//    private fun countDownTimer() {
//        val totalDuration = countdownNumbers.size * 1000L // Total countdown duration
//        timer = object : CountDownTimer(totalDuration, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                val secondsLeft = (millisUntilFinished / 1000).toInt()
//                if (currentCount < countdownNumbers.size) {
//                    announceTime(countdownNumbers[currentCount])
//                    currentCount++
//                }
//            }
//
//            override fun onFinish() {
//                if (currentCount < countdownNumbers.size) {
//                    announceTime(countdownNumbers[currentCount]) // Announce "Go"
//                }
//                eventAttandeeAdapter = EventAttandeeAdapter(attandeeList, this@RaceTypeActivity)
//                binding.listItem.adapter = eventAttandeeAdapter
////                startSearching()
//            }
//        }
//
//        (timer as CountDownTimer).start()
//    }

//    private fun announceTime(text: String) {
//        val tts = TextToSpeech(this) { status ->
//            if (status == TextToSpeech.SUCCESS) {
//                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//            }
//        }
//    }


    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        try {
            startActivityForResult(intent, 100)
        } catch (e: ActivityNotFoundException) {
            // Handle the exception if speech recognition is not supported
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                handleVoiceCommand(matches[0])
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToUTC(originalTimestamp: String): String {
        // Define the format of the original timestamp
        val originalFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

        // Parse the original timestamp into a LocalDateTime
        val localDateTime = LocalDateTime.parse(originalTimestamp, originalFormat)

        // Convert the LocalDateTime to a ZonedDateTime in UTC
        val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC)

        // Define the format of the desired output
        val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        // Format the ZonedDateTime as a string in the desired format
        return zonedDateTime.format(outputFormat)
    }


    private fun speakOut() {
        val text = "One two three and Go"
        textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }
    private fun handleVoiceCommand(command: String) {
        when (command.toLowerCase()) {
            "1" -> {
                speakText("hii Vivek")
                //Log.d("Go","1")
            }
            "2" -> {
                speakText("Command 2")
               // Log.d("Go","2")
            }
            "3" -> {
                speakText("Command 3")
               // Log.d("Go","3")
            }
            "go" -> {
                speakText("Go command")
               // Log.d("Go","Go")
            }
            else -> {
                speakText("Command not recognized")
            }
        }
    }

    private fun speakText(text: String) {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        textToSpeech?.speak(text, QUEUE_FLUSH, null, null)
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_F1) {
            if (TempAttandeeList.isEmpty()) {
                Snackbar.make(binding.root, "No Data Found", Snackbar.LENGTH_SHORT).show()
            } else {
//            val dataBase = EventDataBase.getDatabase(this@RaceTypeActivity)
//            val eventDao = dataBase.EventDao()
                if (binding.btnStart.isEnabled == false) {
                    binding.btnStart.isEnabled = true
                    binding.btnStart.setBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.forest_green
                        )
                    )
                    binding.btnStart.setTextColor(Color.WHITE)
                }
                if (!isInventoryRunning && !isCountDownTimer) {

                    if (binding.spType.selectedItemPosition == 0) {
                        Snackbar.make(
                            binding.root,
                            "Please select race type",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        startSearching()
                    }

                    // startSearching()

                    // Start inventory service
                    iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                        @RequiresApi(Build.VERSION_CODES.O)
                        @SuppressLint("NotifyDataSetChanged")
                        override fun getInventoryData(var1: SpdInventoryData) {
                            val epc = var1.getEpc()
                            //val rfidNo = epc.substring(0, 4)
                            handleUIItems(epc)

                        }

                        override fun onInventoryStatus(p0: Int) {
                            Looper.prepare()
                            if (p0 == 65277) {
                                iuhfService.closeDev()
                                SystemClock.sleep(100)
                                iuhfService.openDev()
                                iuhfService.inventoryStart()
                            } else {
                                iuhfService.inventoryStart()
                            }
                            Looper.loop()
                        }

                    })
                } else {
                    stopSearching()
                    //isCountDownTimer = true
                }

            }
        }

//        else{
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                finish()
//            }
//        }
        return super.onKeyDown(keyCode, event)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun extractDateFromDateTime(dateTimeString: String): String {
        val dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
        return dateTime.toLocalDate().toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(): LocalDate {
        return LocalDate.now()
    }

//    override fun onBackPressed() {
//        if (isInventoryRunning || isCountDownTimer) {
//            stopSearching()
//
//
//        } else {
//            textToSpeech?.stop()
//            textToSpeech?.shutdown()
//            textToSpeech = null
//            timer?.cancel()
//            countdownHandler.removeCallbacksAndMessages(null)
//            super.onBackPressed() // Allow the default back behavior if not searching or in a countdown timer.
//        }
//    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun countDownTimerWithRaw(context: Context) {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.final_audio_file)
            mediaPlayer?.setOnCompletionListener {
                // Release the MediaPlayer when playback is completed
                mediaPlayer?.release()
                mediaPlayer = null
            }
            mediaPlayer?.start()

           handler.postDelayed({
               onGoSpeechCompleted()
           },4000)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun countDownTimer() {
        val countdownList = listOf("one", "two", "three", "go")
        val delayMillis = 1000L
        val mainHandler = Handler(Looper.getMainLooper())

        for ((index, countdown) in countdownList.withIndex()) {
            mainHandler.postDelayed({
                textToSpeech?.speak(countdown, TextToSpeech.QUEUE_FLUSH, null, countdown)

                // Delay for a short period before starting the next countdown
                val nextCountdownDelay = 500L
                mainHandler.postDelayed({
                    if (index == countdownList.lastIndex) {
                        onGoSpeechCompleted()
                    }
                }, nextCountdownDelay)
            }, index * delayMillis)
        }
    }


    //    @RequiresApi(Build.VERSION_CODES.O)
//    private fun countDownTimer() {
//        val countdownList = listOf("one", "two", "three", "go")
//        val delayMillis = 1000L
//
//        for ((index, countdown) in countdownList.withIndex()) {
//            countdownHandler.postDelayed({
//                textToSpeech?.speak(countdown, TextToSpeech.QUEUE_FLUSH, null, countdown)
//                if (index == countdownList.lastIndex) {
//
//                    onGoSpeechCompleted()
//                }
//            }, index * delayMillis)
//        }
//    }
    inner class CountdownHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            //Toast.makeText(this@RaceTypeActivity,msg.toString(),Toast.LENGTH_SHORT).show()
            // Handle countdown messages
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun convertDateFormat(inputDate: String): String {
        try {
            // Parse the input date
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val date = inputFormat.parse(inputDate)

            // Define the desired output format
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

            // Format the date to the desired format
            return outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
//        if (isCountDownTimer) {
//            countDownTimer()
//        }

        runOnUiThread(Runnable {
        binding.btnStartAttendance.text = Cons.Stop
        binding.btnStartAttendance.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
        binding.btnStartAttendance.setTextColor(Color.WHITE)
        binding.blinkingDot.isVisible = true

        val blinkAnimation = AlphaAnimation(1.0f, 0.0f)
        blinkAnimation.duration = 500 // Adjust the duration as needed
        blinkAnimation.repeatMode = Animation.REVERSE
        blinkAnimation.repeatCount = Animation.INFINITE

        // Start the animation
        binding.blinkingDot.startAnimation(blinkAnimation)
        isInventoryRunning = true
         timer?.cancel()
        initSoundPool()
        eventAttandeeAdapter = EventAttandeeAdapter(attandeeList,this@RaceTypeActivity)
        binding.listItem.adapter = eventAttandeeAdapter
        binding.tvTotalAttandee.text = totalAttandeEList.size.toString()
        binding.totalBatchNo.text = totalBatcNoList.size.toString()
       // eventAttandeeAdapter.notifyDataSetChanged()


        binding.spType.isEnabled = true
        })
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30
            eventAttandeeAdapter = EventAttandeeAdapter(attandeeList,this)
            binding.listItem.adapter = eventAttandeeAdapter
            binding.spType.isEnabled = false
        } catch (e:Exception){
           // Log.d("Exception",e.toString())
        }
        // binding.btnStart.text = "Stop"
        iuhfService.inventoryStart()
    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        soundPool!!.release()
        //binding.btnStart.text = "Start"
       // isCountDownTimer = false
        binding.btnStartAttendance.text = Cons.STARTATTENDANCERaceStart
        binding.btnStartAttendance.setBackgroundColor(ContextCompat.getColor(this, R.color.forest_green))
        binding.btnStartAttendance.setTextColor(Color.WHITE)
        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
        isCountDownTimer = false
        binding.spType.isEnabled = true
        textToSpeech?.stop()
        val blinkingDot = binding.blinkingDot
        blinkingDot.clearAnimation()
        blinkingDot.visibility = View.GONE

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTime(): String {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS a")
        return currentTime.format(formatter)
    }


    @SuppressLint("SetTextI18n")
    private fun showYesNoDialog(position: Int) {
        if (!isDialogShowing) {
            isDialogShowing = true

            dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.remove_item_dialog)
            dialog.setCancelable(true)
            dialog.show()

            val cancel: MaterialButton = dialog.findViewById(R.id.btnNo)
            cancel.setOnClickListener {
                dialog.dismiss()
                isDialogShowing = false
            }

            val yes: MaterialButton = dialog.findViewById(R.id.btnYEs)
            yes.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val indexToRemove = position
                    val indexForBatchList = position

                    if (indexToRemove in 0 until totalAttandeEList.size) {
                        totalAttandeEList.removeAt(indexToRemove)
                        binding.tvTotalAttandee.text = totalAttandeEList.size.toString()
                    }

                    if (indexForBatchList in 0 until totalBatcNoList.size) {
                        totalBatcNoList.removeAt(indexForBatchList)
                        binding.totalBatchNo.text = totalBatcNoList.size.toString()
                    }

                    val popItemAnimator = PopItemAnimator()
                    binding.listItem.itemAnimator = popItemAnimator

                    if (indexToRemove in 0 until attandeeList.size) {
                        val removeRfid = attandeeList[indexToRemove].chestNumber
                        attandeeList.removeAt(indexToRemove)
                        tempList.remove(removeRfid)
                        eventAttandeeAdapter.notifyItemRemoved(indexToRemove)
                    } else {
                        Log.e("IndexOutOfBounds", "Invalid position: $indexToRemove")
                    }

                    isDialogShowing = false
                }, 200)
                dialog.dismiss()
            }
        }
    }





//    @SuppressLint("SetTextI18n")
//    private fun showYesNoDialog(position: Int) {
//        if (!isDialogShowing) { // Check if the dialog is not already showing
//            isDialogShowing = true // Set the flag to true to indicate that the dialog is being displayed
//
//            dialog = Dialog(this)
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            dialog.setContentView(R.layout.remove_item_dialog)
//            dialog.setCancelable(true)
//            dialog.show()
//
//            val cancel: MaterialButton = dialog.findViewById(R.id.btnNo)
//            cancel.setOnClickListener {
//                dialog.dismiss()
//                isDialogShowing = false // Set the flag to false to indicate that the dialog is dismissed
//            }
//
//            val yes: MaterialButton = dialog.findViewById(R.id.btnYEs)
//            yes.setOnClickListener {
//                Handler(Looper.getMainLooper()).postDelayed({
//
//                    val indexToRemove = position
//                    val indexForBatchList = position
//
//
//                    if (indexToRemove >= 0 && indexToRemove < totalAttandeEList.size) {
//                        totalAttandeEList.removeAt(indexToRemove)
//                        binding.tvTotalAttandee.text = totalAttandeEList.size.toString()
//                    } else {
//                        binding.tvTotalAttandee.text = totalAttandeEList.size.toString()
//                    }
//
//                    if (indexForBatchList >= 0 && indexForBatchList <  totalBatcNoList.size) {
//                        totalBatcNoList.removeAt(indexForBatchList)
//                        binding.totalBatchNo.text =  totalBatcNoList.size.toString()
//                    } else {
//                        binding.totalBatchNo.text =  totalBatcNoList.size.toString()
//                    }
//                    val popItemAnimator = PopItemAnimator()
//                    binding.listItem.itemAnimator = popItemAnimator
//                    val positionToDelete = position // Replace with the position you want to delete
//                    attandeeList.removeAt(positionToDelete) // Remove the item from your dataset
//                    tempList.removeAt(position)
//                    eventAttandeeAdapter.notifyItemRemoved(positionToDelete)
//                    isDialogShowing = false // Set the flag to false to indicate that the dialog is dismissed
//                }, 200) // Adjust the delay time as needed
//                dialog.dismiss()
//            }
//        }
//    }








    fun initSoundPool() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attributes = AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build()
        } else {
            SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        }
        soundId = soundPool!!.load(this@RaceTypeActivity, com.example.racetracking.R.raw.beep, 0)
    }


    override fun onStop() {
        super.onStop()
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        textToSpeech?.shutdown()
        //soundPool?.release()
        try {
            soundPool?.release()

        } catch (e:Exception){

        }
        iuhfService.closeDev()
    }

    override fun onPause() {
        super.onPause()
        if (isInventoryRunning==true){
            stopSearching()

        }

        iuhfService.closeDev()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.US // Set the desired language
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(item: RaceRegsModel.RaceRegsModelItem,position: Int) {
      //  showYesNoDialog(position)
       // Log.d("itememe",item.name.toString())
        Toast.makeText(this,item.posting.toString(),Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        val receivedIntent = intent
        val receivedBundle = receivedIntent.extras
        val isCommingFromTotalAttendeesTrue = receivedBundle?.getBoolean(Cons.ISCOMMINGFROMTOTALATTENDEE)
        if (receivedBundle != null && isCommingFromTotalAttendeesTrue==false) {
            val database = Room.databaseBuilder(
                applicationContext,
                EventDataBase::class.java,
                "ArmyEventDataBase"
            ).build()
            GlobalScope.launch {
                val data = database.EventDao().getAllAttandees()
                if (data.isNotEmpty()) {
                    database.EventDao().deleteAttandeeDetailsTemp()
                }
            }
        }
    }


    fun threadpooling(p0:Int) {
        // Create a thread pool with 4 threads
        val threadPool: ExecutorService = Executors.newFixedThreadPool(4)

        // Define a Runnable with your code
        val runnable = Runnable {
            Looper.prepare()
            if (p0 == 65277) {
                // Log.d("p0", p0.toString())
                iuhfService.closeDev()
                SystemClock.sleep(100)
                startSearching()
            } else {
                iuhfService.inventoryStart()
            }
            Looper.loop()
        }

        // Submit the Runnable to the thread pool
                threadPool.submit(runnable)

                // Shutdown the thread pool when done
                threadPool.shutdown()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private  fun handleInventoryData(var1: SpdInventoryData) {
        try {
            val dataBase = EventDataBase.getDatabase(this@RaceTypeActivity)
            val eventDao = dataBase.EventDao()

            val timeMillis = System.currentTimeMillis()
            val l: Long = timeMillis - lastTimeMillis
            if (l < 100) {
                return
            }
            lastTimeMillis = System.currentTimeMillis()
            soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)
           // Log.d("RFFFF", var1.getEpc().substring(0,4))
            rfidNo = var1.getEpc().substring(0,4)
            if (rfidNo!=null) {
                if (!tempList.contains(rfidNo)) {
                    tempList.add(rfidNo)

                    val getAttandeeDetails =
                        TempAttandeeList.find { it.chestNumber == rfidNo }
                    if (getAttandeeDetails != null) {
                        val inputDate = getAttandeeDetails.dob
                        val outputDate = inputDate?.let { convertDateFormat(it) }
                        try {
                            if (binding.spType.selectedItemPosition==1) {
                                val getAttandeData =
                                    eventDao.getAttandeeDetailsByRfidNoofBPETEvent(
                                        rfidNo
                                    )
                                getAttandeData.let { item ->
                                    val chestNumber = item.chestNumber
                                    val startTime = extractDateFromDateTime(item.startTime)
                                   // Log.d("starttime", startTime)
                                    if (chestNumber == rfidNo && startTime == getCurrentDate().toString() && item.raceResultMasterId==1) {
                                    } else {
                                        attandeeList.add(
                                            RaceRegsModel.RaceRegsModelItem(
                                                getAttandeeDetails.active,
                                                getAttandeeDetails.ageGroupMaster,
                                                getAttandeeDetails.ageGroupValue,
                                                getAttandeeDetails.armyNumber,
                                                getAttandeeDetails.chestNumber,
                                                getAttandeeDetails.company,
                                                getAttandeeDetails.companyvalue,
                                                getAttandeeDetails.distance,
                                                outputDate,
                                                getAttandeeDetails.endTime,
                                                getAttandeeDetails.gender,
                                                getAttandeeDetails.marks,
                                                getAttandeeDetails.midPoint,
                                                getAttandeeDetails.name,
                                                getAttandeeDetails.posting,
                                                getAttandeeDetails.raceResultMasterId,
                                                getAttandeeDetails.raceTypeMaster,
                                                getAttandeeDetails.companyvalue,
                                                getAttandeeDetails.rank,
                                                getAttandeeDetails.rankValue,
                                                getAttandeeDetails.registrationId,
                                                getAttandeeDetails.resultCategory,
                                                getAttandeeDetails.soldierType,
                                                "",
                                                getAttandeeDetails.unit,
                                                getAttandeeDetails.unitValue
                                            )
                                        )
                                        runOnUiThread {

                                            binding.listItem.postDelayed({
                                                // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                                binding.listItem.scrollToPosition(
                                                    attandeeList.size
                                                )
                                            }, 100)
                                            totalAttandeEList.add(getAttandeeDetails.chestNumber.toString())
                                            totalBatcNoList.add(getAttandeeDetails.chestNumber.toString())
                                            eventAttandeeAdapter.notifyDataSetChanged()
                                            binding.tvTotalAttandee.text =
                                                totalAttandeEList.size.toString()
                                            binding.totalBatchNo.text =
                                                totalBatcNoList.size.toString()
                                        }

                                    }
//                                                else {
//                                                    Log.d("ChestNumer", chestNumber)
//                                                }
                                }

                            } else if (binding.spType.selectedItemPosition==2){
                                val getAttandeData = eventDao.getAttandeeDetailsByRfidNoofBPETEvent(rfidNo)
                                getAttandeData.let { item ->
                                    val chestNumber = item.chestNumber
                                    val startTime = extractDateFromDateTime(item.startTime)
                                   // Log.d("start time", startTime)
                                    if (chestNumber == rfidNo && startTime == getCurrentDate().toString() && item.raceResultMasterId==2) {
                                    } else{
                                        attandeeList.add(
                                            RaceRegsModel.RaceRegsModelItem(
                                                getAttandeeDetails.active,
                                                getAttandeeDetails.ageGroupMaster,
                                                getAttandeeDetails.ageGroupValue,
                                                getAttandeeDetails.armyNumber,
                                                getAttandeeDetails.chestNumber,
                                                getAttandeeDetails.company,
                                                getAttandeeDetails.companyvalue,
                                                getAttandeeDetails.distance,
                                                outputDate,
                                                getAttandeeDetails.endTime,
                                                getAttandeeDetails.gender,
                                                getAttandeeDetails.marks,
                                                getAttandeeDetails.midPoint,
                                                getAttandeeDetails.name,
                                                getAttandeeDetails.posting,
                                                getAttandeeDetails.raceResultMasterId,
                                                getAttandeeDetails.raceTypeMaster,
                                                getAttandeeDetails.companyvalue,
                                                getAttandeeDetails.rank,
                                                getAttandeeDetails.rankValue,
                                                getAttandeeDetails.registrationId,
                                                getAttandeeDetails.resultCategory,
                                                getAttandeeDetails.soldierType,
                                                "",
                                                getAttandeeDetails.unit,
                                                getAttandeeDetails.unitValue
                                            )
                                        )
                                        runOnUiThread {

                                            binding.listItem.postDelayed({
                                                // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                                binding.listItem.scrollToPosition(
                                                    attandeeList.size
                                                )
                                            }, 100)
                                            totalAttandeEList.add(getAttandeeDetails.chestNumber.toString())
                                            totalBatcNoList.add(getAttandeeDetails.chestNumber.toString())
                                            eventAttandeeAdapter.notifyDataSetChanged()
                                            binding.tvTotalAttandee.text =
                                                totalAttandeEList.size.toString()
                                            binding.totalBatchNo.text =
                                                totalBatcNoList.size.toString()
                                        }

                                    }
//                                                else {
//                                                    Log.d("ChestNumer", chestNumber)
//                                                }

                                }
                            }


                        }catch (e:Exception){
                            attandeeList.add(
                                RaceRegsModel.RaceRegsModelItem(
                                    getAttandeeDetails.active,
                                    getAttandeeDetails.ageGroupMaster,
                                    getAttandeeDetails.ageGroupValue,
                                    getAttandeeDetails.armyNumber,
                                    getAttandeeDetails.chestNumber,
                                    getAttandeeDetails.company,
                                    getAttandeeDetails.companyvalue,
                                    getAttandeeDetails.distance,
                                    outputDate,
                                    getAttandeeDetails.endTime,
                                    getAttandeeDetails.gender,
                                    getAttandeeDetails.marks,
                                    getAttandeeDetails.midPoint,
                                    getAttandeeDetails.name,
                                    getAttandeeDetails.posting,
                                    getAttandeeDetails.raceResultMasterId,
                                    getAttandeeDetails.raceTypeMaster,
                                    getAttandeeDetails.companyvalue,
                                    getAttandeeDetails.rank,
                                    getAttandeeDetails.rankValue,
                                    getAttandeeDetails.registrationId,
                                    getAttandeeDetails.resultCategory,
                                    getAttandeeDetails.soldierType,
                                    "",
                                    getAttandeeDetails.unit,
                                    getAttandeeDetails.unitValue
                                )
                            )
                            runOnUiThread {

                                binding.listItem.postDelayed({
                                    // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                    binding.listItem.scrollToPosition(attandeeList.size)
                                }, 100)
                                totalAttandeEList.add(getAttandeeDetails.chestNumber.toString())
                                totalBatcNoList.add(getAttandeeDetails.chestNumber.toString())
                                eventAttandeeAdapter.notifyDataSetChanged()
                                binding.tvTotalAttandee.text = totalAttandeEList.size.toString()
                                binding.totalBatchNo.text = totalBatcNoList.size.toString()
                            }



                        }



                    }
                }
            }


        } catch (e: Exception) {
           // Log.d("exception", e.toString())
        }
    }




    override fun onBackPressed() {
        val dialogView = layoutInflater.inflate(R.layout.quite_dialog, null)
        val builder = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()
        val database = Room.databaseBuilder(
            applicationContext,
            EventDataBase::class.java,
            "ArmyEventDataBase"
        ).build()
        dialogView.btn_yes.setOnClickListener {
            if (isInventoryRunning || isCountDownTimer) {
                stopSearching()
            }
           // else {
                mediaPlayer?.pause()
                mediaPlayer?.seekTo(0)
//                textToSpeech?.stop()
//                textToSpeech?.shutdown()
//                textToSpeech = null
//                timer?.cancel()
//                countdownHandler.removeCallbacksAndMessages(null)

                GlobalScope.launch {
                    val data = database.EventDao().getAllAttandees()
                    if (data.isNotEmpty()) {
                        database.EventDao().deleteAttandeeDetailsTemp()
                    }
                }
                super.onBackPressed()
           // }

            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            dialog.dismiss()

        }

        dialogView.btn_no.setOnClickListener {
            dialog.dismiss()
            // Handle any other action you want
        }

        dialog.show()
    }



    fun handleUIItems(RfidNo:String){
        try {
            //Log.d("RFFFF", var1.getEpc().substring(0,4))
                      // rfidNo =RfidNo
           val  rfidNo = RfidNo.substring(0,4)
            if (rfidNo!=null) {
                if (tempList.contains(rfidNo)) {
                    Log.d("RfidNo",rfidNo)
                    val getAttandeeDetails = TempAttandeeList.find { it.chestNumber == rfidNo }
                    if (getAttandeeDetails != null) {
                        val inputDate = getAttandeeDetails.dob
                        val outputDate = inputDate?.let { convertDateFormat(it) }
                        try {

                            attandeeList.add(
                                RaceRegsModel.RaceRegsModelItem(
                                    getAttandeeDetails.active,
                                    getAttandeeDetails.ageGroupMaster,
                                    getAttandeeDetails.ageGroupValue,
                                    getAttandeeDetails.armyNumber,
                                    getAttandeeDetails.chestNumber,
                                    getAttandeeDetails.company,
                                    getAttandeeDetails.companyvalue,
                                    getAttandeeDetails.distance,
                                    outputDate,
                                    getAttandeeDetails.endTime,
                                    getAttandeeDetails.gender,
                                    getAttandeeDetails.marks,
                                    getAttandeeDetails.midPoint,
                                    getAttandeeDetails.name,
                                    getAttandeeDetails.posting,
                                    getAttandeeDetails.raceResultMasterId,
                                    getAttandeeDetails.raceTypeMaster,
                                    getAttandeeDetails.companyvalue,
                                    getAttandeeDetails.rank,
                                    getAttandeeDetails.rankValue,
                                    getAttandeeDetails.registrationId,
                                    getAttandeeDetails.resultCategory,
                                    getAttandeeDetails.soldierType,
                                    "",
                                    getAttandeeDetails.unit,
                                    getAttandeeDetails.unitValue
                                )
                            )
                            runOnUiThread {
                                binding.listItem.scrollToPosition(attandeeList.size)
                                totalAttandeEList.add(getAttandeeDetails.chestNumber.toString())
                                totalBatcNoList.add(getAttandeeDetails.chestNumber.toString())
                                eventAttandeeAdapter.notifyDataSetChanged()
                                binding.tvTotalAttandee.text = totalAttandeEList.size.toString()
                                binding.totalBatchNo.text = totalBatcNoList.size.toString()
                            }

                        }catch (e:Exception){

                        }



                    }
                    tempList.remove(rfidNo)
                }

            }
            val timeMillis = System.currentTimeMillis()
            val l: Long = timeMillis - lastTimeMillis
            if (l < 100) {
                return
            }
            lastTimeMillis = System.currentTimeMillis()
            soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)
        } catch (e: Exception) {
            // Log.d("exception", e.toString())
        }
    }




//    override fun onBackPressed() {
//        if (doubleBackToExitPressedOnce) {
//            super.onBackPressed()
//            return
//        }
//
//        this.doubleBackToExitPressedOnce = true
//        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
//
//        Handler().postDelayed({
//            doubleBackToExitPressedOnce = false
//        }, 2000) // Change this delay as needed (in milliseconds)
//
//        if (doubleBackToExitPressedOnce==true) {
//            if (isInventoryRunning || isCountDownTimer) {
//                stopSearching()
//
//
//            } else {
//                textToSpeech?.stop()
//                textToSpeech?.shutdown()
//                textToSpeech = null
//                timer?.cancel()
//                countdownHandler.removeCallbacksAndMessages(null)
//                super.onBackPressed() // Allow the default back behavior if not searching or in a countdown timer.
//            }
//        }
//    }



    fun getDataFromBD(sRaceTypeId:Int){
        val database = Room.databaseBuilder(
            this@RaceTypeActivity,
            EventDataBase::class.java,
            "ArmyEventDataBase"
        ).build()

        var attandeeData= listOf<RaceRegsModelItemRGS>()
        lifecycleScope.launch {
            if (sRaceTypeId == 1) {
                withContext(Dispatchers.IO) {
                    attandeeData = database.EventDao().getAllRaceTypeBPET()
                    Log.d("DataSize", attandeeData.toString())
                    //  finaldata=attandeeData2
                }
            }

            else if (sRaceTypeId == 2) {

                withContext(Dispatchers.IO) {
                    attandeeData = database.EventDao()
                        .getAllRaceTypePPT()
                }
                Log.d("DataSize", attandeeData.size.toString())
            }

            // Process attandeeData
            attandeeData.forEach {
                TempAttandeeList.add(
                    RaceRegsModel.RaceRegsModelItem(
                        it.active,
                        it.ageGroupMaster,
                        it.ageGroupValue,
                        it.armyNumber,
                        it.chestNumber,
                        it.company,
                        it.companyvalue,
                        it.distance,
                        it.dob,
                        it.endTime,
                        it.gender,
                        it.marks,
                        it.midPoint,
                        it.name,
                        it.posting,
                        it.raceResultMasterId,
                        it.raceTypeMaster,
                        it.raceTypeValue,
                        it.rank,
                        it.rankValue,
                        it.registrationId,
                        it.resultCategory,
                        it.soldierType,
                        it.startTime,
                        it.unit,
                        it.unitValue
                    )
                )
                tempList.add(it.chestNumber.toString())
            }
        }
    }



    fun getSpTYpeData(){
        val database = Room.databaseBuilder(this@RaceTypeActivity, EventDataBase::class.java, "ArmyEventDataBase").build()

        lifecycleScope.launch {
            val sprintData = withContext(Dispatchers.IO) { database.EventDao().getAllRaceType() }
            sprintData.forEach {
                mList.add(RaceTypeDataModel.RaceTypeDataModelItem(it.distance, it.id, it.raceType))
            }
        }
    }

}