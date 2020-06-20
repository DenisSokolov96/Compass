package com.example.compass

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_statistic.*
import java.util.*

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
            val loc1 = Location("")
            loc1.latitude = list_GPS[0].latitude
            loc1.longitude = list_GPS[0].longitude
            val loc2 = Location("")
            var distanceInMeters  = 0.0
            while (i < list_GPS.size) {
                loc2.latitude = list_GPS[i].latitude
                loc2.longitude = list_GPS[i].longitude
                distanceInMeters += loc1.distanceTo(loc2)
                loc1.latitude =  loc2.latitude
                loc1.longitude =  loc2.longitude
                i++
            }
            if (distanceInMeters>1000) text_way.text = "Пройденный путь: %.3f".format(distanceInMeters/1000) + " км"
            else text_way.text = "Пройденный путь: %.1f".format(distanceInMeters) + " м"

            text_point.text = list_GPS.size.toString() + " точек."
        }
        else text_way.text = "Недостаточно данных."

    }
}
