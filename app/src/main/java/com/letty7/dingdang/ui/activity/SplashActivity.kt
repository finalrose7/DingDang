package com.letty7.dingdang.ui.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
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
                .setTitle("提示")
                .setMessage("读取手机状态信息等权限仅用来统计分析，不会获取用户隐私，请放心授予权限！")
                .setNegativeButton("取消") { _, _ ->
                    request?.cancel()
                }
                .setPositiveButton("确定") { _, _ ->
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
                .setTitle("提示")
                .setMessage("缺少正常运行所需权限，请退出后重新打开并授予权限。")
                .setPositiveButton("确定") { _, _ -> finish() }
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
                .setTitle("提示")
                .setMessage("缺少正常运行所需权限，请前往设置授予权限。")
                .setNegativeButton("取消") { _, _ -> finish() }
                .setPositiveButton("确定") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                    finish()
                }
                .create()
                .show()
    }


}