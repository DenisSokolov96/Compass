package com.example.compass.ui.main

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.compass.R
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_statistic.*
import java.util.ArrayList

class StatisticActivity : AppCompatActivity() {

    var list_GPS = ArrayList<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //получаю координаты
        list_GPS = this.getIntent().getParcelableArrayListExtra("list")
        setContentView(R.layout.activity_statistic)
        way()
    }

    fun way(){
        if (list_GPS.size>1){
            var i = 1
            val p1 = doubleArrayOf(list_GPS[0].latitude,list_GPS[0].latitude)
            var distance  = 0.0
            while (i < list_GPS.size) {
                val p2 = doubleArrayOf(list_GPS[i].latitude,list_GPS[i].latitude)
                distance += Math.sqrt(
                    Math.pow((p1[0]-p2[0]),2.0)
                    +
                    Math.pow((p1[1]-p2[1]),2.0)
                )
                p1[0] = p2[0]
                p1[1] = p2[1]
                i++
            }
            if (distance>1000) text_way.text = "Пройденный путь: %.3f".format(distance/1000) + " км"
            else text_way.text = "Пройденный путь: %.1f".format(distance) + " м"
        }
        else text_way.text = "Недостаточно данных."

    }
}
