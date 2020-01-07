package com.errorguys.clientridesharing.InterFaceConstant

import com.google.android.gms.location.LocationResult

interface locationListener {
    fun locationResponse(locationResult: LocationResult)
}