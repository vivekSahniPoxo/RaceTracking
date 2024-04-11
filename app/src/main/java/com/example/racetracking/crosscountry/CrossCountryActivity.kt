package com.example.racetracking.crosscountry

import android.annotation.SuppressLint
import android.app.Dialog
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
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.racetracking.R
import com.example.racetracking.bpet.BPETActivity
import com.example.racetracking.bpet.adapter.UpdateMidPointAdapter
import com.example.racetracking.bpet.datamodel.UpdateMidPoint
import com.example.racetracking.bpet.datamodel.UpdateMidTimeModelItem
import com.example.racetracking.bpet.datamodel.UpdateMidTimeModelLocalDb
import com.example.racetracking.bpet.datamodel.tempMidOrTurningPoint
import com.example.racetracking.databinding.ActivityCrossCountryBinding
import com.example.racetracking.utils.Cons
import com.google.android.material.button.MaterialButton
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import kotlinx.android.synthetic.main.quite_dialog.view.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class CrossCountryActivity : AppCompatActivity() {
    lateinit var iuhfService: IUHFService
    var isInventoryRunning = false
    private var soundId = 0
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0
    var rfidNo = ""
    lateinit var tempList:ArrayList<String>
    lateinit var rfidList:ArrayList<UpdateMidPoint>
    lateinit var binding:ActivityCrossCountryBinding
    lateinit var submitItem:ArrayList<UpdateMidTimeModelItem>
    lateinit var twoPointSevenFiveAdapter: UpdateMidPointAdapter
    lateinit var tempSubmitItem:ArrayList<UpdateMidTimeModelItem>
    lateinit var dialog:Dialog
    private var isDialogShowing = false
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrossCountryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tempList  = arrayListOf()
        rfidList = arrayListOf()
        tempSubmitItem = arrayListOf()
        submitItem = arrayListOf()
        twoPointSevenFiveAdapter = UpdateMidPointAdapter(rfidList)
        iuhfService = UHFManager.getUHFService(this)




        val receivedIntent = intent
        val receivedBundle = receivedIntent.extras
        if (receivedBundle!=null){
            binding.tvEventNAme.text = receivedBundle.getString(Cons.GETEVENTFROMAPI)
            binding.tvToolBarTitle.text = receivedBundle.getString(Cons.GETEVENTFROMAPI)
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


        binding.btnSubmit.setOnClickListener {
            Log.d("submitEvent",submitItem.toString())
            if (submitItem.isNotEmpty()) {
                //submitEvent(submitItem)
//                tempSubmitItem.forEach {
//                    updateMidTimeViewModel.addMidOrTurningPoint(UpdateMidTimeModelLocalDb(0, it.chestNo, it.midtime))
//                    eventsViewModel.tempMidOrTurningPoint(tempMidOrTurningPoint(0,it.chestNo,it.midtime))
//                }

                rfidList.clear()
                tempList.clear()
                submitItem.clear()
                binding.count.text = ""
                val adapter = UpdateMidPointAdapter(rfidList)
                binding.listOfItem.adapter = adapter
                adapter.clearData()
                twoPointSevenFiveAdapter.notifyDataSetChanged()
            } else{
                Toast.makeText(this, "No item found for submit", Toast.LENGTH_SHORT).show()
            }
        }


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


                            rfidNo = var1.getEpc().substring(0,4)
                            if (rfidNo!=null) {
                                if (!tempList.contains(rfidNo)) {
                                    tempList.add(rfidNo)
                                    rfidList.add(UpdateMidPoint(getCurrentTime().toString(), rfidNo))

                                    rfidNo = var1.getEpc()
                                    val localDateTime = LocalDateTime.now() // Replace with your LocalDateTime object
                                    val formattedDate2 = formatLocalDateTime(localDateTime)
                                    tempSubmitItem.add(UpdateMidTimeModelItem(rfidNo,localDateTime.toString()))
                                    submitItem.add(UpdateMidTimeModelItem(var1.getEpc(),formattedDate2))

                                    runOnUiThread {

                                        binding.listOfItem.postDelayed({
                                            // binding.listOfItem.scrollToPosition(twoPointSevenFiveAdapter.itemCount - 1)
                                            binding.listOfItem.scrollToPosition(rfidList.size)
                                        }, 100)
                                        binding.count.text = tempList.size.toString()
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

        return super.onKeyDown(keyCode, event)
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
        })
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
        runOnUiThread(kotlinx.coroutines.Runnable {
            val blinkingDot = binding.blinkingDot
            blinkingDot.clearAnimation()
            blinkingDot.visibility = View.GONE
        })
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
        soundId = soundPool!!.load(this@CrossCountryActivity, R.raw.beep, 0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTime(): String {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return currentTime.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val formattedDate = localDateTime.atOffset(ZoneOffset.UTC).format(formatter)
        return formattedDate
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
            val intent = Intent(this, AllCrossCountryEventsActivity::class.java)
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