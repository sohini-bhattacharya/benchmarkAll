package com.example.benchmarkall

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class ForegroundService : SensorEventListener, LocationListener, Service() {

    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock

    private lateinit var light_manager: SensorManager
    private var light_sensor: Sensor? = null

    private lateinit var proxi_manager: SensorManager
    private var proxi_sensor: Sensor? = null

    private lateinit var mag_manager : SensorManager
    private var mag_sensor: Sensor? = null

    private lateinit var orient_manager : SensorManager
    private var orient_sensor: Sensor? = null

    private lateinit var pressure_manager : SensorManager
    private var pressure_sensor: Sensor? = null

    private lateinit var location_manager : LocationManager
//    private lateinit var location_sensor :


    lateinit var fileOutputStream: FileOutputStream
    lateinit var outputWriter: OutputStreamWriter

    lateinit var builder: NotificationCompat.Builder

    var ForegroundPermission = false
    var WakePermission = false
    var WritePermission = false
    var ReadPermission = false
    var Internet = false
    var FineLocation = false
    var CoarseLocation = false

    override fun onCreate() {
        //onCreate shouldn't be used for sensor u should use onStartCommand
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        fileOutputStream = openFileOutput("final_test.txt", Context.MODE_APPEND)
        outputWriter = OutputStreamWriter(fileOutputStream)
        outputWriter.write("systemTime,hourOfDay,proximityValue,lightValue,yaw,pitch,roll,x,y,z,pressure,latitude,longitude"+"\n")

        outputWriter.close()

        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "MyApp::MyWakelockTag")
        wakeLock.acquire()

        location_manager = getSystemService(LOCATION_SERVICE) as LocationManager
        isGPSEnabled = location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnabled = location_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//        location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
//        location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        location_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10)
//            location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10)
//        };


        light_manager = getSystemService(SENSOR_SERVICE) as SensorManager
        light_sensor = light_manager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        proxi_manager = getSystemService(SENSOR_SERVICE) as SensorManager
        proxi_sensor = proxi_manager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)


        mag_manager = getSystemService(SENSOR_SERVICE) as SensorManager
        mag_sensor = mag_manager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        orient_manager = getSystemService(SENSOR_SERVICE) as SensorManager
        orient_sensor = orient_manager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        pressure_manager = getSystemService(SENSOR_SERVICE) as SensorManager
        pressure_sensor = pressure_manager!!.getDefaultSensor(Sensor.TYPE_PRESSURE)


        light_manager.registerListener(this, light_sensor, SENSOR_DELAY_FASTEST)
        proxi_manager.registerListener(this, proxi_sensor, SENSOR_DELAY_FASTEST)

        pressure_manager.registerListener(this, pressure_sensor, 5000000)

        mag_manager.registerListener(this, mag_sensor, SENSOR_DELAY_FASTEST)
        orient_manager.registerListener(this, orient_sensor, SENSOR_DELAY_FASTEST)

        Log.i("HERE","HERE")
        generateForegroundNotification()


        return START_REDELIVER_INTENT
    }



    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 123

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, 0)
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(mNotificationManager != null)
                mNotificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup("chats_group", "Chats")
                )
                val notificationChannel =
                    NotificationChannel("service_channel", "Service Notifications",
                        NotificationManager.IMPORTANCE_MIN)
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            builder = NotificationCompat.Builder(this, "service_channel")
            builder.setContentTitle(StringBuilder(resources.getString(R.string.app_name)).append(" service is running").toString())
                .setTicker(StringBuilder(resources.getString(R.string.app_name)).append("service is running").toString())
                .setContentText("Touch to open") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.jupiter)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }

            notification = builder.build()
            startForeground(mNotificationId, notification)

        }
    }
    private fun csv(str:String) {
        try {
            fileOutputStream = openFileOutput("hello1.txt", Context.MODE_APPEND)
            outputWriter = OutputStreamWriter(fileOutputStream)
            outputWriter.write(str+"\n")

            outputWriter.close()

        } catch (e: IOException) {

        }
    }

    override fun onDestroy() {

        light_manager.unregisterListener(this)
        proxi_manager.unregisterListener(this)
        mag_manager.unregisterListener(this)
        pressure_manager.unregisterListener(this)
        orient_manager.unregisterListener(this)
        wakeLock.release()
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show()
        Log.i("HERE","STOPPED")

        super.onDestroy()

    }



    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            pressure = event.values[0]

            pressure_text.text = pressure.toString()


            Log.v("PRESSURE", "${System.currentTimeMillis()},${pressure}")
//            csvlight("${System.currentTimeMillis()},${light}")


        }

        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            light = event.values[0]


            light_text.text = light.toString()

            Log.v("LIGHT", "${System.currentTimeMillis()},${light}")
//            csvlight("${System.currentTimeMillis()},${light}")

//
        }

        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            proxi = event.values[0]


            proximity_text.text = proxi.toString()

            Log.v("PROXI", "${System.currentTimeMillis()},${proxi}")
//            csvproxi("${System.currentTimeMillis()},${proxi}")


        }

        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            x = event.values[0]
            y = event.values[1]
            z = event.values[2]

            x_text.text = x.toString()
            y_text.text = y.toString()
            z_text.text = z.toString()


            Log.v("MAG", "${System.currentTimeMillis()},${x},${y},${z}")
//            csvlight("${System.currentTimeMillis()},${light}")


        }

        if (event?.sensor?.type == Sensor.TYPE_ORIENTATION) {
            yaw = event.values[0]
            pitch = event.values[1]
            roll = event.values[2]

            yaw_text.text = yaw.toString()
            pitch_text.text = pitch.toString()
            roll_text.text = roll.toString()


            Log.v("ORIENT", "${System.currentTimeMillis()},${yaw},${pitch},${roll}")
//            csvlight("${System.currentTimeMillis()},${light}")


        }

        if(status) {

            csv(
                "${System.currentTimeMillis()},${
                    SimpleDateFormat(
                        "HH",
                        Locale.US
                    ).format(Date())
                },${proxi},${light},${yaw},${pitch},${roll},${x},${y},${z},${pressure},${latitude},${longitude}"
//                ${pressure}
            )
            Log.i("ALL","${System.currentTimeMillis()},${SimpleDateFormat("HH", Locale.US).format(Date())}," +
                    "${proxi.toInt()},${light.toInt()}," +
                    "${yaw.toInt()},${pitch.toInt()},${roll.toInt()}," +
                    "${x.toInt()},${y.toInt()},${z.toInt()},${pressure.toInt()},${latitude.toInt()},${longitude.toInt()}")
//
        }
//        Log.i("DOOR","${System.currentTimeMillis()},${SimpleDateFormat("HH", Locale.US).format(Date())},${proxi.toInt()},${light.toInt()},${door}")
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }
//    private val locationListener: LocationListener = object : LocationListener {
//        override fun onLocationChanged(loc: Location) {
//            latitude = loc.latitude.toFloat()
//            latitude = loc.longitude.toFloat()
//        }
//
//        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
//        override fun onProviderEnabled(provider: String) {}
//        override fun onProviderDisabled(provider: String) {}
//
//    }

//    private fun requestPermission(){
//        ForegroundPermission = ContextCompat.checkSelfPermission(this,
//            Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
//
//        WakePermission = ContextCompat.checkSelfPermission(this,
//            Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED
//
//        FineLocation = ContextCompat.checkSelfPermission(this,
//            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//
//        Internet = ContextCompat.checkSelfPermission(this,
//            Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
//
//        WritePermission = ContextCompat.checkSelfPermission(this,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//
//        ReadPermission = ContextCompat.checkSelfPermission(this,
//            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//
//        CoarseLocation = ContextCompat.checkSelfPermission(this,
//            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//
//        val permissionsRequest : MutableList<String> = ArrayList()
//
//
//        if(!CoarseLocation){
//            permissionsRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
//        }
//
//        if(!ForegroundPermission){
//            permissionsRequest.add(Manifest.permission.FOREGROUND_SERVICE)
//        }
//
//        if(!WakePermission){
//            permissionsRequest.add(Manifest.permission.WAKE_LOCK)
//        }
//
//        if(!WritePermission){
//            permissionsRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        }
//
//        if(!ReadPermission){
//            permissionsRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
//        }
//        if(!Internet){
//            permissionsRequest.add(Manifest.permission.INTERNET)
//        }
//
//        if(!FineLocation){
//            permissionsRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//
//        if(permissionsRequest.isNotEmpty()){
//            permissionLauncher.launch(permissionsRequest.toTypedArray())
//        }



    override fun onLocationChanged(loc: Location) {
        if(isGPSEnabled || isNetworkEnabled) {
            latitude = loc.latitude.toFloat()
            longitude = loc.longitude.toFloat()

            latitude_text.text = latitude.toString()
            longitude_text.text = longitude.toString()

            Log.v("LOC", "${System.currentTimeMillis()},${latitude},${longitude}")
        }

    }

}