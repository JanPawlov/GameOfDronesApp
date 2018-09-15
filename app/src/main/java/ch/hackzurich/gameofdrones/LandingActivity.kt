package ch.hackzurich.gameofdrones

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import ch.hackzurich.gameofdrones.main.MainActivity

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
class LandingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkPermissionGPS(this))
            checkPermissionGPSAndRequestIfNotGranted(this)
        else
            startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!checkPermissionGPS(this)) {
            System.out.println("!checkPermissionGPS")
            finish()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}