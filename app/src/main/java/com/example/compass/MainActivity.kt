package com.example.compass

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.main_activity.*
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener, SensorEventListener {

    private val REQUEST_LOCATION = 2
    private var sensorManager: SensorManager? = null
    private var list_GPS = ArrayList<LatLng>()

    /***************/
    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    /***************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setLocation()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        startCompass()

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //вызов StaticActivity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(applicationContext, "статистика", Toast.LENGTH_LONG).show()
                val intent = Intent(this, StatisticActivity::class.java)
                intent.putExtra("list", list_GPS)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setLocation() {
      if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
      != PackageManager.PERMISSION_GRANTED
          && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
      != PackageManager.PERMISSION_GRANTED){
          ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
         REQUEST_LOCATION )
      }else{

          var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
          val criteria = Criteria()
          val provider = locationManager.getProviders(criteria, false) //locationManager.getBestProvider(criteria, false)
          val location = locationManager.getLastKnownLocation(provider[0])


          if (location != null) {
              text_view_location.text = convertLocationToString(location.latitude, location.longitude)
              /***************/
              if ( (list_GPS.size==0) || (list_GPS[list_GPS.size-1] != LatLng(location.latitude, location.longitude)) ){
                  list_GPS.add( LatLng(location.latitude, location.longitude))

                  mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
                  mapFragment.getMapAsync(OnMapReadyCallback {
                      googleMap = it

                      val location1 = LatLng(location.latitude, location.longitude)
                      googleMap.addMarker(MarkerOptions().position(location1).title("Точка - " + list_GPS.size.toString()))
                      googleMap.addPolyline(PolylineOptions()
                          .addAll(list_GPS)
                          .width(5f)
                          .color(Color.RED))
                      googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location1, 17f))
                  })
                  Toast.makeText(this,list_GPS.get(list_GPS.size-1).toString() + " :" + list_GPS.size.toString(),Toast.LENGTH_SHORT).show()
                  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,20000,0f, this)
              }

              /***************/
          }else{
                Toast.makeText(this,"Location not available!",Toast.LENGTH_SHORT).show()
          }
      }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_LOCATION) setLocation()
    }

    private fun convertLocationToString(latitude: Double, longitude: Double): String {

        val builder = StringBuilder()
        if (latitude < 0) builder.append("S ") else builder.append("N ")

        val latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS)
        val latitudeSplit = latitudeDegrees.split((":").toRegex()).dropLastWhile({it.isEmpty()}).toTypedArray()
        builder.append(latitudeSplit[0])
        builder.append("*")
        builder.append(latitudeSplit[1])
        builder.append("'")
        builder.append(latitudeSplit[2])
        builder.append("\"")
        builder.append("\n")

        if (longitude < 0) builder.append("W ") else builder.append("E ")
        val longlatitudeDegrees =  Location.convert(Math.abs(longitude),Location.FORMAT_SECONDS)
        val longitudeSplit = longlatitudeDegrees.split((":")).dropLastWhile({it.isEmpty()}).toTypedArray()
        builder.append(longitudeSplit[0])
        builder.append("*")
        builder.append(longitudeSplit[1])
        builder.append("'")
        builder.append(longitudeSplit[2])
        builder.append("\"")
        return builder.toString()
    }

    override fun onLocationChanged(location: Location?) {
        setLocation()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private var azimuth:Int = 0
    private var lastAccelerometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometer =  FloatArray(3)
    private var lastMagnetometerSet = false

        override fun onSensorChanged(event: SensorEvent?) {
            if (event!!.sensor.type == Sensor.TYPE_ROTATION_VECTOR){
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event!!.values)
                azimuth = (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0].toDouble())+360).toInt()%360
            }

            if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER ){
                System.arraycopy(event!!.values, 0, lastAccelerometer,0,event.values.size)
                lastAccelerometerSet = true
            }else if(event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD){
                System.arraycopy(event.values,0, lastMagnetometer,0,event.values.size)
                lastMagnetometerSet = true
            }
            if (lastAccelerometerSet && lastMagnetometerSet) {
                SensorManager.getRotationMatrix(rotationMatrix,null,lastAccelerometer, lastMagnetometer)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth = (Math.toDegrees(SensorManager.getOrientation(rotationMatrix,orientation)[0].toDouble())+360).toInt()%360
            }
            azimuth = Math.round(azimuth.toFloat())

            compass_image.rotation = (-azimuth).toFloat()

            val where = when(azimuth){
                in 281..349 -> "NW";
                in 261..280 -> "W"
                in 191..260 -> "SW"
                in 171..190 -> "S"
                in 101..170 -> "SE"
                in 81..100 ->  "E"
                in 11..80 ->  "NE"
                else -> "N"
            }
            text_view_degree.text = "$azimuth*$where"
    }

    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var haveSensorAccelerometer = false
    private var haveSensorMagnetometer = false
    private var rotationVector: Sensor? = null
    private var haveSensorRotationVector = false

    private fun startCompass(){
        if (sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null ||
                sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)==null){
                noSensorAlert()
            } else {
                accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                haveSensorAccelerometer = sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                haveSensorMagnetometer = sensorManager!!.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            }
        }else {
            rotationVector = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            haveSensorRotationVector = sensorManager!!.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopCompass(){
        if (haveSensorRotationVector) sensorManager!!.unregisterListener(this, rotationVector)
        if (haveSensorAccelerometer) sensorManager!!.unregisterListener(this, accelerometer)
        if (haveSensorMagnetometer ) sensorManager!!.unregisterListener(this, magnetometer)
    }

    private fun noSensorAlert() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Not support a compass!").setCancelable(false).setNegativeButton("Close"){_,_->finish()}
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        startCompass()
    }
    override fun onPause() {
        super.onPause()
        stopCompass()
    }
}
