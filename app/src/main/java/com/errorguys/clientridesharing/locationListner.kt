package com.errorguys.clientridesharing

import com.google.android.gms.location.LocationResult

interface locationListner {
    fun locationResponse(locationResult: LocationResult)
}