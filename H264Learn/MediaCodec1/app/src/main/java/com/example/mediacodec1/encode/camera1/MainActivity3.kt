package com.example.mediacodec1.encode.camera1

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.example.mediacodec1.R
import com.example.mediacodec1.util.permission.PermissionsActivity
import com.example.mediacodec1.util.permission.PermissionsChecker

class MainActivity3 : AppCompatActivity() {
    // 请求码
    private val RESULT_CODE = 0
    val permissions = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val mPermissionsChecker = PermissionsChecker(this)
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker!!.checkPermissions(*permissions)) {
            //startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            PermissionsActivity.startActivityForResult(this, RESULT_CODE, *permissions)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == RESULT_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish()
        }
    }
}