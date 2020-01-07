package com.errorguys.clientridesharing.MapsUtils

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.errorguys.clientridesharing.InterFaceConstant.locationListener
import com.google.android.gms.location.*

open class Location (var activity: AppCompatActivity, locationListner: locationListener) {
    private val permissionFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permissionCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    private val REQUEST_CODE_LOCATION = 100

    internal var fusedLocationClient: FusedLocationProviderClient? = null

    private var locationRequest : LocationRequest? = null
    private var callBack: LocationCallback? = null

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity.applicationContext)
        initializeLocationRequest()
        callBack = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)

                locationListner.locationResponse(p0!!)
            }
        }
    }

    private fun initializeLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest?.interval = 50000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun validatePermissionLocation(): Boolean {
        val fineLocationAvailable= ActivityCompat.checkSelfPermission(activity.applicationContext, permissionFineLocation)== PackageManager.PERMISSION_GRANTED
        val coarseLocationAvailable=ActivityCompat.checkSelfPermission(activity.applicationContext, permissionCoarseLocation)==PackageManager.PERMISSION_GRANTED

        return fineLocationAvailable && coarseLocationAvailable
    }

    private fun requestPermissions(){
        val contextProvider=ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionFineLocation)

        if(contextProvider){
            Toast.makeText(activity.applicationContext, "Permission is required to obtain location", Toast.LENGTH_SHORT).show()
        }
        permissionRequest()
    }

    fun onRequestPermissionsResult(requestCode:Int, permissions:Array<out String>, grantResults:IntArray){
        when(requestCode){
            REQUEST_CODE_LOCATION->{
                if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getLocation()
                }else{
                    Toast.makeText(activity.applicationContext, "You did not give permissions to get location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun permissionRequest(){
        ActivityCompat.requestPermissions(activity, arrayOf(permissionFineLocation, permissionCoarseLocation), REQUEST_CODE_LOCATION)
    }

    fun stopUpdateLocation(){
        this.fusedLocationClient?.removeLocationUpdates(callBack)
    }
    fun inicializeLocation(){
        if (validatePermissionLocation()){
            getLocation()
        }else{
            requestPermissions()
        }
    }
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        validatePermissionLocation()
        fusedLocationClient?.requestLocationUpdates(locationRequest, callBack, null)
    }
}