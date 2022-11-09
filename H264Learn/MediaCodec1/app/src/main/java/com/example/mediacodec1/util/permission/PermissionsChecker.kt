package com.example.mediacodec1.util.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionsChecker(context: Context) {
    private var mContext: Context = context.applicationContext

    // 判断权限集合
    fun checkPermissions(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (checkPermission(permission)) {
                return true
            }
        }
        return false
    }

    // 判断是否缺少权限
    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_DENIED
    }
}