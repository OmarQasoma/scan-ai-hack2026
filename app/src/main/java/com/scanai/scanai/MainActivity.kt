package com.scanai.scanai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.scanai.scanai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layoutProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.cardCreateOrder.setOnClickListener {
            startActivity(Intent(this, CreateOrderActivity::class.java))
        }
    }
}