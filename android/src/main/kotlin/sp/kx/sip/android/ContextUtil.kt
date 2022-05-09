package sp.kx.sip.android

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast

fun Context.getSharedPreferences(): SharedPreferences {
    return getSharedPreferences(
        "${BuildConfig.APPLICATION_ID}.preferences",
        Context.MODE_PRIVATE
    ) ?: error("Shared preferences does not exist!")
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
