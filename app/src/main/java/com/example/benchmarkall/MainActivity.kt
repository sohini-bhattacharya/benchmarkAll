package com.example.benchmarkall

//proximity, light, locationLatitude, locationLongitude, barometericPressure (or NA),
// orientationPitch, orientationYaw, orientationRoll, compassDegreesX, compassDegreesY, compassDegreesZ

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.collections.ArrayList

var light: Float = 0.0f
var proxi: Float = 0.0f
var x:  Float = 0.0f
var y:  Float = 0.0f
var z:  Float = 0.0f
var yaw:  Float = 0.0f
var pitch:  Float = 0.0f
var roll:  Float = 0.0f
var pressure: Float = 0.0f
var latitude: Float = 0.0f
var longitude: Float = 0.0f
var status: Boolean = false
var isGPSEnabled: Boolean = false
var isNetworkEnabled: Boolean = false


lateinit var proximity_text: TextView
lateinit var light_text: TextView
lateinit var yaw_text: TextView
lateinit var pitch_text: TextView
lateinit var roll_text: TextView
lateinit var x_text: TextView
lateinit var y_text: TextView
lateinit var z_text: TextView
lateinit var pressure_text: TextView
lateinit var latitude_text: TextView
lateinit var longitude_text: TextView


class MainActivity : AppCompatActivity() {
//    EasyPermissions.PermissionCallbacks
//    RadioGroup.OnCheckedChangeListener



    lateinit var intentService: Intent

    lateinit var permission_button: Switch
    lateinit var start_button: Switch

    lateinit var storage: SharedPreferences


    lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    var ForegroundPermission = false
    var WakePermission = false
    var WritePermission = false
    var ReadPermission = false
    var Internet = false
    var FineLocation = false
    var CoarseLocation = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storage = PreferenceManager.getDefaultSharedPreferences(this)
        permission_button = findViewById(R.id.permissions)
        start_button = findViewById(R.id.startservice)
        proximity_text = findViewById(R.id.proximity)
        pressure_text = findViewById(R.id.pressure)
        light_text = findViewById(R.id.light)
        yaw_text = findViewById(R.id.yaw)
        pitch_text = findViewById(R.id.pitch)
        roll_text = findViewById(R.id.roll)
        x_text = findViewById(R.id.x)
        y_text = findViewById(R.id.y)
        z_text = findViewById(R.id.z)
        latitude_text = findViewById(R.id.latitude)
        longitude_text = findViewById(R.id.longitude)

        permission_button.isChecked = getRadioState()

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                CoarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: CoarseLocation
                FineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: FineLocation
                Internet = permissions[Manifest.permission.INTERNET] ?: Internet
                ForegroundPermission = permissions[Manifest.permission.FOREGROUND_SERVICE] ?: ForegroundPermission
                WakePermission = permissions[Manifest.permission.WAKE_LOCK] ?: WakePermission
                WritePermission = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: WritePermission
                ReadPermission = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: ReadPermission
            }


        permission_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ) {

                    requestPermission()
                    permission_button.isChecked = true
                    saveRadioState(true)

                } else {
//                    requestForUpdates()
                }
            } else {
                saveRadioState(false)
//                deregisterForUpdates()
            }
        }

        start_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ) {

                    intentService = Intent(this, ForegroundService::class.java)
                    startService(intentService)
                    status = true
                    start_button.isChecked = true
                    saveRadioState(true)
                    Toast.makeText(this,"STARTED",Toast.LENGTH_SHORT).show()


                } else {
//                    requestForUpdates()
                }
            } else {
                stopService(intentService)
                status = false
                Toast.makeText(this,"STOPPED",Toast.LENGTH_SHORT).show()
                saveRadioState(false)
//                deregisterForUpdates()
            }
        }


    }

    private fun requestPermission(){
        ForegroundPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED

        WakePermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED

        FineLocation = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        Internet = ContextCompat.checkSelfPermission(this,
            Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED

        WritePermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        ReadPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        CoarseLocation = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val permissionsRequest : MutableList<String> = ArrayList()


        if(!CoarseLocation){
            permissionsRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if(!ForegroundPermission){
            permissionsRequest.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        if(!WakePermission){
            permissionsRequest.add(Manifest.permission.WAKE_LOCK)
        }

        if(!WritePermission){
            permissionsRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(!ReadPermission){
            permissionsRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!Internet){
            permissionsRequest.add(Manifest.permission.INTERNET)
        }

        if(!FineLocation){
            permissionsRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if(permissionsRequest.isNotEmpty()){
            permissionLauncher.launch(permissionsRequest.toTypedArray())
        }
    }


    private fun saveRadioState(value: Boolean) {
        storage
            .edit()
            .putBoolean(Constants.RADIO, value)
            .apply()
    }

    private fun getRadioState() = storage.getBoolean(Constants.RADIO, false)


}