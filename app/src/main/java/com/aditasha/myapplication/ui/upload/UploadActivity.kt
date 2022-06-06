package com.aditasha.myapplication.ui.upload

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aditasha.myapplication.R
import com.aditasha.myapplication.Result
import com.aditasha.myapplication.api.GeneralResponse
import com.aditasha.myapplication.api.LoginResult
import com.aditasha.myapplication.databinding.ActivityUploadBinding
import com.aditasha.myapplication.helper.bitmapToFile
import com.aditasha.myapplication.helper.reduceFileImage
import com.aditasha.myapplication.helper.rotateBitmap
import com.aditasha.myapplication.preferences.UserPreference
import com.aditasha.myapplication.ui.camera.CameraActivity
import com.aditasha.myapplication.ui.home.HomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var uploadViewModel: UploadViewModel
    private lateinit var preference: UserPreference
    private lateinit var credentials: LoginResult

    private lateinit var reducedSize: File

    private lateinit var fusedLocationClient: FusedLocationProviderClient
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

    private var lat: Float = 0f
    private var lon: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preference = UserPreference(this@UploadActivity)
        credentials = preference.getCred()
        if (!credentials.name.isNullOrEmpty()) {
            binding.senderTextView.text = getString(R.string.sender_upload, credentials.name)

            binding.uploadButton.setOnClickListener { uploadImage() }
        }

        val view: UploadViewModel by viewModels {
            UploadViewModelFactory(
                this@UploadActivity,
                credentials.token.toString()
            )
        }
        uploadViewModel = view

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getMyLastLocation()

        setupView()

        val result = intent.getIntExtra(CameraActivity.RESULT, 0)
        if (result == CAMERA_X_RESULT) {
            val image = intent.getSerializableExtra(CameraActivity.PICTURE) as File
            val isBackCamera = intent.getBooleanExtra(CameraActivity.BACK_CAMERA, true)
            val rotatedImage = rotateBitmap(
                BitmapFactory.decodeFile(image.path),
                isBackCamera
            )
            val getFile = bitmapToFile(rotatedImage, this@UploadActivity)
            reducedSize = reduceFileImage(getFile)
            binding.previewImage.setImageBitmap(rotatedImage)
        } else {
            val image = intent.getSerializableExtra(CameraActivity.PICTURE) as File
            reducedSize = reduceFileImage(image)
            val bitmap = BitmapFactory.decodeFile(image.path)
            binding.previewImage.setImageBitmap(bitmap)
        }
    }

    private fun setupView() {
        supportActionBar?.hide()
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
                    lat = location.latitude.toFloat()
                    lon = location.longitude.toFloat()
                } else {
                    Toast.makeText(
                        this@UploadActivity,
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

    private fun uploadImage() {
        val textDesc = binding.uploadTextLayout.text.toString()

        val description = textDesc.toRequestBody("text/plain".toMediaType())
        val requestImageFile = reducedSize.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            reducedSize.name,
            requestImageFile
        )

        if (lat != 0f && lon != 0f) {
            execute(description, imageMultipart, lat, lon)
        } else {
            execute(description, imageMultipart)
        }
    }

    private fun execute(
        desc: RequestBody,
        image: MultipartBody.Part,
        lat: Float? = null,
        lon: Float? = null
    ) {
        if (lat != null && lon != null) {
            uploadViewModel.upload(desc, image, lat, lon).observe(this@UploadActivity) { result ->
                observer(result)
            }
        } else {
            uploadViewModel.upload(desc, image).observe(this@UploadActivity) { result ->
                observer(result)
            }
        }
    }

    private fun observer(result: Result<GeneralResponse>) {
        when (result) {
            is Result.Loading -> {
                showLoading(true)
                showFailed(false, "")
            }
            is Result.Error -> {
                showLoading(false)
                showFailed(true, result.error)
            }
            is Result.Success -> {
                showLoading(false)
                showFailed(false, "")
                Toast.makeText(this@UploadActivity, R.string.upload_success, Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(this@UploadActivity, HomeActivity::class.java)
                startActivity(intent)
            }
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
        return MaterialAlertDialogBuilder(this@UploadActivity)
            .setMessage(e)
            .setPositiveButton(resources.getString(R.string.close_dialog)) { dialog, _ ->
                dialog.dismiss()
            }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        const val GALLERY = 201
    }
}