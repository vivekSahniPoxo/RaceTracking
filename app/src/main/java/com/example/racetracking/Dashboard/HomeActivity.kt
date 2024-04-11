package com.example.racetracking.Dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.example.racetracking.R
import com.example.racetracking.bpet.BPETActivity
import com.example.racetracking.bpet.adapter.TwoPointSevenFiveAdapter
import com.example.racetracking.bpet.datamodel.SubmitEvent
import com.example.racetracking.crosscountry.CrossCountryActivity
import com.example.racetracking.databinding.ActivityHomeBinding
import com.example.racetracking.localdatabase.EventDataBase
import com.example.racetracking.ppt.PActivity
import com.example.racetracking.ppt.hundredmtrrace.HundredMeterRaceActivity
import com.example.racetracking.race_type.RaceTypeActivity
import com.example.racetracking.race_type.data.PostRaceResultItelItem
import com.example.racetracking.race_type.data.RaceRegsModel
import com.example.racetracking.reports.ReportsActivity
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.utils.App
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.quite_dialog.view.*
import kotlinx.android.synthetic.main.remove_item_dialog.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    lateinit var binding:ActivityHomeBinding
    lateinit var progressDialog: ProgressDialog
    private var soundId = 0
    private var soundPool: SoundPool? = null
    lateinit var posdtRaceResultList:ArrayList<PostRaceResultItelItem>

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        posdtRaceResultList = arrayListOf()
        val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()

        binding.btnSynceData.setOnClickListener {
            Thread {
                val data = database.EventDao().getAllData() // Replace with your specific DAO and query
                val postRaceResultList = ArrayList<PostRaceResultItelItem>()

                data.forEach {
                    postRaceResultList.add(PostRaceResultItelItem(it.armyNumber, it.chestNumber, it.soldierType, it.raceResultMasterId, it.startTime, it.midPoint))
                }

                if (postRaceResultList.isNotEmpty()) {
                    runOnUiThread {
                        postRaceResult(postRaceResultList)
                    }
                } else {
                    runOnUiThread {
                        Snackbar.make(binding.root, "No Data Found", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }


        binding.raceSprint.setOnClickListener {
            val intent = Intent(this, HundredMeterRaceActivity::class.java)
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
                    progressDialog.dismiss()
                    if (response.body()==null){
                        progressDialog.dismiss()

                    }
                    val database = Room.databaseBuilder(applicationContext, EventDataBase::class.java, "ArmyEventDataBase").build()
                    GlobalScope.launch {
                        database.EventDao().deleteAttandeeDetails()
                    }
                    Toast.makeText(this@HomeActivity,response.body().toString(),Toast.LENGTH_SHORT).show()

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

                    Toast.makeText(this@HomeActivity,response.body().toString(),Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@HomeActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
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
        }