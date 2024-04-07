package com.digital.pianoassist

import android.util.Log

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun Any.logInformation(message: String) {
    Log.i(TAG, message)
}

fun Any.logDebug(message: String) {
    Log.d(TAG, message)
}
