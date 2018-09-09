package com.letty7.dingdang.ui.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.letty7.dingdang.R
import permissions.dispatcher.*


@RuntimePermissions
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showPermissionsWithPermissionCheck()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
    )
    fun showPermissions() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    @OnShowRationale(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
    )
    fun showRationale(request: PermissionRequest?) {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.c_tips)
                .setMessage(R.string.permission_rationale)
                .setNegativeButton(R.string.c_cancel) { _, _ ->
                    request?.cancel()
                }
                .setPositiveButton(R.string.c_sure) { _, _ ->
                    request?.proceed()
                }
                .create()
                .show()
    }

    @OnPermissionDenied(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE)
    fun onDenied() {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.c_tips)
                .setMessage(R.string.permission_denied)
                .setPositiveButton(R.string.c_sure) { _, _ -> finish() }
                .create()
                .show()
    }

    @OnNeverAskAgain(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE)
    fun onNeverAskAgain() {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.c_tips)
                .setMessage(R.string.permission_ask_again)
                .setNegativeButton(R.string.c_cancel) { _, _ -> finish() }
                .setPositiveButton(R.string.c_sure) { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                    finish()
                }
                .create()
                .show()
    }


}