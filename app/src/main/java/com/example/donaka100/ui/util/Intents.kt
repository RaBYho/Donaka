package com.example.donaka100.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.ouvrirAppel(telephone: String) {
    try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telephone")))
    } catch (_: ActivityNotFoundException) { }
}

fun Context.ouvrirSms(telephone: String, message: String) {
    try {
        startActivity(
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$telephone"))
                .putExtra("sms_body", message)
        )
    } catch (_: ActivityNotFoundException) { }
}