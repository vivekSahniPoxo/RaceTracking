package com.example.racetracking.crosscountry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.racetracking.bpet.UpdateMidTimeActivity
import com.example.racetracking.databinding.ActivityAllCrossCountryEventsBinding
import com.example.racetracking.databinding.ActivityCrossCountryBinding
import com.example.racetracking.utils.Cons

class AllCrossCountryEventsActivity : AppCompatActivity() {
    lateinit var binding:ActivityAllCrossCountryEventsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllCrossCountryEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardPointA.setOnClickListener {
            val intent = Intent(this, CrossCountryActivity::class.java)
            val bundle = Bundle()
            bundle.putString(Cons.GETEVENTFROMAPI, "Point A")
            intent.putExtras(bundle)
            startActivity(intent)
        }

        binding.cardPointB.setOnClickListener {
            val intent = Intent(this,CrossCountryActivity::class.java)
            val bundle = Bundle()
            bundle.putString(Cons.GETEVENTFROMAPI, "Point B")
            intent.putExtras(bundle)
            startActivity(intent)
        }

        binding.cardPointC.setOnClickListener {
            val intent = Intent(this,CrossCountryActivity::class.java)
            val bundle = Bundle()
            bundle.putString(Cons.GETEVENTFROMAPI, "Point C")
            intent.putExtras(bundle)
            startActivity(intent)

        }
    }
}