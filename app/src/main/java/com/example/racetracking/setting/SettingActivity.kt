package com.example.racetracking.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.racetracking.R
import com.example.racetracking.databinding.ActivitySettingBinding
import com.example.racetracking.utils.CacheHelper
import com.example.racetracking.utils.Cons
import com.example.racetracking.utils.sharPref.SharePref
import com.google.android.material.snackbar.Snackbar

class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding
    lateinit var sharePref: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharePref = SharePref()

        binding.buttonSubmitUrl.setOnClickListener {
            sharePref.clearAll()
            CacheHelper.clearWebViewCache(this)
            CacheHelper.clearApplicationData(this)
            val updateBaseUrl = binding.baseUrlConfig.text.toString().trim()
            if (updateBaseUrl.isNotEmpty()) {
                Cons.BASE_URL = "http://$updateBaseUrl"
                binding.ipconfigForm.visibility = View.GONE
                sharePref.saveData("baseUrl", Cons.BASE_URL)
                //showExitConfirmationDialog()
                Toast.makeText(this@SettingActivity, Cons.BASE_URL, Toast.LENGTH_SHORT).show()
                Snackbar.make(binding.root,"Please! close the application and reopen", Snackbar.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SettingActivity, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ipconfig.setOnClickListener {
            binding.ipconfigForm.visibility = View.VISIBLE

        }
    }
}