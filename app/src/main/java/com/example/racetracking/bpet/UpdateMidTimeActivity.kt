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
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.R
import com.example.racetracking.bpet.adapter.TwoPointSevenFiveAdapter
import com.example.racetracking.bpet.adapter.UpdateMidPointAdapter
import com.example.racetracking.bpet.datamodel.Rfid
import com.example.racetracking.bpet.datamodel.SubmitEvent
import com.example.racetracking.bpet.datamodel.UpdateMidPoint
import com.example.racetracking.bpet.datamodel.UpdateMidTimeModelItem
import com.example.racetracking.databinding.ActivityUpdateMidTimeBinding
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.utils.App
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class UpdateMidTimeActivity : AppCompatActivity() {
    lateinit var binding:ActivityUpdateMidTimeBinding
    lateinit var progressDialog: ProgressDialog
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var  handler: Handler
    lateinit var rfidList:ArrayList<UpdateMidPoint>
    lateinit var tempList:ArrayList<String>
    lateinit var submitItem:ArrayList<UpdateMidTimeModelItem>
    var isPassed = ""
    var rfidNo = ""
    private var soundId = 0
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0
    lateinit var dialog:Dialog
    private var isDialogShowing = false
    lateinit var twoPointSevenFiveAdapter: UpdateMidPointAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateMidTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rfidList = arrayListOf()
        tempList = arrayListOf()
        submitItem = arrayListOf()
        handler = Handler()
        twoPointSevenFiveAdapter = UpdateMidPointAdapter(rfidList)
        iuhfService = UHFManager.getUHFService(this)

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

        // Format the date using the custom pattern
        val formattedDate = currentDateTime.format(formatter)
        binding.tvCurrentDateTime.text = formattedDate

        val receivedIntent = intent
        val receivedBundle = receivedIntent.extras

        binding.btnSubmit.setOnClickListener {
            Log.d("submitEvent",submitItem.toString())
            if (submitItem.isNotEmpty()) {
                submitEvent(submitItem)
            } else{
                Toast.makeText(this, "No item found for submit",Toast.LENGTH_SHORT).show()
            }
        }



//        binding.btnClear.setOnClickListener {
//            twoPointSevenFiveAdapter.clearData()
//        }


    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val formattedDate = localDateTime.atOffset(ZoneOffset.UTC).format(formatter)
        return formattedDate
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun submitEvent(updateMidTimeActivity: ArrayList<UpdateMidTimeModelItem>){
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
                    rfidList.clear()
                    tempList.clear()
                    submitItem.clear()
                    val adapter = UpdateMidPointAdapter(rfidList)
                    binding.listOfItem.adapter = adapter
                    adapter.clearData()
                    twoPointSevenFiveAdapter.notifyDataSetChanged()
                    Toast.makeText(this@UpdateMidTimeActivity,response.body().toString(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@UpdateMidTimeActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }

    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true
        initSoundPool()
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30

            val twoPointSevenFiveAdapter = UpdateMidPointAdapter(rfidList)
            binding.listOfItem.adapter = twoPointSevenFiveAdapter


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
        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
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
        soundId = soundPool!!.load(this@UpdateMidTimeActivity, R.raw.beep, 0)
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_F1) {
        if (!isInventoryRunning) {
            startSearching()

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
                                rfidList.add(UpdateMidPoint(getCurrentTime().toString(), rfidNo))

                                rfidNo = var1.getEpc()
                                val localDateTime = LocalDateTime.now() // Replace with your LocalDateTime object
                                val formattedDate2 = formatLocalDateTime(localDateTime)
                                submitItem.add(UpdateMidTimeModelItem(var1.getEpc(),formattedDate2))
                                runOnUiThread {

                                    binding.listOfItem.postDelayed({
                                       // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                        binding.listOfItem.scrollToPosition(rfidList.size)
                                    }, 100)
                                    binding.count.text = tempList.size.toString()
                                }
                               // val twoPointSevenFiveAdapter = UpdateMidPointAdapter(rfidList)
//                                val itemPosition = twoPointSevenFiveAdapter.getPositionOfItem(rfidNo)
//                                if (itemPosition != RecyclerView.NO_POSITION) {
//                                    twoPointSevenFiveAdapter.notifyItemChanged(itemPosition)
//                                }
                              //  binding.listOfItem.adapter = twoPointSevenFiveAdapter

                                twoPointSevenFiveAdapter.notifyDataSetChanged()

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

//        else{
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                //finish()
//            }
//        }
        return super.onKeyDown(keyCode, event)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTime(): String {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return currentTime.format(formatter)
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




}