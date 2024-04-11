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
import androidx.core.view.isVisible
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
import com.example.racetracking.bpet.datamodel.AttandeeDetails
import com.example.racetracking.race_type.attandees_details.AttandeesDetailsActivity
import kotlinx.android.synthetic.main.activity_race_type.*


class RaceTypeActivity : AppCompatActivity(),TextToSpeech.OnInitListener,onItemPositionListenr {
    lateinit var binding:ActivityRaceTypeBinding
    lateinit var progressDialog: ProgressDialog
    lateinit var raceTypeAdapter: RaceTypeAdapter
    var isCountDownTimer = false
    private var isDialogShowing = false
    lateinit var mList: ArrayList<RaceTypeDataModel.RaceTypeDataModelItem>
    lateinit var attandeeList:kotlin.collections.ArrayList<RaceRegsModel.RaceRegsModelItem>
    lateinit var TempAttandeeList:kotlin.collections.ArrayList<RaceRegsModel.RaceRegsModelItem>
    lateinit var dialog:Dialog

    private val countdownNumbers = arrayOf("One", "Two", "Three", "Go")
    private var currentCount = 0
    private val countdownHandler = CountdownHandler()
    var localeDateTime = ""
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
        iuhfService = UHFManager.getUHFService(this)
        textToSpeech = TextToSpeech(this, this)
        dialog = Dialog(this)
        TempAttandeeList = arrayListOf()
        localBDList = arrayListOf()

       // val database = Room.databaseBuilder(this, EventDataBase::class.java, "EventDataBase").build()
        eventAttandeeAdapter = EventAttandeeAdapter(attandeeList,this)
        initializeTextToSpeech()

        binding.cardBpet2.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, AttandeesDetailsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        mList.add(RaceTypeDataModel.RaceTypeDataModelItem("knck", 0, "Choose RaceType"))
        raceTypeAdapter = RaceTypeAdapter(this,mList)
        getRaceType()
        getAttandies()
        //countDownTimer()
       // val database2 = EventDataBase.getDatabase(applicationContext)

        binding.btnStart.setOnClickListener {

            if (attandeeList.isNotEmpty()) {
                // if (binding.btnStart.text=="Start") {
                //  binding.btnStart.text = "Stop"

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

//        else if (binding.btnStart.text=="Stop"){
//                binding.btnStart.text = "Start"
//            }


        }

        binding.imBack.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }


        binding.btnSave.setOnClickListener {

            binding.btnStart.isEnabled = true
            binding.spType.isEnabled = true
            binding.btnStart.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#164B60"))
            binding.btnStart.setTextColor(Color.WHITE)

            if (isInventoryRunning==true){
                stopSearching()
            }

              //  GlobalScope.launch(Dispatchers.Main) {
                    try {
                        for (getData in attandeeList) {
                            val chestNumber = getData.chestNumber.toString()
                            val soldierType = getData.soldierType.toString()
                           // val startTime = getData.startTime.toString()

                            val midPoint = getData.midPoint ?: "2023-10-26T13:23:35.772Z"
                            // Handle null midPoint
                            val armyNumber = getData.armyNumber.toString()
                           // val raceResultID = getData.raceResultMasterId ?: 0
                            //GlobalScope.launch {
//                            val inputTime = getData.startTime.toString()
//
//                            // Create a SimpleDateFormat for parsing the input time format
//                            val inputTimeFormat = SimpleDateFormat("hh:mm:ss a")
//
//                            // Parse the input time
//                            val date = inputTimeFormat.parse(inputTime)
//
//                            // Create a SimpleDateFormat for formatting the output in ISO 8601 format
//                            val outputTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//
//                            // Format the date in ISO 8601 format
//                            val formattedTime = outputTimeFormat.format(date)
                            RaceTypeViewModel.addAttandeDetails(AttandeeData(0,sRaceTypeId, armyNumber, chestNumber, soldierType, sRaceTypeId, localeDateTime , midPoint))

                            RaceTypeViewModel.addTempattanDetails(AttandeeDetails(0,armyNumber,chestNumber,getData.companyvalue.toString(),getData.distance.toString(),getData.dob.toString(),getData.gender.toString(),getData.name.toString(),getData.raceTypeValue.toString(),getData.soldierType.toString(),getData.startTime.toString(),getData.unitValue.toString(),getData.posting.toString()))
                            Log.d("masterID",localeDateTime.toString())
                            localBDList.add(getData.chestNumber.toString())
                            binding.spType.isEnabled = true
                            if (binding.btnStart.text=="Stop"){
                                binding.btnStart.text = "Start"
                            }
                               // }

                        }
//                        runOnUiThread {
//                            attandeeList.clear()
//                            eventAttandeeAdapter.notifyDataSetChanged()
//                        }
                    } catch (e: Exception) {
                        Log.d("exception", e.toString())
                    }
                //}


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

        }






        binding.listItem.adapter = eventAttandeeAdapter

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mList.map { it.raceType })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spType.adapter = raceTypeAdapter
        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val department = mList[position]
                sRaceTypeId = department.id


                try {
                    Toast.makeText(this@RaceTypeActivity, sRaceTypeId.toString(), Toast.LENGTH_SHORT).show()
                } catch (e:Exception){

                }
                // Do something with departmentId
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
               Log.d("TextToSpeech","initialization failed")
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
                Log.d("Go","1")
            }
            "2" -> {
                speakText("Command 2")
                Log.d("Go","2")
            }
            "3" -> {
                speakText("Command 3")
                Log.d("Go","3")
            }
            "go" -> {
                speakText("Go command")
                Log.d("Go","Go")
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
        if (!isInventoryRunning && !isCountDownTimer) {

            if (binding.spType.selectedItemPosition==0){
                Snackbar.make(binding.root,"Please select race type",Snackbar.LENGTH_SHORT).show()
            } else {
                startSearching()
            }

           // startSearching()

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
                        Log.d("RFFFF", var1.getEpc())
                        rfidNo = var1.getEpc()
                        if (rfidNo!=null) {
                            if (!tempList.contains(rfidNo)) {
                                tempList.add(rfidNo)

                                val getAttandeeDetails = TempAttandeeList.find { it.chestNumber == rfidNo }
                                val inputDate = getAttandeeDetails?.dob
                                val outputDate = inputDate?.let { convertDateFormat(it) }
                                attandeeList.add(RaceRegsModel.RaceRegsModelItem(getAttandeeDetails!!.active,getAttandeeDetails.ageGroupMaster,getAttandeeDetails.ageGroupValue,getAttandeeDetails.armyNumber,getAttandeeDetails.chestNumber,getAttandeeDetails.company,getAttandeeDetails.companyvalue,getAttandeeDetails.distance,outputDate,getAttandeeDetails.endTime,getAttandeeDetails.gender,getAttandeeDetails.marks,getAttandeeDetails.midPoint,getAttandeeDetails.name,getAttandeeDetails.posting,getAttandeeDetails.raceResultMasterId,getAttandeeDetails.raceTypeMaster,getAttandeeDetails.companyvalue,getAttandeeDetails.rank,getAttandeeDetails.rankValue,getAttandeeDetails.registrationId,getAttandeeDetails.resultCategory,getAttandeeDetails.soldierType,"",getAttandeeDetails.unit,getAttandeeDetails.unitValue))
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
            //isCountDownTimer = true
        }

        }

//        else{
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                finish()
//            }
//        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun countDownTimer() {
        val countdownList = listOf("one", "two", "three", "go")
        val delayMillis = 1000L

        for ((index, countdown) in countdownList.withIndex()) {
            countdownHandler.postDelayed({
                textToSpeech?.speak(countdown, TextToSpeech.QUEUE_FLUSH, null, countdown)
                if (index == countdownList.lastIndex) {

                    onGoSpeechCompleted()
                }
            }, index * delayMillis)
        }
    }
    inner class CountdownHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Toast.makeText(this@RaceTypeActivity,msg.toString(),Toast.LENGTH_SHORT).show()
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
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30
            eventAttandeeAdapter = EventAttandeeAdapter(attandeeList,this)
            binding.listItem.adapter = eventAttandeeAdapter
            binding.spType.isEnabled = false
        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }
        // binding.btnStart.text = "Stop"
        iuhfService.inventoryStart()
    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        soundPool!!.release()
        //binding.btnStart.text = "Start"
       // isCountDownTimer = false
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
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss a")
        return currentTime.format(formatter)
    }


    @SuppressLint("SetTextI18n")
    private fun showYesNoDialog(position: Int) {
        if (!isDialogShowing) { // Check if the dialog is not already showing
            isDialogShowing = true // Set the flag to true to indicate that the dialog is being displayed

            dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.remove_item_dialog)
            dialog.setCancelable(true)
            dialog.show()

            val cancel: MaterialButton = dialog.findViewById(R.id.btnNo)
            cancel.setOnClickListener {
                dialog.dismiss()
                isDialogShowing = false // Set the flag to false to indicate that the dialog is dismissed
            }

            val yes: MaterialButton = dialog.findViewById(R.id.btnYEs)
            yes.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({

                    val indexToRemove = position
                    val indexForBatchList = position

                    if (indexToRemove >= 0 && indexToRemove < totalAttandeEList.size) {
                        totalAttandeEList.removeAt(indexToRemove)
                        binding.tvTotalAttandee.text = totalAttandeEList.size.toString()
                    } else {
                        binding.tvTotalAttandee.text = totalAttandeEList.size.toString()
                    }

                    if (indexForBatchList >= 0 && indexForBatchList <  totalBatcNoList.size) {
                        totalBatcNoList.removeAt(indexForBatchList)
                        binding.totalBatchNo.text =  totalBatcNoList.size.toString()
                    } else {
                        binding.totalBatchNo.text =  totalBatcNoList.size.toString()
                    }
                    val popItemAnimator = PopItemAnimator()
                    binding.listItem.itemAnimator = popItemAnimator
                    val positionToDelete = position // Replace with the position you want to delete
                    attandeeList.removeAt(positionToDelete) // Remove the item from your dataset
                    eventAttandeeAdapter.notifyItemRemoved(positionToDelete)
                    isDialogShowing = false // Set the flag to false to indicate that the dialog is dismissed
                }, 200) // Adjust the delay time as needed
                dialog.dismiss()
            }
        }
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
        Log.d("itememe",item.name.toString())
        Toast.makeText(this,item.posting.toString(),Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()
        GlobalScope.launch {
            val data = database.EventDao().getAllAttandees()
            if (data.isNotEmpty()) {
                database.EventDao().deleteAttandeeDetailsTemp()
            }
        }
    }




}