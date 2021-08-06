package com.satpal.sliderbuttonexample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.satpal.sliderbutton.ISliderButtonCallbacks
import com.satpal.sliderbutton.SliderButtonView

class MainActivity : AppCompatActivity(), ISliderButtonCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sliderButtonView = findViewById<SliderButtonView>(R.id.slider_button)
        sliderButtonView.callbacks = this
    }

    override fun sliderUnlocked() {
        Toast.makeText(this, "slider unlocked", Toast.LENGTH_LONG).show()
    }
}