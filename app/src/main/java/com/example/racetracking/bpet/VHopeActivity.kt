package com.example.racetracking.bpet

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.R
import com.example.racetracking.bpet.adapter.TwoPointSevenFiveAdapter
import com.example.racetracking.bpet.datamodel.Rfid
import com.example.racetracking.bpet.datamodel.SubmitEvent
import com.example.racetracking.databinding.ActivityVhopeBinding
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.utils.App
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.utils.ErrorStatus
import com.speedata.libuhf.utils.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class VHopeActivity : AppCompatActivity() {
    lateinit var binding:ActivityVhopeBinding
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var  handler: Handler
    lateinit var rfidList:ArrayList<Rfid>
    lateinit var tempList:ArrayList<String>
    lateinit var submitItem:ArrayList<SubmitEvent>
    var isPassed = ""
    var rfidNo = ""
    lateinit var progressDialog: ProgressDialog
    lateinit var twoPointSevenFiveAdapter: TwoPointSevenFiveAdapter
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityVhopeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rfidList = arrayListOf()
        tempList = arrayListOf()
        submitItem = arrayListOf()
        handler = Handler()
        twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
        iuhfService = UHFManager.getUHFService(this)
        val currentDateTime = LocalDateTime.now()
        val customPattern = "MMM dd, yyyy"
        val formatter = DateTimeFormatter.ofPattern(customPattern, Locale.ENGLISH)

        // Format the date using the custom pattern
        val formattedDate = currentDateTime.format(formatter)
        binding.tvCurrentDateTime.text = formattedDate

        val receivedIntent = intent
        val receivedBundle = receivedIntent.extras

        binding.btnSubmit.setOnClickListener {
            submitEvent(submitItem)
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

        binding.btnPass.setOnClickListener {
            if (receivedBundle != null) {
                val message = receivedBundle.getInt("key")
                isPassed = "1"
                val updateValue = rfidList.find { it.rfid == rfidNo }
                updateValue?.isPassed = "1"
                twoPointSevenFiveAdapter.notifyDataSetChanged()

                submitItem.add(SubmitEvent(0, message, rfidNo, 1, currentDateTime.toString()))
                //submitEvent(submitItem)
            }
        }

        binding.btnClear.setOnClickListener {
            twoPointSevenFiveAdapter.clearData()
        }

        binding.btnFail.setOnClickListener {

            if (receivedBundle != null) {
                val message = receivedBundle.getInt("key")
                isPassed = "2"
                val updateValue = rfidList.find { it.rfid == rfidNo }
                updateValue?.isPassed = "2"
                twoPointSevenFiveAdapter.notifyDataSetChanged()
                submitItem.add(SubmitEvent(0, message, rfidNo, 0, currentDateTime.toString()))
                //submitEvent(submitItem)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode==131) {
            if (isInventoryRunning == false) {
                startSearching()

                iuhfService.setOnReadListener { var1 ->
                    val stringBuilder = StringBuilder()
                    val epcData = var1.epcData
                    val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
                    if (!TextUtils.isEmpty(hexString)) {
                        stringBuilder.append("EPC：").append(hexString).append("\n")
                    } else {
                        //Toast.makeText(this, "No Scan", Toast.LENGTH_SHORT).show()
                    }
                    if (var1.status == 0) {
                        val readData = var1.readData
                        val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
                        stringBuilder.append("ReadData:").append(readHexString).append("\n")
                        Toast.makeText(this, readHexString, Toast.LENGTH_SHORT).show()
                        if (!tempList.contains(readHexString)) {
                            tempList.add(readHexString)
                            rfidList.add(Rfid(tempList.size,readHexString,""))
                            rfidNo = readHexString

                        }

                        val twoPointSevenFiveAdapter = TwoPointSevenFiveAdapter(rfidList)
                        binding.listOfItem.adapter = twoPointSevenFiveAdapter
                        twoPointSevenFiveAdapter.notifyDataSetChanged()
                    } else {
                        stringBuilder.append(this.resources.getString(R.string.read_fail))
                            .append(":").append(ErrorStatus.getErrorStatus(this,var1.status)).append("\n")
                    }
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder))

                }
                val readArea = iuhfService.readArea(1, 2, 6, "00000000")
                if (readArea != 0) {
                    val err: String =
                        this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(this,readArea) + "\n"
                    handler.sendMessage(handler.obtainMessage(1, err))

                }

                return true
            } else {
                stopSearching()
            }
        }
        else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        return super.onKeyUp(keyCode, event)
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

        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30


        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }

        iuhfService.inventoryStart()
    }

    override fun onBackPressed() {
        //  stopInventoryService()
        val intent = Intent(this, BPETActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        iuhfService.closeDev()
        finish()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun submitEvent(submitEvent: kotlin.collections.ArrayList<SubmitEvent>){
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

        RetrofitClient.getResponseFromApi().submitEvent(submitEvent).enqueue(object :
            Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                    submitItem.clear()
                    Toast.makeText(this@VHopeActivity,response.body().toString(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@VHopeActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }
}