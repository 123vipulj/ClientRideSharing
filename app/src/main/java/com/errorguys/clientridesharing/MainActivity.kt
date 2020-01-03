package com.errorguys.clientridesharing

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.provider.CalendarContract
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*

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
    lateinit var text_title: TextView


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRelativeLayout = findViewById(R.id.view)
        mToolbar = findViewById(R.id.toolbar1)
        mFirstLinear = findViewById(R.id.firstImage)
        mRelativeLayoutPro = RelativeLayout(this)
        mResultReceiver = AddressResultReceiver(Handler())
        text_title = findViewById(R.id.tittle_appbar)


        // set profile invisible when first appear on screen
        profile_setting.visibility = View.INVISIBLE

        setSupportActionBar(mToolbar)

        mToolbar.setNavigationOnClickListener {
            if (profile_setting.isVisible) {
                val valueAnimator = ValueAnimator.ofFloat(1f, 0f)
                valueAnimator.addUpdateListener {
                    val value = it.animatedValue as Float
                    profile_setting.alpha = value
                    if (value == 0.0.toFloat()) {
                        profile_setting.visibility = View.INVISIBLE
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
                    profile_setting.alpha = value

                }
                valueAnimator.interpolator = LinearInterpolator()
                valueAnimator.duration = 400L
                valueAnimator.start()
                mToolbar.setNavigationIcon(R.drawable.ic_left)
                profile_setting.visibility = View.VISIBLE
            }
        }


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        firstImage.setOnClickListener {

            first.getLocationOnScreen(lastTouchDownXY)

            val floatX = lastTouchDownXY[0]
            val floatY = lastTouchDownXY[1]

            val txt_price = TextView(this)
            txt_price.width = 150
            txt_price.maxWidth = 160
            txt_price.textSize = 17f
            txt_price.setPadding(10)
            txt_price.text = "Rs. 120"
            txt_price.setTextColor(Color.RED)
            txt_price.background = ContextCompat.getDrawable(this, R.drawable.roundpatch)
            txt_price.translationX = floatX.toFloat()
            txt_price.translationY = (floatY - 200).toFloat()
            txt_price.elevation = 8f
            activity_root_layout.addView(txt_price)
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
        }

        location = Location(this, object : locationListner {
            override fun locationResponse(locationResult: LocationResult) {
                mMap.clear()
                val currentLoc = LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                marker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmapScaledDescriptorFromVector())).position(currentLoc).title("hi"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 14f))
                location?.stopUpdateLocation()
            }
        })
    }


        /*
        animator start from where it set when you translate y to out of screen .
        Then you need to animate from outside the screen into screen.
        * */

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        // hide with toolbar and bootom navigation
        mMap.setOnCameraMoveStartedListener {
            mMap.clear()
            gps_pinpoint.visibility = View.VISIBLE

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

            val mAnimatorSet = AnimatorSet()
            mAnimatorSet.duration = 600
            mAnimatorSet.interpolator = AccelerateInterpolator()
            mAnimatorSet.play(valueAnimator).with(valueAnimToolbar)
            mAnimatorSet.start()

        }


        mMap.setOnCameraIdleListener {


            val midLatLng : LatLng = mMap.cameraPosition.target
            if (marker != null) {
                marker?.position = midLatLng
                mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmapScaledDescriptorFromVector())).position(marker!!.position).title("hi"))
                startIntentService(midLatLng)
            }

            gps_pinpoint.visibility = View.INVISIBLE



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
            text_title.text = resultData.getString(Constant.RESULT_DATA_KEY)
        }
    }

}
