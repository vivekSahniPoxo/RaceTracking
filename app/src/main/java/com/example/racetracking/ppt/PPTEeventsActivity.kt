package com.example.racetracking.ppt

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.R
import com.example.racetracking.bpet.adapter.TwoPointSevenFiveAdapter
import com.example.racetracking.bpet.adapter.UpdateMidPointAdapter
import com.example.racetracking.bpet.datamodel.*
import com.example.racetracking.crosscountry.AllCrossCountryEventsActivity
import com.example.racetracking.databinding.ActivityPpteeventsBinding
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class PPTEeventsActivity : AppCompatActivity() {
    val processedGroupSet= mutableSetOf<Pair<String, Int>>()
    lateinit var binding:ActivityPpteeventsBinding
    var selected = ""
    var rfidNo = ""
    private var soundId = 0
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var  handler: Handler
    lateinit var rfidList:ArrayList<Rfid>
    lateinit var tempList:ArrayList<String>
    lateinit var statusList:ArrayList<String>
    lateinit var pptEvents:TwoPointSevenFiveAdapter
    lateinit var progressDialog: ProgressDialog
    lateinit var dialog:Dialog
    private var isDialogShowing = false
    lateinit var submitItem:kotlin.collections.ArrayList<SubmitPPTEvent>
    var current = arrayListOf<String>()
  var totalAttend = arrayListOf<String>()
    var currentBatch = 0

    private val eventsViewModel: RaceTypeViewMode by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPpteeventsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rfidList = arrayListOf()
        tempList = arrayListOf()
        submitItem = arrayListOf()
        handler = Handler()
        statusList = arrayListOf()
        pptEvents = TwoPointSevenFiveAdapter(rfidList)
        iuhfService = UHFManager.getUHFService(this)
        iuhfService.antennaPower = 30
        statusList()
//       dataBase = EventDataBase.getDatabase(this@PPTEeventsActivity)
//        eventDao = dataBase.EventDao()


        binding.imBack.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, PActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                val child = binding.listOfItem.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    val position = binding.listOfItem.getChildAdapterPosition(child)
                    showYesNoDialog(position)
                    Log.d("position", position.toString())
                    // 'position' now contains the position of the long-pressed item
                    // Use it as needed
                }
            }
        })

        binding.listOfItem.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(e)
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        })

        val currentDateTime = LocalDateTime.now()
        val customPattern = "MMM dd, yyyy"
        val formatter = DateTimeFormatter.ofPattern(customPattern, Locale.ENGLISH)
        val formattedDate = currentDateTime.format(formatter)
        binding.tvCurrentDateTime.text = formattedDate

        val receivedIntent = intent
        val receivedBundle = receivedIntent.extras
        if (receivedBundle!=null){
            binding.tvEventNAme.text = receivedBundle.getString(Cons.GETEVENTFROMAPI)
            binding.tvToolBarTitle.text = receivedBundle.getString(Cons.GETEVENTFROMAPI)
        }


        binding.btnSubmit.setOnClickListener {
            if (current.isNotEmpty()) {
                if (isInventoryRunning == true) {
                    stopSearching()
                }
                val selectedPosition = binding.spType.selectedItemPosition
                if (selectedPosition == 0) {
                    Toast.makeText(this, "Please select status", Toast.LENGTH_SHORT).show()
                } else if (submitItem.isNotEmpty()) {

                    submitItem.forEach {
                        eventsViewModel.addPPTEventSubmit(
                            SubmitPPTEventLDB(
                                0,
                                it.id,
                                it.eventId,
                                it.chestNo,
                                it.status,
                                it.date,
                                receivedBundle?.getString(Cons.GETEVENTFROMAPI).toString()
                            )
                        )
                        //submitItem.add(SubmitPPTEvent(0,it.eventId,it.chestNo,it.status,it.date))
                        //submitEvent(submitItem)
                       // getSubmittedPPTChestNumber()
                    }
                    currentBatch++
                    binding.tvCurrentBatch.text = currentBatch.toString()
                    submitItem.clear()
//                    tempList.clear()
                    rfidList.clear()
                    current.clear()
                    binding.tvCurrentCount.text = ""
                    //binding.count.text = ""
                    val adapter = TwoPointSevenFiveAdapter(rfidList)
                    binding.listOfItem.adapter = adapter


                    adapter.clearData()

                    //twoPointSevenFiveAdapter.notifyDataSetChanged()
                }
            }else{
                Toast.makeText(applicationContext,"No Data Found",Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun statusList(){
        statusList.add("Please select status")
        statusList.add("Super Excellent")
        statusList.add("Excellent")
        statusList.add("Good")
        statusList.add("Satisfied")
        statusList.add("Poor")
        statusList.add("Fail")
        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, androidx.appcompat.R.layout.select_dialog_item_material,statusList ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView =
                    super.getDropDownView(position, convertView, parent) as TextView

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
                if (parent.getItemAtPosition(position).toString() != "Please select Status") {

                    val selectedPosition = binding.spType.selectedItemPosition
                    if (selectedPosition != AdapterView.INVALID_POSITION) {
                        selected = binding.spType.getItemAtPosition(selectedPosition).toString()
                        //if (tempList.isNotEmpty()){
                          tempList.clear()
                        getSubmittedPPTChestNumber()
                       // }

                        Log.d("selected",selected)
                    }
                    //Toast.makeText(this@LoginActivity, "position" + binding.spType.getItemIdAtPosition(position), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode==131) {
        val selectedPosition = binding.spType.selectedItemPosition
        if (selectedPosition==0){
            Snackbar.make(binding.root,"Please select status",Snackbar.LENGTH_SHORT).show()
        } else
        if (!isInventoryRunning) {
            val dataBase = EventDataBase.getDatabase(this@PPTEeventsActivity)
            val eventDao = dataBase.EventDao()
            startSearching()

            // Start inventory service
            iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                @RequiresApi(Build.VERSION_CODES.O)
                @SuppressLint("NotifyDataSetChanged")
                override fun getInventoryData(var1: SpdInventoryData) {
                    try {


                       rfidNo = var1.getEpc().substring(0,4)
                        //rfidNo = var1.getEpc()
                       // Log.d("RFEPC", rfidNo)
                        if (rfidNo!=null) {
                            if (!tempList.contains(rfidNo)) {
                               // Log.d("RFEPC2", rfidNo)
                                  getDataItemPPT(rfidNo)

                            }
                        }


                    } catch (e: Exception) {
                       e.printStackTrace()
                    }


                    try{
                    val timeMillis = System.currentTimeMillis()
                    val l: Long = timeMillis - lastTimeMillis
                    if (l < 100) {
                        return
                    }
                    lastTimeMillis = System.currentTimeMillis()
                    soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)
                    } catch (e:Exception){
                        Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val formattedDate = localDateTime.atOffset(ZoneOffset.UTC).format(formatter)
        return formattedDate
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
        runOnUiThread(kotlinx.coroutines.Runnable {
            val blinkingDot = binding.blinkingDot
            blinkingDot.clearAnimation()
            blinkingDot.visibility = View.GONE
        })
    }


    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true
        initSoundPool()

        runOnUiThread(kotlinx.coroutines.Runnable {
            binding.blinkingDot.isVisible = true

            val blinkAnimation = AlphaAnimation(1.0f, 0.0f)
            blinkAnimation.duration = 500 // Adjust the duration as needed
            blinkAnimation.repeatMode = Animation.REVERSE
            blinkAnimation.repeatCount = Animation.INFINITE

            // Start the animation
            binding.blinkingDot.startAnimation(blinkAnimation)


            val pptEvents = TwoPointSevenFiveAdapter(rfidList)
            binding.listOfItem.adapter = pptEvents

        })

        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
           //iuhfService.antennaPower = 30
            iuhfService.inventoryStart()


        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }


    }

//    override fun onBackPressed() {
//        //  stopInventoryService()
//        val intent = Intent(this, PActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//       // iuhfService.closeDev()
//       // finish()
//    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun submitEvent(submitEvent: ArrayList<SubmitPPTEvent>){
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

        RetrofitClient.getResponseFromApi().createPPTEventResult(submitEvent).enqueue(object :
            Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                    submitItem.clear()
                    tempList.clear()
                   // rfidList.clear()
                    val adapter = TwoPointSevenFiveAdapter(rfidList)
                    binding.listOfItem.adapter = adapter
                    adapter.clearData()
                    //twoPointSevenFiveAdapter.notifyDataSetChanged()
                    Toast.makeText(this@PPTEeventsActivity,response.body().toString(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@PPTEeventsActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
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
                            binding.listOfItem.postDelayed({
                                // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                binding.listOfItem.scrollToPosition(rfidList.size)
                            }, 100)


                        }
                        //twoPointSevenFiveAdapter.notifyItemRemoved(indexToRemove)
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
        if (isInventoryRunning) {
            stopSearching()
        }
        val dialogView = layoutInflater.inflate(R.layout.quite_dialog, null)
        val builder = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()

        dialogView.btn_yes.setOnClickListener {
            if (isInventoryRunning) {
                stopSearching()
            }
            val intent = Intent(this, PActivity::class.java)
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


    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDataItemPPT(RfidNo:String){

        val receivedIntent = intent
        val receivedBundle = receivedIntent.extras
        val eventID = receivedBundle?.getInt(Cons.EVentID)
        try {
          //  val getChestNumber = eventDao.getPPTEventDetails(rfidNo, eventID!!)
            val groupKey = Pair(RfidNo, eventID)
            val isSameGroup = processedGroupSet.contains(groupKey)
            if (!isSameGroup) {
                rfidList.add(Rfid(tempList.size, rfidNo, ""))
                current.add(rfidNo)
                totalAttend.add(rfidNo)

                val localDateTime = LocalDateTime.now()
                val formattedDate2 = formatLocalDateTime(localDateTime)

                    submitItem.add(SubmitPPTEvent(
                        0,
                        receivedBundle!!.getInt(Cons.EVentID),
                        rfidNo,
                        selected,
                        formattedDate2
                    )
                    )
               // }
                runOnUiThread {
                    binding.listOfItem.scrollToPosition(current.size)

                    binding.count.text = totalAttend.size.toString()
                    binding.tvCurrentCount.text = current.size.toString()
                   // binding.listOfItem.adapter = pptEvents

                }
                tempList.add(rfidNo)
               // pptEvents.notifyDataSetChanged()

            }
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        getSubmittedPPTChestNumber()
    }

    fun getSubmittedPPTChestNumber(){
        val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                database.EventDao().getAllRaceTypePPTEventDetails()
            }
            data.forEach { item->
                Log.d("items",item.toString())
                val groupKey = Pair(item.chestNo, item.eventId)
                processedGroupSet.add(groupKey)


            }
        }
    }
}