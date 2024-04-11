package com.example.racetracking.sprint.hundredmtrrace

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.R
import com.example.racetracking.sprint.data.SprintDataModel
import com.example.racetracking.bpet.adapter.UpdateMidPointAdapter
import com.example.racetracking.bpet.datamodel.AttandeeData
import com.example.racetracking.bpet.datamodel.CreateSprintResultModelInLocalDb
import com.example.racetracking.bpet.datamodel.UpdateMidPoint
import com.example.racetracking.databinding.ActivityHundredMeterRaceBinding
import com.example.racetracking.localdatabase.EventDataBase
import com.example.racetracking.ppt.PActivity
import com.example.racetracking.race_type.RaceTypeActivity
import com.example.racetracking.race_type.adapter.EventAttandeeAdapter
import com.example.racetracking.race_type.adapter.RaceTypeAdapter
import com.example.racetracking.race_type.data.RaceRegsModel
import com.example.racetracking.race_type.data.RaceTypeDataModel
import com.example.racetracking.race_type.viewModel.RaceTypeViewMode
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.sprint.adapter.EventSPrintAdapter
import com.example.racetracking.sprint.adapter.SprintAdapter
import com.example.racetracking.sprint.data.CreateSprintResultModelItem
import com.example.racetracking.sprint.data.SprintAttandeeEvenModelClass
import com.example.racetracking.utils.App
import com.example.racetracking.utils.Cons
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import kotlinx.android.synthetic.main.quite_dialog.view.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class HundredMeterRaceActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var  handler: Handler
    lateinit var rfidList:ArrayList<UpdateMidPoint>
    lateinit var tempList:ArrayList<String>
    private var soundId = 0
    private var timer: CountDownTimer? = null
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0
    var rfidNo = ""
    val temp = arrayListOf<String>()
    var sSprintEventId = 0
    var isRaceStart = false
    private var scanningJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
   // lateinit var twoPointSevenFiveAdapter: UpdateMidPointAdapter
    lateinit var binding:ActivityHundredMeterRaceBinding
    var isCountDownTimer = false
    lateinit var TempAttandeeList:kotlin.collections.ArrayList<RaceRegsModel.RaceRegsModelItem>
    lateinit var mList: ArrayList<SprintDataModel.SprintDataModelItem>
   lateinit var createSprintList:ArrayList<CreateSprintResultModelItem>
    lateinit var postCreateSprintList:ArrayList<CreateSprintResultModelItem>
    lateinit var sprintAdater: SprintAdapter
    private val countdownHandler = CountdownHandler()
    var localeDateTimeShowing = ""
    var localeDateTimePosting = ""
    var textToSpeech: TextToSpeech? = null
    lateinit var eventSPrintAdapter: EventSPrintAdapter
    private val sprintRaceViewModel: RaceTypeViewMode by viewModels()
    lateinit var attandeeList:kotlin.collections.ArrayList<RaceRegsModel.RaceRegsModelItem>
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHundredMeterRaceBinding.inflate(layoutInflater)
        progressDialog = ProgressDialog(this)
        setContentView(binding.root)
        mList = arrayListOf()
        postCreateSprintList = arrayListOf()
        createSprintList = arrayListOf()
        TempAttandeeList =  arrayListOf()
        attandeeList =  arrayListOf()
        sprintAdater = SprintAdapter(this,mList)
        mList.add(SprintDataModel.SprintDataModelItem(0, "Choose Sprints"))
        eventSPrintAdapter = EventSPrintAdapter(attandeeList)

        rfidList = arrayListOf()
        tempList = arrayListOf()
        createSprintList = arrayListOf()

        val database = Room.databaseBuilder(this@HundredMeterRaceActivity, EventDataBase::class.java, "ArmyEventDataBase").build()

        lifecycleScope.launch {
            try {
                // Use async to perform both queries concurrently in the background thread
                val sprintData = withContext(Dispatchers.IO) { database.EventDao().getAllSprintDataModel() }
                val attandeeData = withContext(Dispatchers.IO) { database.EventDao().getAllAttandee() }

                // Process sprintData
                sprintData.forEach {
                    mList.add(SprintDataModel.SprintDataModelItem(it.raceEventId, it.sprintName))
                }

                // Process attandeeData
                attandeeData.forEach {
                    TempAttandeeList.add(
                        RaceRegsModel.RaceRegsModelItem(
                            it.active, it.ageGroupMaster, it.ageGroupValue, it.armyNumber, it.chestNumber,
                            it.company, it.companyvalue, it.distance, it.dob, it.endTime, it.gender,
                            it.marks, it.midPoint, it.name, it.posting, it.raceResultMasterId,
                            it.raceTypeMaster, it.raceTypeValue, it.rank, it.rankValue, it.registrationId,
                            it.resultCategory, it.soldierType, it.startTime, it.unit, it.unitValue
                        )
                    )
                }

            } catch (e: Exception) {
                Toast.makeText(this@HundredMeterRaceActivity, "No data found", Toast.LENGTH_SHORT).show()
            }
        }


        binding.btnStartAttendance.setOnClickListener {

            if (!isInventoryRunning) {
                if (binding.spType.selectedItemPosition==0){
                    Snackbar.make(binding.root,"Please select race sprint",Snackbar.LENGTH_SHORT).show()
                } else {
                    startSearching()
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
                                        handleInventoryData(var1)
                                    })
                                }

                            }

                            override fun onInventoryStatus(status: Int) {
                                threadpooling(status)
                            }


                        })
                    }
                } else {
                    stopSearching()
                    scanningJob?.cancel()
                }
            }






//        getSprint()
//        getAttandies()

        handler = Handler()
        //twoPointSevenFiveAdapter = EventSPrintAdapter(rfidList)
        iuhfService = UHFManager.getUHFService(this)
        initializeTextToSpeech()



        binding.btnStart.setOnClickListener {
            isRaceStart = true
            if (createSprintList.isNotEmpty()) {
                binding.spType.isEnabled = false

                if (isInventoryRunning) {
                    stopSearching()
                }

                countDownTimer()

                binding.btnStart.isEnabled = false
                binding.btnStart.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                binding.btnStart.setTextColor(Color.WHITE)


            } else{
                Snackbar.make(binding.root,"No attendees Found",Snackbar.LENGTH_SHORT).show()
            }
        }


        binding.btnSubmit.setOnClickListener {
            if (isInventoryRunning==true){
                stopSearching()
            }
            var startTime = ""
            if (postCreateSprintList.isNotEmpty()) {

                    // Process the postCreateSprintList in a coroutine scope tied to the ViewModel's lifecycle
                    postCreateSprintList.forEach {
                        if (it.starttime.isNotEmpty()) {
                            startTime = it.starttime
//                            sprintRaceViewModel.raceSprintDetails(
//                                CreateSprintResultModelInLocalDb(
//                                    0,
//                                    it.armyNumber,
//                                    it.raceEventId,
//                                    it.rfidNo,
//                                    it.soldiertype,
//                                    it.raceEventId,
//                                    it.starttime
//                                )
//                            )

//                            createSprintResult(postCreateSprintList)

//                            createSprintList.clear()
//                            postCreateSprintList.clear()
//                            eventSPrintAdapter.clear()
//                            binding.tvTotalAttandee.text = ""
//                            val twoPointSevenFiveAdapter = EventSPrintAdapter(attandeeList)
//                            binding.listItem.adapter = twoPointSevenFiveAdapter
//
//                            // createSprintResult(postCreateSprintList)
//                            // The following code outside the coroutine will run after the coroutine completes
//                            binding.btnStart.isEnabled = true
//                            binding.spType.isEnabled = true
//                            binding.btnStart.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#164B60"))
//                            binding.btnStart.setTextColor(Color.WHITE)

                        }else{
                            Snackbar.make(binding.root,"Please start race",Snackbar.LENGTH_SHORT).show()
                        }
                    }

                if (startTime.isNotEmpty()) {
                    startTime = ""
                    createSprintResult(postCreateSprintList)
                    createSprintList.clear()
                    postCreateSprintList.clear()
                    eventSPrintAdapter.clear()
                    binding.tvTotalAttandee.text = ""
                    val twoPointSevenFiveAdapter = EventSPrintAdapter(attandeeList)
                    binding.listItem.adapter = twoPointSevenFiveAdapter

                    // createSprintResult(postCreateSprintList)
                    // The following code outside the coroutine will run after the coroutine completes
                    binding.btnStart.isEnabled = true
                    binding.spType.isEnabled = true
                    binding.btnStart.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#164B60"))
                    binding.btnStart.setTextColor(Color.WHITE)
                }
            }

        }






        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mList.map { it.sprintName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spType.adapter = sprintAdater
        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val department = mList[position]
                sSprintEventId = department.raceEventId
                if (tempList.isNotEmpty()) {
                    tempList.clear()
                }


                try {

                } catch (e:Exception){

                }
                // Do something with departmentId
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        binding.imBack.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val formattedDate = localDateTime.atOffset(ZoneOffset.UTC).format(formatter)
        return formattedDate
    }

//    @SuppressLint("ResourceAsColor")
//    fun  startSearching(){
//        isInventoryRunning = true
//        initSoundPool()
//        try {
//            iuhfService = UHFManager.getUHFService(this)
//            iuhfService.openDev()
//            iuhfService.antennaPower = 30
//
//            val twoPointSevenFiveAdapter = EventSPrintAdapter(createSprintList)
//            binding.listItem.adapter = twoPointSevenFiveAdapter
//
//
//        } catch (e:Exception){
//            Log.d("Exception",e.toString())
//        }
//        // binding.btnStart.text = "Stop"
//        iuhfService.inventoryStart()
//    }



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
        soundId = soundPool!!.load(this@HundredMeterRaceActivity, R.raw.beep, 0)
    }

    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
//        if (isCountDownTimer) {
//            countDownTimer()
//
//        }

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
        val twoPointSevenFiveAdapter = EventSPrintAdapter(attandeeList)
        binding.listItem.adapter = twoPointSevenFiveAdapter

        runOnUiThread(Runnable {
            binding.btnStartAttendance.text = Cons.Stop
            binding.btnStartAttendance.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            binding.btnStartAttendance.setTextColor(Color.WHITE)
        })

        binding.spType.isEnabled = true
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30
            binding.spType.isEnabled = false
        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }
        // binding.btnStart.text = "Stop"
        iuhfService.inventoryStart()
    }

    override fun onStop() {
        super.onStop()
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_F1) {
            if (!isInventoryRunning) {
                val dataBase = EventDataBase.getDatabase(this@HundredMeterRaceActivity)
                val eventDao = dataBase.EventDao()
                if (binding.spType.selectedItemPosition==0){
                    Snackbar.make(binding.root,"Please select race sprint",Snackbar.LENGTH_SHORT).show()
                } else {
                    startSearching()
                }

                // Start inventory service
                iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    @SuppressLint("NotifyDataSetChanged")
                    override fun getInventoryData(var1: SpdInventoryData) {
                        try {
                            val timeMillis = System.currentTimeMillis()
                            val l: Long = timeMillis - lastTimeMillis
                            if (l < 100) {
                                return
                            }
                            lastTimeMillis = System.currentTimeMillis()
                            soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)
                            Log.d("RFFFF", var1.getEpc().substring(0,4))
                            rfidNo = var1.getEpc().substring(0,4)

                            if (rfidNo!=null) {
                                if (!tempList.contains(rfidNo)) {
                                    tempList.add(rfidNo)
                                    try {
                                        val getChestNumber =
                                            eventDao.getSprintItemChestNumberAndEventID(
                                                rfidNo,
                                                sSprintEventId
                                            )
                                        if (getChestNumber == null) {
                                            createSprintList.add(
                                                CreateSprintResultModelItem(
                                                    "Not Available",
                                                    sSprintEventId,
                                                    rfidNo,
                                                    "Not Available",
                                                    sSprintEventId,
                                                    ""
                                                )
                                            )

                                            rfidNo = var1.getEpc().substring(0,4)


                                            val getAttandeeDetails =
                                                TempAttandeeList.find { it.chestNumber == rfidNo }
                                            val inputDate = getAttandeeDetails?.dob
                                            val outputDate =
                                                inputDate?.let { convertDateFormat(it) }
                                            attandeeList.add(
                                                RaceRegsModel.RaceRegsModelItem(
                                                    getAttandeeDetails!!.active,
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
                                            postCreateSprintList.add(
                                                CreateSprintResultModelItem(
                                                    getAttandeeDetails.armyNumber.toString(),
                                                    sSprintEventId,
                                                    rfidNo,
                                                    getAttandeeDetails.soldierType.toString(),
                                                    sSprintEventId,
                                                    ""
                                                )
                                            )
                                            runOnUiThread {

                                                binding.listItem.postDelayed({
                                                    // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                                    binding.listItem.scrollToPosition(attandeeList.size)
                                                }, 100)

                                                binding.tvTotalAttandee.text =
                                                    attandeeList.size.toString()
                                                eventSPrintAdapter.notifyDataSetChanged()

                                            }

                                        } else if (getChestNumber.rfidNo == rfidNo && getChestNumber.raceEventId == sSprintEventId) {
                                        } else{
                                            /// rfidList.add(UpdateMidPoint("", rfidNo))

                                            createSprintList.add(
                                                CreateSprintResultModelItem(
                                                    "Not Available",
                                                    sSprintEventId,
                                                    rfidNo,
                                                    "Not Available",
                                                    sSprintEventId,
                                                    ""
                                                )
                                            )

                                            rfidNo = var1.getEpc().substring(0,4)


                                            val getAttandeeDetails =
                                                TempAttandeeList.find { it.chestNumber == rfidNo }
                                            val inputDate = getAttandeeDetails?.dob
                                            val outputDate =
                                                inputDate?.let { convertDateFormat(it) }
                                            attandeeList.add(
                                                RaceRegsModel.RaceRegsModelItem(
                                                    getAttandeeDetails!!.active,
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
                                            postCreateSprintList.add(
                                                CreateSprintResultModelItem(
                                                    getAttandeeDetails.armyNumber.toString(),
                                                    sSprintEventId,
                                                    rfidNo,
                                                    getAttandeeDetails.soldierType.toString(),
                                                    sSprintEventId,
                                                    ""
                                                )
                                            )
                                            runOnUiThread {

                                                binding.listItem.postDelayed({
                                                    // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                                    binding.listItem.scrollToPosition(attandeeList.size)
                                                }, 100)

                                                binding.tvTotalAttandee.text =
                                                    attandeeList.size.toString()

                                            }
                                            eventSPrintAdapter.notifyDataSetChanged()
                                        }



                                    } catch (e:Exception){
                                        e.printStackTrace()
                                    }
                                }
                            }


                        } catch (e: Exception) {
                            Log.d("exception", e.toString())
                        }
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
            }
        }

        return super.onKeyDown(keyCode, event)
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


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getSprint(){
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            Snackbar.make(binding.root,"No Internet", Snackbar.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        } else{
//            progressDialog = ProgressDialog(this)
//            progressDialog.setMessage("Please wait...")
//            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
//            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().getSprintData().enqueue(object :
            Callback<SprintDataModel> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<SprintDataModel>, response: Response<SprintDataModel>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                    // mList.add(RaceTypeDataModel.RaceTypeDataModelItem("iiiiiii", 0, "Choose RaceType"))
                    response.body()?.forEach {
                        mList.add(SprintDataModel.SprintDataModelItem(it.raceEventId,it.sprintName))
                    }

                    sprintAdater  = SprintAdapter(this@HundredMeterRaceActivity,mList as ArrayList<SprintDataModel.SprintDataModelItem>)
                    binding.spType.adapter = sprintAdater

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

            override fun onFailure(call: Call<SprintDataModel>, t: Throwable) {
                Toast.makeText(this@HundredMeterRaceActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
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
                    createSprintList.clear()
                    postCreateSprintList.clear()
                    eventSPrintAdapter.clear()
                    val twoPointSevenFiveAdapter = EventSPrintAdapter(attandeeList)
                    binding.listItem.adapter = twoPointSevenFiveAdapter
                    //eventSPrintAdapter.notifyDataSetChanged()
                    Toast.makeText(this@HundredMeterRaceActivity,response.body().toString(),Toast.LENGTH_SHORT).show()


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
                Toast.makeText(this@HundredMeterRaceActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTime(): String {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss a")
        return currentTime.format(formatter)
    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        soundPool!!.release()
        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
        isCountDownTimer = false
        binding.spType.isEnabled = true
        textToSpeech?.stop()
        val blinkingDot = binding.blinkingDot
        blinkingDot.clearAnimation()
        blinkingDot.visibility = View.GONE

        runOnUiThread(Runnable {
        binding.btnStartAttendance.text = Cons.STARTATTENDANCE
        binding.btnStartAttendance.setBackgroundColor(ContextCompat.getColor(this, R.color.forest_green))
        binding.btnStartAttendance.setTextColor(Color.WHITE)
        })



    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.setSpeechRate(1.0f)
            } else {
                Log.d("TextToSpeech","initialization failed")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun countDownTimer() {
        val countdownList = listOf("one", "two", "three", "go")
        val delayMillis = 1000L

        for ((index, countdown) in countdownList.withIndex()) {
            countdownHandler.postDelayed({
                textToSpeech?.speak(countdown, TextToSpeech.QUEUE_FLUSH, null, countdown)

                // Delay for a short period before starting the next countdown
                val nextCountdownDelay = 500L
                countdownHandler.postDelayed({
                    if (index == countdownList.lastIndex) {
                        onGoSpeechCompleted()
                    }
                }, nextCountdownDelay)
            }, index * delayMillis)
        }
    }

//  @RequiresApi(Build.VERSION_CODES.O)
//    private fun countDownTimer() {
//        val countdownList = listOf("one", "two", "three", "go")
//        val delayMillis = 1000L
//
//        for ((index, countdown) in countdownList.withIndex()) {
//            countdownHandler.postDelayed({
//                textToSpeech?.speak(countdown, TextToSpeech.QUEUE_FLUSH, null, countdown)
//                if (index == countdownList.lastIndex) {
//                    onGoSpeechCompleted()
//                }
//            }, index * delayMillis)
//        }
//    }
    inner class CountdownHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Toast.makeText(this@HundredMeterRaceActivity,msg.toString(),Toast.LENGTH_SHORT).show()

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun onGoSpeechCompleted() {
        // startSearching()

        val newStartTime = getCurrentTime()
        for (i in attandeeList.indices) {
            val currentItem = attandeeList[i]
            if (currentItem.startTime.isNullOrBlank()) {
                // Update the startTime only if it's null or an empty string
                val newItem = currentItem.copy(startTime = newStartTime)
                attandeeList[i] = newItem
                Log.d("current", newItem.toString())
                val currentDateTime = LocalDateTime.now()
                localeDateTimeShowing = formatLocalDateTime(currentDateTime)

            }



        }


        val newStartTime2 = formatLocalDateTime(LocalDateTime.now())
        for (i in postCreateSprintList.indices) {
            val currentItem = postCreateSprintList[i]
            if (currentItem.starttime.isNullOrBlank()) {
                // Update the startTime only if it's null or an empty string
                val newItem = currentItem.copy(starttime = newStartTime2)
                postCreateSprintList[i] = newItem
                localeDateTimeShowing = LocalDateTime.now().toString()
                val currentTime = LocalDateTime.now()
                localeDateTimePosting = formatLocalDateTime(currentTime)
                Log.d("localeDateTimePosting", localeDateTimePosting)

                runOnUiThread {
                    binding.listItem.postDelayed({
                        binding.listItem.scrollToPosition(postCreateSprintList.size)
                        binding.tvTotalAttandee.text = postCreateSprintList.size.toString()
                    }, 100)
                }
            }
        }
        val twoPointSevenFiveAdapter = EventSPrintAdapter(attandeeList)
        binding.listItem.adapter = twoPointSevenFiveAdapter
        Log.d("postCreateSprintList",postCreateSprintList.toString())

        eventSPrintAdapter.notifyDataSetChanged()
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


                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RaceRegsModel>, t: Throwable) {
                Toast.makeText(this@HundredMeterRaceActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }



    override fun onBackPressed() {
        val dialogView = layoutInflater.inflate(R.layout.quite_dialog, null)
        val builder = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()

        dialogView.btn_yes.setOnClickListener {
            if (isInventoryRunning || isCountDownTimer) {
                stopSearching()


            } else {
                textToSpeech?.stop()
                textToSpeech?.shutdown()
                textToSpeech = null
                timer?.cancel()
                countdownHandler.removeCallbacksAndMessages(null)
                super.onBackPressed() // Allow the default back behavior if not searching or in a countdown timer.
            }
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            dialog.dismiss()

        }

        dialogView.btn_no.setOnClickListener {
            dialog.dismiss()
            // Handle any other action you want
        }

        dialog.show()

    }


    private  fun handleInventoryData(var1: SpdInventoryData) {
        try {


            val timeMillis = System.currentTimeMillis()
            val l: Long = timeMillis - lastTimeMillis
            if (l < 100) {
                return
            }
            lastTimeMillis = System.currentTimeMillis()
            soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)
            rfidNo = var1.getEpc().substring(0,4)
            if (rfidNo!=null) {
                if (!tempList.contains(rfidNo)) {
                    tempList.add(rfidNo)
                    try {
                        createSprintList.add(
                            CreateSprintResultModelItem(
                                "Not Available",
                                sSprintEventId,
                                rfidNo,
                                "Not Available",
                                sSprintEventId,
                                ""
                            )
                        )

                       // rfidNo = var1.getEpc().substring(0,4)


                        val getAttandeeDetails =
                            TempAttandeeList.find { it.chestNumber == rfidNo }
                        val inputDate = getAttandeeDetails?.dob
                        val outputDate =
                            inputDate?.let { convertDateFormat(it) }
                        attandeeList.add(
                            RaceRegsModel.RaceRegsModelItem(
                                getAttandeeDetails!!.active,
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
                        postCreateSprintList.add(
                            CreateSprintResultModelItem(
                                getAttandeeDetails.armyNumber.toString(),
                                sSprintEventId,
                                rfidNo,
                                getAttandeeDetails.soldierType.toString(),
                                sSprintEventId,
                                ""
                            )
                        )
                        runOnUiThread {

                            binding.listItem.postDelayed({
                                // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                binding.listItem.scrollToPosition(attandeeList.size)
                            }, 100)

                            binding.tvTotalAttandee.text =
                                attandeeList.size.toString()
                            eventSPrintAdapter.notifyDataSetChanged()

                        }



                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
}catch (e:Exception){
    e.printStackTrace()
}
}
}