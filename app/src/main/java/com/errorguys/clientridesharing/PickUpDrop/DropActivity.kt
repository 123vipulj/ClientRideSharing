package com.errorguys.clientridesharing.PickUpDrop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.errorguys.clientridesharing.R

class DropActivity : AppCompatActivity(){

    lateinit var toolbarDrop: Toolbar
    lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drop_activity)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        toolbarDrop = findViewById(R.id.toolbar_drop)
        searchView = findViewById(R.id.serachview_drop)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}