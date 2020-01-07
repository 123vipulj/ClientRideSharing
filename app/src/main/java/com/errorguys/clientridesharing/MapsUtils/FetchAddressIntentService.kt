package com.errorguys.clientridesharing.MapsUtils

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import android.widget.Toast
import com.errorguys.clientridesharing.InterFaceConstant.Constant
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.round

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

        val location :LatLng? = intent.getParcelableExtra(
            Constant.LOCATION_DATA_EXTRA
        )

        var addresses: List<Address> = emptyList()

        try {
            /*
            *  geocoder cannot reverse process every long decimal latitude and longitude value
            * , so better limit the decimal
            * */
            val latitude_dec = DecimalFormat("#.###")
                latitude_dec.roundingMode = RoundingMode.CEILING

            val longitude_dec = DecimalFormat("#.###")
            latitude_dec.roundingMode = RoundingMode.CEILING


            Log.e(TAG,  (latitude_dec.format(location!!.longitude)).toString() + "  " + (longitude_dec.format(location.latitude)).toString())
            addresses = geocoder!!.getFromLocation(latitude_dec.format(location.latitude).toDouble(), latitude_dec.format(
                location.longitude).toDouble(), 1)
        } catch (ioException: IOException) {
            Log.e(TAG, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, "invalid lat long used")
        }

        if (addresses.isEmpty()) {
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

            deliverResultToReceiver(
                Constant.SUCCESS_RESULT,
                addressFragment.joinToString(separator = "\n"))

        }
    }

    // send data to MainActivity where onReceiveResult method implemented
    // which wait for result to come
    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle().apply { putString(Constant.RESULT_DATA_KEY, message) }
        receiver?.send(resultCode, bundle)
    }

    fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()
}