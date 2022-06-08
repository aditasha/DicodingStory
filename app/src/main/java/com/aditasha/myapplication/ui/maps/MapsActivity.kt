package com.aditasha.myapplication.ui.maps

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.aditasha.myapplication.GlideApp
import com.aditasha.myapplication.R
import com.aditasha.myapplication.Result
import com.aditasha.myapplication.database.StoryEntity
import com.aditasha.myapplication.databinding.ActivityMapsBinding
import com.aditasha.myapplication.helper.dateFormat
import com.aditasha.myapplication.preferences.UserPreference
import com.aditasha.myapplication.ui.detail.DetailActivity
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import java.util.concurrent.TimeUnit


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var preference: UserPreference
    private lateinit var list: List<StoryEntity>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var boundsBuilder = LatLngBounds.builder()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLastLocation()
                }
                else -> {
                    // No location access granted.
                }
            }
        }

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK ->
                    Log.i(TAG, "onActivityResult: All location settings are satisfied.")
                RESULT_CANCELED ->
                    Toast.makeText(
                        this@MapsActivity,
                        "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preference = UserPreference(this@MapsActivity)

        val view: MapsViewModel by viewModels {
            MapsViewModelFactory(
                this@MapsActivity
            )
        }
        mapsViewModel = view

        // Delete data from database
        view.deleteStories()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.language_only, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.locale -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        createLocationRequest()

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        observe(mMap)
        setMapStyle()

        mMap.setOnMapLongClickListener { latLng ->
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("New Marker")
                    .snippet("Lat: ${latLng.latitude} Long: ${latLng.longitude}")
            )
        }

        mMap.setOnPoiClickListener { pointOfInterest: PointOfInterest ->
            val poiMarker = mMap.addMarker(
                MarkerOptions()
                    .position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
            poiMarker!!.showInfoWindow()
        }

        mMap.setOnInfoWindowClickListener { marker ->
            for (story in list) {
                if (marker.tag == story.id) {
                    val intent = Intent(this@MapsActivity, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.STORY, story)
                    startActivity(intent)
                }
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    showStartMarker(location)
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showStartMarker(location: Location) {
        val startLocation = LatLng(location.latitude, location.longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(startLocation)
                .title(getString(R.string.start_pin))
        )
        boundsBuilder.include(startLocation)
        val bounds: LatLngBounds = boundsBuilder.build()
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 128))
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getMyLastLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this@MapsActivity, sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun observe(map: GoogleMap) {
        val token = preference.getCred().token.toString()
        mapsViewModel.getStories(token).observe(this@MapsActivity) { result ->

            if (result != null) {
                when (result) {
                    is Result.Loading -> {
                        showFailed(false, "")
                        showLoading(true)
                    }
                    is Result.Success -> {
                        showFailed(false, "")
                        showLoading(false)

                        if (result.data != null) {
                            list = result.data
                            populateView(map, result.data)
                        }
                    }
                    is Result.Error -> {
                        showFailed(true, result.error)
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun populateView(map: GoogleMap, storyList: List<StoryEntity>) {
        for (story in storyList) {
            if (story.lat != null && story.lon != null) {
                binding.dataTextView.visibility = View.INVISIBLE
                val location = LatLng(story.lat, story.lon)
                GlideApp.with(this)
                    .asBitmap()
                    .dontTransform()
                    .load(story.photoUrl)
                    .into(object : CustomTarget<Bitmap>() {

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val markerView =
                                View.inflate(this@MapsActivity, R.layout.marker_image, null)
                            val roundImageView: ShapeableImageView =
                                markerView.findViewById(R.id.marker)
                            roundImageView.setImageBitmap(resource)

                            val spec =
                                View.MeasureSpec.makeMeasureSpec(175, View.MeasureSpec.EXACTLY)
                            roundImageView.measure(spec, spec)
                            roundImageView.layout(
                                0,
                                0,
                                roundImageView.measuredWidth,
                                roundImageView.measuredHeight
                            )

                            val bitmap = roundImageView.drawToBitmap()

                            map.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .title(story.name)
                                    .snippet(dateFormat(story.createdAt))
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            )?.tag = story.id
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            map.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .title(story.name)
                                    .snippet(dateFormat(story.createdAt))
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_AZURE
                                        )
                                    )
                            )?.tag = story.id
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })

                // automatically zoom out to view all story
                boundsBuilder.include(location)
                val bounds: LatLngBounds = boundsBuilder.build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 128))

            } else {
                binding.dataTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loading.visibility = View.VISIBLE
        } else {
            binding.loading.visibility = View.INVISIBLE
        }
    }

    private fun showFailed(isFailed: Boolean, e: String) {
        if (isFailed) {
            val text = getString(R.string.error, e)
            errorDialog(text).show()
        }
    }

    private fun errorDialog(e: String): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(this@MapsActivity)
            .setMessage(e)
            .setPositiveButton(resources.getString(R.string.close_dialog)) { dialog, _ ->
                dialog.dismiss()
            }
    }
}