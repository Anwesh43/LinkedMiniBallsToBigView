package com.example.linkedminiballstobigview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.miniballstobigview.MiniBallsToBigView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MiniBallsToBigView.create(this)
        fullScreen()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}