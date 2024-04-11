package com.example.racetracking.bpet

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.racetracking.R
import com.example.racetracking.bpet.adapter.TwoPointSevenFiveAdapter
import com.example.racetracking.bpet.datamodel.*
import com.example.racetracking.databinding.ActivitySeventyFiveMeterRaceBinding
import com.example.racetracking.localdatabase.EventDao
import com.example.racetracking.localdatabase.EventDataBase
import com.example.racetracking.race_type.viewModel.RaceTypeViewMode
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.utils.App
import com.example.racetracking.utils.Cons
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import kotlinx.android.synthetic.main.quite_dialog.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class BPETRACE : AppCompatActivity() {
    val processedGroupSet = mutableSetOf<Pair<String, Int>>()
    lateinit var binding:ActivitySeventyFiveMeterRaceBinding
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var  handler: Handler
    lateinit var rfidList:ArrayList<Rfid>
    lateinit var tempList:ArrayList<String>
    private val eventsViewModel: RaceTypeViewMode by viewModels()
    private lateinit var eventDao: EventDao


    lateinit var submitItem:kotlin.collections.ArrayList<SubmitEvent>
     var isPassed = ""
    var rfidNo = ""
    var passPrFail  = 0
    private var soundId = 0
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0

    lateinit var progressDialog: ProgressDialog
    lateinit var dialog:Dialog
    private var isDialogShowing = false
    lateinit var twoPointSevenFiveAdapter:TwoPointSevenFiveAdapter
    var pssList = arrayListOf<String>()
    var failList =  arrayListOf<String>()
    var fail = arrayListOf<String>()
    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeventyFiveMeterRaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rfidList = arrayListOf()
        tempList = arrayListOf()
        submitItem = arrayListOf()
        handler = Handler()
        val database = Room.databaseBuilder(this, EventDataBase::class.java, "EventDataBase").build()
        eventDao = EventDataBase.getDatabase(applicationContext).EventDao()
        val allData = eventDao.getAllData()
        if (allData.isNotEmpty()) {
            allData.forEach {
                val groupKey = Pair(it.chestNumber,it.id)
                processedGroupSet.add(groupKey)

            }
        }



        twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
        iuhfService = UHFManager.getUHFService(this)
        val currentDateTime = LocalDateTime.now()
        val customPattern = "MMM dd, yyyy"
        val formatter = DateTimeFormatter.ofPattern(customPattern, Locale.ENGLISH)

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                val child = binding.listOfItemRecyclerView.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    val position = binding.listOfItemRecyclerView.getChildAdapterPosition(child)
                    showYesNoDialog(position)
                    Log.d("position", position.toString())
                    // 'position' now contains the position of the long-pressed item
                    // Use it as needed
                }
            }
        })

        binding.listOfItemRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(e)
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        })







        binding.btnSubmit.setOnClickListener {
            if (isInventoryRunning==true){
                stopSearching()
            }

            if(passPrFail!=0) {
               // submitEvent(submitItem)
//                GlobalScope.launch {
                val receivedIntent = intent
                val receivedBundle = receivedIntent.extras
                if (receivedBundle!=null) {

                    val getEventName = receivedBundle.getString(Cons.GETEVENTFROMAPI)
                    submitItem.forEach {
                        eventsViewModel.addBPETEventsDetails(
                            SubmitEventInRoomDB(
                                0,
                                it.eventId,
                                it.chestNo,
                                passPrFail,
                                currentDateTime.toString(),
                                getEventName.toString()
                            )
                        )


                            val currentDateTime = LocalDateTime.now()

                            val getEventName = receivedBundle.getString(Cons.GETEVENTFROMAPI)
                            if(getEventName==Cons.twoPointSevenFiveMtrDitch){
                                if (passPrFail==1){
                                    pssList.add(it.chestNo)
                                    eventsViewModel.twoPointSevenMeterTurning(onlytwoPointSevenFiveMtrTemp(0,it.chestNo,"Pass"))
                                } else{
                                    failList.add(it.chestNo)
                                    eventsViewModel.twoPointSevenMeterTurning(onlytwoPointSevenFiveMtrTemp(0,it.chestNo,"Fail"))
                                }
                            } else if (getEventName==Cons.VRope){
                                if (passPrFail==1){
                                    pssList.add(it.chestNo)
                                    eventsViewModel.tempVRope(VRope(0,it.chestNo,"Pass"))
                                } else{
                                    failList.add(it.chestNo)
                                    eventsViewModel.tempVRope(VRope(0,it.chestNo,"Fail"))
                                }
                            } else if (getEventName==Cons.HRope){
                                if (passPrFail==1){
                                    pssList.add(it.chestNo)
                                    eventsViewModel.tempHRope(HRope(0,it.chestNo,"Pass"))

                                } else{
                                    failList.add(it.chestNo)
                                    eventsViewModel.tempHRope(HRope(0,it.chestNo,"Fail"))
                                }
                            }



                    }
                }
               // }

                submitItem.clear()
                tempList.clear()
                rfidList.clear()
                binding.count.text = ""
                binding.tvPass.text = pssList.size.toString()
                binding.tvFail.text = failList.size.toString()

                val adapter = TwoPointSevenFiveAdapter(rfidList)
                binding.listOfItemRecyclerView.adapter = adapter

                adapter.clearData()
                twoPointSevenFiveAdapter.clearData()
                twoPointSevenFiveAdapter.notifyDataSetChanged()
            } else{
                Snackbar.make(binding.root,"Please select pass or fail",Snackbar.LENGTH_SHORT).show()
            }
        }

        // Format the date using the custom pattern
        val formattedDate = currentDateTime.format(formatter)
        binding.tvCurrentDateTime.text = formattedDate

        val receivedIntent = intent
        val receivedBundle = receivedIntent.extras
        if (receivedBundle!=null){
            binding.tvEventNAme.text = receivedBundle.getString(Cons.GETEVENTFROMAPI)
            binding.tvToolBarTitle.text = receivedBundle.getString(Cons.GETEVENTFROMAPI)
        }

        binding.imBack.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, BPETActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }

        binding.btnPass.setOnClickListener {
            if (receivedBundle != null) {
                val message = receivedBundle.getInt(Cons.EVentID)
                isPassed = "1"
                passPrFail = 1
//                val updateValue = rfidList.find { it.rfid == rfidNo }
//                updateValue?.isPassed = "1"


               // val itemIdToUpdate = rfidNo // Replace with the actual ID
                val newValue = "1" // Replace with the new value

                runOnUiThread {
                    val twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
                    binding.listOfItemRecyclerView.adapter = twoPointSevenFiveAdapter
                    twoPointSevenFiveAdapter.updateAllStatus(newValue)

                    val itemPosition = twoPointSevenFiveAdapter.getPositionOfItem(newValue)
                    if (itemPosition != RecyclerView.NO_POSITION) {
                        twoPointSevenFiveAdapter.notifyItemChanged(itemPosition)
                    }
                    twoPointSevenFiveAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Passed", Toast.LENGTH_SHORT).show()
                }

                val receivedIntent = intent
                val receivedBundle = receivedIntent.extras
                if (receivedBundle!=null) {
                    val currentDateTime = LocalDateTime.now()

                    val getEventName = receivedBundle.getString(Cons.GETEVENTFROMAPI)
                    if(getEventName==Cons.twoPointSevenFiveMtrDitch){
                        if (passPrFail==1){
                           // eventsViewModel.twoPointSevenMeterTurning(onlytwoPointSevenFiveMtrTemp(0,"Pass"))
                        } else{
                           // eventsViewModel.twoPointSevenMeterTurning(onlytwoPointSevenFiveMtrTemp(0,"Fail"))
                        }
                    } else if (getEventName==Cons.VRope){
                        if (passPrFail==1){
                           // eventsViewModel.tempVRope(VRope(0,"Pass"))
                        } else{
                           // eventsViewModel.tempVRope(VRope(0,"Fail"))
                        }
                    }

                    GlobalScope.launch(Dispatchers.IO) {
                        rfidList.forEach {
                            val entity = SubmitEventInRoomDB(
                                0,
                                message,
                                it.rfid,
                                1,
                                currentDateTime.toString(),
                                getEventName.toString()
                            )
                            database.EventDao().addEvents(entity)
                        }
                    }
                }
                val status = 1 // Replace with the new isPassed value

                submitItem.forEach { item ->
                    item.isPassed = status
                }

            }
        }



        binding.btnFail.setOnClickListener {

            if (receivedBundle != null) {
                val message = receivedBundle.getInt(Cons.EVentID)
                isPassed = "2"
                passPrFail = 2

                val newValue = "2" // Replace with the new value

                val status = 2 // Replace with the new isPassed value

                submitItem.forEach { item ->
                    item.isPassed = status
                }


                runOnUiThread {
                    val twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
                    binding.listOfItemRecyclerView.adapter = twoPointSevenFiveAdapter
                    twoPointSevenFiveAdapter.updateAllStatus( newValue)
                    twoPointSevenFiveAdapter.notifyDataSetChanged()
                    val itemPosition = twoPointSevenFiveAdapter.getPositionOfItem(newValue)
                    if (itemPosition != RecyclerView.NO_POSITION) {
                        twoPointSevenFiveAdapter.notifyItemChanged(itemPosition)
                    }
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }

            }


        }


    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode==131) {
        if (!isInventoryRunning) {
            val dataBase = EventDataBase.getDatabase(this@BPETRACE)
            val eventDao = dataBase.EventDao()
            startSearching()

            // Start inventory service
            iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                @RequiresApi(Build.VERSION_CODES.O)
                @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
                override fun getInventoryData(var1: SpdInventoryData) {
                    try {

                        rfidNo = var1.getEpc().substring(0,4)
                        Log.d("RFFFF", rfidNo)



                       // Toast.makeText(this@SeventyFiveMeterRace,rfidNo,Toast.LENGTH_SHORT).show()

                        if (rfidNo!=null) {
                            if (!tempList.contains(rfidNo)) {
                                tempList.add(rfidNo)

//                             rfidList.add(Rfid(tempList.size,var1.getEpc(),""))
                             //rfidNo = rfidNo
                                val receivedIntent = intent
                                val receivedBundle = receivedIntent.extras



                                try {
                                    if (receivedBundle != null) {
                                        val enventId = receivedBundle.getInt(Cons.EVentID)
                                    val getChestNumber = eventDao.getBPETEventDetailsByChestNumberAndEventId(rfidNo,enventId)
                                        if (getChestNumber.chestNo == rfidNo && getChestNumber.eventId == enventId) {
                                    }else{

                                        val currentDateTime = LocalDateTime.now()
                                        rfidList.add(Rfid(tempList.size, rfidNo, ""))
                                            submitItem.add(
                                                SubmitEvent(
                                                    0,
                                                    enventId,
                                                    rfidNo,
                                                    0,
                                                    currentDateTime.toString()
                                                )
                                            )
                                        }
                                    }
                                } catch (e:Exception){
                                    val currentDateTime = LocalDateTime.now()
                                    val enventId = receivedBundle?.getInt(Cons.EVentID)
                                    //val getEventName = receivedBundle.getString(Cons.eventName)


                                    rfidList.add(Rfid(tempList.size, rfidNo, ""))
                                    submitItem.add(
                                        SubmitEvent(
                                            0,
                                            enventId?:0,
                                            rfidNo,
                                            0,
                                            currentDateTime.toString()
                                        )
                                    )

                                }


                                             runOnUiThread {
                                                 binding.listOfItemRecyclerView.postDelayed({
                                                     // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                                     binding.listOfItemRecyclerView.scrollToPosition(
                                                         rfidList.size
                                                     )
                                                 }, 100)
                                                 binding.count.text = submitItem.size.toString()

                                             }


                                             twoPointSevenFiveAdapter.notifyDataSetChanged()



                            }
                        }


                    } catch (e: Exception) {
                        Log.d("exception", e.toString())
                    }
                    try{
                        val timeMillis = System.currentTimeMillis()
                        val l: Long = timeMillis - lastTimeMillis
                        if (l < 100) {
                            return
                        }
                        lastTimeMillis = System.currentTimeMillis()
                        soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)
                        // val RfidNo = var1.getEpc()
                    } catch (e:Exception){
                        e.printStackTrace()
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
        //else if(keyCode == KeyEvent.KEYCODE_BACK){
//            finish()
//
//        }
        return super.onKeyDown(keyCode, event)
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
        soundId = soundPool!!.load(this, R.raw.beep, 0)
    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode==131) {
//
//            if (isInventoryRunning == false) {
//                startSearching()
//
//                iuhfService.setOnReadListener { var1 ->
//                    val stringBuilder = StringBuilder()
//                    val epcData = var1.epcData
//                    val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
//                    if (!TextUtils.isEmpty(hexString)) {
//                        stringBuilder.append("EPC：").append(hexString).append("\n")
//                    } else {
//                        //Toast.makeText(this, "No Scan", Toast.LENGTH_SHORT).show()
//                    }
//                    if (var1.status == 0) {
//                        val readData = var1.readData
//                        val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
//                        stringBuilder.append("ReadData:").append(readHexString).append("\n")
//                        Toast.makeText(this, readHexString, Toast.LENGTH_SHORT).show()
//                         if (!tempList.contains(readHexString)) {
//                             tempList.add(readHexString)
//                             rfidList.add(Rfid(tempList.size,readHexString,""))
//                             rfidNo = readHexString
//
//
//
//                         }
//
//                        runOnUiThread {
////                            val twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
////                            binding.listOfItem.adapter = twoPointSevenFiveAdapter
//                            twoPointSevenFiveAdapter.notifyDataSetChanged()
//
//
//                        }
//
//
//                    } else {
//                        stringBuilder.append(this.resources.getString(R.string.read_fail))
//                            .append(":").append(ErrorStatus.getErrorStatus(this,var1.status)).append("\n")
//                    }
//                    handler.sendMessage(handler.obtainMessage(1, stringBuilder))
//
//                }
//                val readArea = iuhfService.readArea(1, 2, 6, "00000000")
//                if (readArea != 0) {
//                    val err: String =
//                        this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(this,readArea) + "\n"
//                    handler.sendMessage(handler.obtainMessage(1, err))
//
//                }
//               // val twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
////                binding.listOfItem.adapter = twoPointSevenFiveAdapter
////                twoPointSevenFiveAdapter.notifyDataSetChanged()
//                return true
//            } else {
//                stopSearching()
//            }
//        }
//        else {
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                // startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            }
//        }
//        return super.onKeyUp(keyCode, event)
//    }



    override fun onPause() {
        super.onPause()
        if (isInventoryRunning==true){
            stopSearching()

        }

        iuhfService.closeDev()
    }

    override fun onStop() {
        super.onStop()
        iuhfService.closeDev()
    }






    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
        val blinkingDot = binding.blinkingDot
        blinkingDot.clearAnimation()
        blinkingDot.visibility = View.GONE
    }


    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true
        initSoundPool()

        binding.blinkingDot.isVisible = true

        val blinkAnimation = AlphaAnimation(1.0f, 0.0f)
        blinkAnimation.duration = 500 // Adjust the duration as needed
        blinkAnimation.repeatMode = Animation.REVERSE
        blinkAnimation.repeatCount = Animation.INFINITE

        // Start the animation
        binding.blinkingDot.startAnimation(blinkAnimation)
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30

            val twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
            binding.listOfItemRecyclerView.adapter = twoPointSevenFiveAdapter

        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }

        iuhfService.inventoryStart()
    }

//    override fun onBackPressed() {
//        //  stopInventoryService()
//        val intent = Intent(this, BPETActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//        //iuhfService.closeDev()
//        //finish()
//    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun submitEvent(submitEvent: ArrayList<SubmitEvent>){
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

        RetrofitClient.getResponseFromApi().submitEvent(submitEvent).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                    submitItem.clear()
                    tempList.clear()
                    rfidList.clear()
                    val adapter = TwoPointSevenFiveAdapter(rfidList)
                    binding.listOfItemRecyclerView.adapter = adapter

                    adapter.clearData()
                    twoPointSevenFiveAdapter.clearData()
                    twoPointSevenFiveAdapter.notifyDataSetChanged()
                    Toast.makeText(this@BPETRACE,response.body().toString(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@BPETRACE,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
    }

  @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
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
                // Ensure the position is within the valid range
                val indexToRemove = position
                if (indexToRemove >= 0 && indexToRemove < rfidList.size) {
                    rfidList.removeAt(indexToRemove)
                    binding.count.text = rfidList.size.toString()
                    // Notify the adapter of the item removal
                    runOnUiThread {
                        binding.listOfItemRecyclerView.postDelayed({
                            // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                            binding.listOfItemRecyclerView.scrollToPosition(rfidList.size)
                        }, 100)


                    }
                    twoPointSevenFiveAdapter.notifyItemRemoved(indexToRemove)
                } else {
                    // Handle the case where the position is out of bounds
                    binding.count.text = rfidList.size.toString()
                }
                isDialogShowing = false
            }, 200)
            dialog.dismiss()
        }
    }
}



    override fun onBackPressed() {
        val dialogView = layoutInflater.inflate(R.layout.quite_dialog, null)
        val builder = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()

        dialogView.btn_yes.setOnClickListener {
            if (isInventoryRunning) {
                stopSearching()
            }
            val intent = Intent(this, BPETActivity::class.java)
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
}