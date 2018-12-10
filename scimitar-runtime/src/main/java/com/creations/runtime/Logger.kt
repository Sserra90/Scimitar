package com.creations.runtime

import android.util.Log

var debug: Boolean = BuildConfig.DEBUG
var debugTag = "Scimitar_Debug"

fun logd(msg: String, vararg args: Any) {
    if (debug) Log.d(debugTag, String.format(msg, args))
}

fun loge(msg: String, vararg args: Any) {
    if (debug) Log.e(debugTag, String.format(msg, args))
}