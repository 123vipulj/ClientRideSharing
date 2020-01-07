package com.errorguys.clientridesharing

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.errorguys.clientridesharing.Fragments.NavigationView
import com.errorguys.clientridesharing.InterFaceConstant.Constant
import com.errorguys.clientridesharing.InterFaceConstant.locationListener
import com.errorguys.clientridesharing.MapsUtils.FetchAddressIntentService
import com.errorguys.clientridesharing.MapsUtils.Location
import com.errorguys.clientridesharing.PickUpDrop.DropActivity
import com.errorguys.clientridesharing.PickUpDrop.PickUpActivity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mRelativeLayout: RelativeLayout
    private lateinit var mFirstLinear: AppCompatImageView
    private lateinit var mToolbar: androidx.appcompat.widget.Toolbar
    private var lastTouchDownXY = IntArray(2)
    private lateinit var mRelativeLayoutPro : RelativeLayout
    var location: Location? = null
    var marker: Marker? = null
    private var lastLocation: android.location.Location? = null
    private var mResultReceiver: AddressResultReceiver? = null
    var autoCompleteFragment : AutocompleteSupportFragment? = null
    val TAG = "MAINLOG"


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Places.initialize(this, "AIzaSyCegfHgKwPaor1xs_4SgoQpnjAujj26CXk")
        val placesClient: PlacesClient = Places.createClient(this)

        // initialize view
        mRelativeLayout = findViewById(R.id.view)
        mToolbar = findViewById(R.id.toolbar1)
        mRelativeLayoutPro = RelativeLayout(this)
        mResultReceiver = AddressResultReceiver(Handler())

        mToolbar.title = ""

        // add layout to fragment
        supportFragmentManager.beginTransaction()
            .add(R.id.navigation_frame, NavigationView.newInstance(), "navi")
            .commit()

        // set profile invisible when first appear on screen
        navigation_frame.visibility = View.INVISIBLE

        setSupportActionBar(mToolbar)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // toolbar navigation and animate
        mToolbar.setNavigationOnClickListener {
            if (navigation_frame.isVisible) {
                val valueAnimator = ValueAnimator.ofFloat(1f, 0f)
                valueAnimator.addUpdateListener {
                    val value = it.animatedValue as Float
                    navigation_frame.alpha = value
                    if (value == 0.0.toFloat()) {
                        navigation_frame.visibility = View.INVISIBLE
                    }

                }
                valueAnimator.interpolator = LinearInterpolator()
                valueAnimator.duration = 400L
                valueAnimator.start()
                mToolbar.setNavigationIcon(R.drawable.ic_menu)


            }else {
                val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                valueAnimator.addUpdateListener {
                    val value = it.animatedValue as Float
                    navigation_frame.alpha = value

                }
                valueAnimator.interpolator = LinearInterpolator()
                valueAnimator.duration = 400L
                valueAnimator.start()
                mToolbar.setNavigationIcon(R.drawable.ic_left)
                navigation_frame.visibility = View.VISIBLE
            }
        }


        // location initializer,  request current location from gps
        location = Location(
            this,
            object :
                locationListener {
                override fun locationResponse(locationResult: LocationResult) {
                    mMap.clear()
                    val currentLoc = LatLng(
                        locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude
                    )

                    marker = mMap.addMarker(
                        MarkerOptions().icon(
                            BitmapDescriptorFactory.fromBitmap(bitmapScaledDescriptorFromVector())
                        ).position(currentLoc).title("hi")
                    )

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 14f))
                    startIntentService(currentLoc)
                    location?.stopUpdateLocation()
                }
            })

        //pickup search activity intent
        tittle_appbar.setOnClickListener {
            val fields: MutableList<Place.Field> = mutableListOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)

            intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setCountry("IN").build(this)
            startActivityForResult(intent, Constant.AUTOCOMPLETE_REQUEST_CODE_PICKUP)

        }

        //drop search activity intent
        tittle_droploc.setOnClickListener {
            val fields: MutableList<Place.Field> = mutableListOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)

            intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setCountry("IN").build(this)
            startActivityForResult(intent, Constant.AUTOCOMPLETE_REQUEST_CODE_PICKUP)
        }

    }


    /*
    animator start from where view setted when you translate y properties
    to out of screen .Then you need to animate view from out of screen into screen.
    * */
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        // gps point
        mMap.isMyLocationEnabled = true

        // hide with toolbar and bootom navigation
        mMap.setOnCameraMoveStartedListener {
            //clear off all properties of mMap object
            mMap.clear()



            // gps_pinpoint is image view fixed on center of screen when user drag map to prefered location,
            // map marker will gone and fake marker will display until user let out
            gps_pinpoint.visibility = View.VISIBLE
            tittle_droploc.visibility = View.INVISIBLE

            val valueAnimator = ValueAnimator.ofInt(0, 600)
            valueAnimator.addUpdateListener {
                val value = it.animatedValue as Int
                mRelativeLayout.translationY = (value).toFloat()

            }


            val valueAnimToolbar = ValueAnimator.ofInt(-1, -200)
            valueAnimToolbar.addUpdateListener {
                val value = it.animatedValue as Int
                mToolbar.translationY =  (value).toFloat()
            }

            // animator set animate two or more view in parallel
            val mAnimatorSet = AnimatorSet()
            mAnimatorSet.duration = 600
            mAnimatorSet.interpolator = AccelerateInterpolator()
            mAnimatorSet.play(valueAnimator).with(valueAnimToolbar)
            mAnimatorSet.start()

        }

        // when user let out finger from map this listener will activate
        mMap.setOnCameraIdleListener {

            // fake marker will gone and real marker put into center of map and fetch the longitude
            // and latitude of this place
            val midLatLng : LatLng = mMap.cameraPosition.target
            if (marker != null) {
                marker?.position = midLatLng
                mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                    .fromBitmap(bitmapScaledDescriptorFromVector()))
                    .position(marker!!.position).title("hi"))
                Toast.makeText(this, "" + midLatLng, Toast.LENGTH_SHORT).show()

                startIntentService( midLatLng)
            }

            gps_pinpoint.visibility = View.INVISIBLE
            tittle_droploc.visibility = View.VISIBLE


            val valueAnimator = ValueAnimator.ofInt(0, 600)
            valueAnimator.addUpdateListener {
                val value = it.animatedValue as Int
                mRelativeLayout.translationY = 600 - (value).toFloat()

            }


            val valueAnimToolbar = ValueAnimator.ofInt(-200, 0)
            valueAnimToolbar.addUpdateListener {
                val value = it.animatedValue as Int
                mToolbar.translationY =   (value).toFloat()

            }

            val mAnimatorSet = AnimatorSet()
            mAnimatorSet.duration = 600
            mAnimatorSet.interpolator = AccelerateInterpolator()
            mAnimatorSet.play(valueAnimator).with(valueAnimToolbar)
            mAnimatorSet.start()
        }

    }

    // get the height of screen
    fun getDisplayHeight() : Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        location?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        location?.inicializeLocation()
    }

    override fun onPause() {
        super.onPause()
        location?.stopUpdateLocation()
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun bitmapScaledDescriptorFromVector(): Bitmap? {
        val height: Int = 140
        val width: Int = 80
        val imageBitmap = BitmapFactory.decodeResource(resources, resources.getIdentifier("custommarker", "drawable", packageName))
        val bitmapDraw = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return bitmapDraw
    }

    // this method start address receiver intent, passing longitude and latitude object
    private fun startIntentService(vlatlng : LatLng) {
        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(Constant.RECEIVER, mResultReceiver)
            putExtra(Constant.LOCATION_DATA_EXTRA, vlatlng)
        }

        startService(intent)
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    inner class AddressResultReceiver internal constructor(handler: Handler) : ResultReceiver(handler) {
        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            text_pickup.text = resultData.getString(Constant.RESULT_DATA_KEY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // pickup
        if (requestCode == Constant.AUTOCOMPLETE_REQUEST_CODE_PICKUP) {
            when (resultCode) {
                RESULT_OK -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    Log.i(TAG, "Place: " + place.name + ", " + place.address)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Log.i(TAG, status.statusMessage.toString())
                }
                Activity.RESULT_CANCELED -> {

                }
            }
        }

        // drop
        if (requestCode == Constant.AUTOCOMPLETE_REQUEST_CODE_DROP) {
            when (resultCode) {
                RESULT_OK -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    Log.i(TAG, "Place: " + place.name + ", " + place.address)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Log.i(TAG, status.statusMessage.toString())
                }
                Activity.RESULT_CANCELED -> {

                }
            }
        }
    }


}
