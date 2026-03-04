package com.akshaglobal.smartcallshield.presentation.ui.dialer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class DialerActivity : Activity() {
    private var phoneNumber: String? = null
    private val REQUEST_CALL_PHONE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent?.action
        val data = intent?.data
        phoneNumber = data?.schemeSpecificPart

        if (action == Intent.ACTION_CALL) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                placeCall()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            }
        } else if (action == Intent.ACTION_DIAL || action == Intent.ACTION_VIEW) {
            // Show your custom dialer UI here, or just finish for now
            finish()
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                placeCall()
            } else {
                Toast.makeText(this, "CALL_PHONE permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun placeCall() {
        phoneNumber?.let {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$it"))
            startActivity(callIntent)
        }
        finish()
    }
}
