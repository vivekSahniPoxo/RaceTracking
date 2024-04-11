package com.example.racetracking.ppt

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.R
import com.example.racetracking.bpet.adapter.TwoPointSevenFiveAdapter
import com.example.racetracking.bpet.adapter.UpdateMidPointAdapter
import com.example.racetracking.bpet.datamodel.Rfid
import com.example.racetracking.bpet.datamodel.SubmitEvent
import com.example.racetracking.bpet.datamodel.SubmitPPTEvent
import com.example.racetracking.bpet.datamodel.UpdateMidTimeModelItem
import com.example.racetracking.databinding.ActivityPpteeventsBinding
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.utils.App
import com.example.racetracking.utils.Cons
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class PPTEeventsActivity : AppCompatActivity() {
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
    lateinit var twoPointSevenFiveAdapter:TwoPointSevenFiveAdapter
    lateinit var progressDialog: ProgressDialog
    lateinit var dialog:Dialog
    private var isDialogShowing = false
    lateinit var submitItem:kotlin.collections.ArrayList<SubmitPPTEvent>
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
        twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
        iuhfService = UHFManager.getUHFService(this)
        statusList()

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
            val selectedPosition = binding.spType.selectedItemPosition
            if (selectedPosition==0){
                Toast.makeText(this,"Please select status",Toast.LENGTH_SHORT).show()
            } else if (submitItem.isNotEmpty()){
                submitEvent(submitItem)
                //submitItem.add(SubmitPPTEvent(0,"","","",""))
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
                        Log.d("selected",selected)
                    }
                    //Toast.makeText(this@LoginActivity, "position" + binding.spType.getItemIdAtPosition(position), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val selectedPosition = binding.spType.selectedItemPosition
        if (selectedPosition==0){
            Snackbar.make(binding.root,"Please select status",Snackbar.LENGTH_SHORT).show()
        } else
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
                                rfidList.add(Rfid(tempList.size,var1.getEpc(),""))
                                rfidNo = rfidNo
                                val localDateTime = LocalDateTime.now() // Replace with your LocalDateTime object
                                val formattedDate2 = formatLocalDateTime(localDateTime)
                                val receivedIntent = intent
                                val receivedBundle = receivedIntent.extras
                                if (receivedBundle!=null) {

                                    submitItem.add(
                                        SubmitPPTEvent(
                                            0,
                                            receivedBundle.getInt(Cons.EVentID),
                                            rfidNo,
                                            selected,
                                            formattedDate2
                                        )
                                    )
                                }
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
    }


    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true
        initSoundPool()

        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30

            val twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
            binding.listOfItem.adapter = twoPointSevenFiveAdapter

        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }

        iuhfService.inventoryStart()
    }

    override fun onBackPressed() {
        //  stopInventoryService()
        val intent = Intent(this, PActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
       // iuhfService.closeDev()
       // finish()
    }

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
                    rfidList.clear()
                    val adapter = TwoPointSevenFiveAdapter(rfidList)
                    binding.listOfItem.adapter = adapter
                    adapter.clearData()
                    twoPointSevenFiveAdapter.notifyDataSetChanged()
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