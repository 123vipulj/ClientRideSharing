package com.errorguys.clientridesharing

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.opengl.GLES32
import android.os.Bundle
import android.os.ResultReceiver
import android.provider.SyncStateContract
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*

class FetchAddressIntentService : IntentService(TAG) {

    var geocoder: Geocoder? = null
    private var receiver: ResultReceiver? = null

    companion object {
        private val TAG = "FetchAddressIS"
    }


    override fun onHandleIntent(intent: Intent?){
        intent ?: return

        var errorMessage = ""
        receiver = intent.getParcelableExtra(Constant.RECEIVER)

        // Check if receiver was properly registered.
        if (receiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to resend the results.")
            return
        }

        geocoder = Geocoder(this, Locale.getDefault())

        val location = intent.getParcelableExtra<LatLng>(
            Constant.LOCATION_DATA_EXTRA)

        var addresses: List<Address> = emptyList()

        try {
            addresses = geocoder!!.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioException: IOException) {
            Log.e(TAG, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, "invalid lat long used")
        }

        if (addresses == null || addresses.isEmpty()) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no address found"
            }
            Log.e(TAG, errorMessage)
            deliverResultToReceiver(Constant.FAILURE_RESULT, errorMessage)

        }else {
            val address = addresses[0]

            val addressFragment = with(address) {
                (0..maxAddressLineIndex).map { getAddressLine(it) }
            }

            deliverResultToReceiver(Constant.SUCCESS_RESULT,
                addressFragment.joinToString(separator = "\n"))

        }
    }

    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle().apply { putString(Constant.RESULT_DATA_KEY, message) }
        receiver?.send(resultCode, bundle)
    }

}