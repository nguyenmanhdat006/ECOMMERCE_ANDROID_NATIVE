package com.example.ecommerce.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ecommerce.R
import dagger.hilt.android.AndroidEntryPoint

// Một Activity duy nhất làm "khung"; các màn hình là Fragment được điều hướng bằng nav_graph
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
