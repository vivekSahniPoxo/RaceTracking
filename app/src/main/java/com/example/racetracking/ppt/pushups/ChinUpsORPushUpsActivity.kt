package com.example.racetracking.ppt.pushups

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.racetracking.R
import com.example.racetracking.bpet.adapter.TwoPointSevenFiveAdapter
import com.example.racetracking.bpet.datamodel.Rfid
import com.example.racetracking.databinding.ActivityChinUpsOrpushUpsBinding
import com.speedata.libuhf.IUHFService

class ChinUpsORPushUpsActivity : AppCompatActivity() {
    lateinit var binding:ActivityChinUpsOrpushUpsBinding
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var  handler: Handler
    lateinit var rfidList:ArrayList<Rfid>
    lateinit var tempList:ArrayList<String>
    var isPassed = ""
    var rfidNo = ""
    lateinit var progressDialog: ProgressDialog
    lateinit var twoPointSevenFiveAdapter: TwoPointSevenFiveAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChinUpsOrpushUpsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}