package com.example.racetracking.ppt

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.bpet.adapter.BPETAdapter
import com.example.racetracking.bpet.clickklistnerinterface.OnItemClickListener
import com.example.racetracking.bpet.datamodel.EventModel
import com.example.racetracking.databinding.ActivityPactivityBinding
import com.example.racetracking.localdatabase.EventDataBase
import com.example.racetracking.sprint.race_sprint_activity.HundredMeterRaceActivity
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.utils.App
import com.example.racetracking.utils.Cons
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PActivity : AppCompatActivity(),OnItemClickListener {
    lateinit var binding: ActivityPactivityBinding
    lateinit var progressDialog: ProgressDialog
    lateinit var bpetAdapter: BPETAdapter
    lateinit var mList: ArrayList<EventModel.EventModelItem>
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mList = arrayListOf()
        bpetAdapter = BPETAdapter(mList,this)
        //getEventData()
        if (mList.isNotEmpty()){
            mList.clear()
        }

        val database = Room.databaseBuilder(this@PActivity, EventDataBase::class.java, "ArmyEventDataBase").build()
        lifecycleScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    database.EventDao().getAllPPTEvent()
                }

                withContext(Dispatchers.Main) {
                    data.forEach {
                        mList.add(EventModel.EventModelItem(it.eventId, it.eventName))
                    }

                    bpetAdapter = BPETAdapter(mList, this@PActivity)
                    binding.recyclerView.adapter = bpetAdapter
                    bpetAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PActivity, "No data found", Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.imBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }

        binding.hundMtrRace.setOnClickListener {
            val intent = Intent(this, HundredMeterRaceActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getEventData(){
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

        RetrofitClient.getResponseFromApi().getPPTEvent().enqueue(object : Callback<EventModel> {
            override fun onResponse(call: Call<EventModel>, response: Response<EventModel>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                    bpetAdapter = response.body()?.let { BPETAdapter(it,this@PActivity) }!!
                    binding.recyclerView.adapter = bpetAdapter
                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EventModel>, t: Throwable) {
                Toast.makeText(this@PActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onItemClick(item: EventModel.EventModelItem) {
        val intent = Intent(this, PPTEeventsActivity::class.java)
        val bundle = Bundle()
        bundle.putString(Cons.GETEVENTFROMAPI, item.eventName)
        bundle.putInt(Cons.EVentID, item.eventId)
        intent.putExtras(bundle)
        startActivity(intent)
    }


}