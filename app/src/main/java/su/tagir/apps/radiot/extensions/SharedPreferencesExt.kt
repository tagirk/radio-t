package su.tagir.apps.radiot.extensions

import android.annotation.SuppressLint
import android.content.SharedPreferences


@SuppressLint("ApplySharedPref")
inline fun SharedPreferences.modify(commit: Boolean = false, action: SharedPreferences.Editor.() -> Unit) {
    val editor = edit()

    action(editor)

    if (commit) {
        editor.commit()
    } else {
        editor.apply()
    }
}