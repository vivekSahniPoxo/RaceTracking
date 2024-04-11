package com.example.racetracking.bpet

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.racetracking.Dashboard.HomeActivity
import com.example.racetracking.MainActivity
import com.example.racetracking.R
import com.example.racetracking.bpet.adapter.BPETAdapter
import com.example.racetracking.bpet.clickklistnerinterface.OnItemClickListener
import com.example.racetracking.bpet.datamodel.EventModel
import com.example.racetracking.databinding.ActivityBpetactivityBinding
import com.example.racetracking.ppt.fivemetershuttle.FiveMeterShuttleActivity
import com.example.racetracking.retrofit.RetrofitClient
import com.example.racetracking.utils.App
import com.example.racetracking.utils.Cons
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BPETActivity : AppCompatActivity(),OnItemClickListener {
    lateinit var binding:ActivityBpetactivityBinding
    lateinit var bpetAdapter: BPETAdapter
    lateinit var progressDialog: ProgressDialog
    lateinit var mList: ArrayList<EventModel.EventModelItem>
    var getEventID = ""
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBpetactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        mList = arrayListOf()
         bpetAdapter = BPETAdapter(mList,this)
         getEventData()
        binding.recyclerView.adapter = bpetAdapter

        binding.imBack.setOnClickListener {

            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }

//        bpetAdapter.onItemClick = {
//            Toast.makeText(this,"Clicked",Toast.LENGTH_SHORT).show()
//            Log.d("ClickTest", "Item clicked: ${it.eventName}")
//            val intent = Intent(this,MainActivity::class.java)
//            val bundle  = Bundle()
//            bundle.putString(Cons.GETEVENTFROMAPI,it.eventName)
//            bundle.putInt(Cons.EVentID,it.eventId)
//            intent.putExtras(bundle)
//            startActivity(intent)
//
//        }

        bpetAdapter.onItemClick = { item ->
            Toast.makeText(this, "Item Clicked: ${item.eventName}", Toast.LENGTH_SHORT).show()
        }




        binding.updateMidPoint.setOnClickListener {
            val intent = Intent(this,UpdateMidTimeActivity::class.java)
            val bundle = Bundle()
            val message = 1
            bundle.putInt("key", message)
            intent.putExtras(bundle)
            startActivity(intent)
        }
//
//        binding.fiveKm.setOnClickListener {
//            val intent = Intent(this,VHopeActivity::class.java)
//            val bundle = Bundle()
//            val message = 4
//            bundle.putInt("key", message)
//            intent.putExtras(bundle)
//            startActivity(intent)
//        }
//
//        binding.HRope.setOnClickListener {
//            val intent = Intent(this,HHopeActivity::class.java)
//            val bundle = Bundle()
//            val message = 4
//            bundle.putInt("key", message)
//            intent.putExtras(bundle)
//            startActivity(intent)
//        }


    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getEventData(){
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            Snackbar.make(binding.root,"No Internet",Snackbar.LENGTH_SHORT).show()
            return
        } else{
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
            progressDialog.show()
        }

        RetrofitClient.getResponseFromApi().getEventS().enqueue(object :Callback<EventModel>{
            override fun onResponse(call: Call<EventModel>, response: Response<EventModel>) {

                if (response.code()==200){
                    progressDialog.dismiss()
                   bpetAdapter = response.body()?.let { BPETAdapter(it,this@BPETActivity) }!!
                    binding.recyclerView.adapter = bpetAdapter
                } else if (response.code()==404){
                    Snackbar.make(binding.root,response.body().toString(),Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==400){
                    Snackbar.make(binding.root,response.body().toString(),Snackbar.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Snackbar.make(binding.root,response.body().toString(),Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EventModel>, t: Throwable) {
               Toast.makeText(this@BPETActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onItemClick(item: EventModel.EventModelItem) {
        Toast.makeText(this, "Clicked: ${item.eventName}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SeventyFiveMeterRace::class.java)
        val bundle = Bundle()
        bundle.putString(Cons.GETEVENTFROMAPI, item.eventName)
        bundle.putInt(Cons.EVentID, item.eventId)
        intent.putExtras(bundle)
        startActivity(intent)
    }


}