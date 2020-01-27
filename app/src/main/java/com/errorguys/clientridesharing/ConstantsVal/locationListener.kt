package com.errorguys.clientridesharing.ConstantsVal

import com.google.android.gms.location.LocationResult

interface locationListener {
    fun locationResponse(locationResult: LocationResult)
}