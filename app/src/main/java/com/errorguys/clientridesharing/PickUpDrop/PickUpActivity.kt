package com.errorguys.clientridesharing.PickUpDrop

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.errorguys.clientridesharing.R
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*

class PickUpActivity: AppCompatActivity() {

    lateinit var toolbarPickup : Toolbar
    lateinit var searchView: SearchView

    val TAG = "PickUPActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pickup_activity)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        toolbarPickup = findViewById(R.id.toolbar_pickup)
        // searchView = findViewById(R.id.serachview_pickup)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}