package com.earnzy.utils

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.fragment.app.Fragment
import com.earnzy.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Number formatting extensions
fun Int.formatCurrency(): String {
    return "₹${NumberFormat.getInstance().format(this)}"
}

fun Double.formatCurrency(): String {
    return "₹${NumberFormat.getInstance().format(this.toInt())}"
}

fun Int.formatCompact(): String {
    return when {
        this >= 1_000_000 -> "${this / 1_000_000}M"
        this >= 1_000 -> "${this / 1_000}K"
        else -> this.toString()
    }
}

// Date formatting extensions
fun Long.formatDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.formatTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.formatDateTime(): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun String.formatDate(fromPattern: String = "yyyy-MM-dd", toPattern: String = "dd MMM yyyy"): String {
    return try {
        val originalFormat = SimpleDateFormat(fromPattern, Locale.getDefault())
        val targetFormat = SimpleDateFormat(toPattern, Locale.getDefault())
        val date = originalFormat.parse(this)
        targetFormat.format(date!!)
    } catch (e: Exception) {
        this
    }
}

// View visibility extensions
fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.hideWithFade(duration: Long = 300) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction { visibility = View.GONE }
        .start()
}

fun View.showWithFade(duration: Long = 300) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .start()
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

// SharedPreferences extensions
fun SharedPreferences.getStringOrNull(key: String): String? {
    return if (contains(key)) getString(key, null) else null
}

fun SharedPreferences.getIntOrNull(key: String): Int? {
    return if (contains(key)) getInt(key, 0) else null
}

// String extensions
fun String?.isValidEmail(): Boolean {
    return !isNullOrEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String?.isValidPhone(): Boolean {
    return !isNullOrEmpty() && this!!.length >= 10
}

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
}

// Fragment extensions
fun Fragment.getSharedPreferences(name: String = "earnzy_prefs"): SharedPreferences {
    return requireContext().getSharedPreferences(name, Context.MODE_PRIVATE)
}

fun Fragment.saveToPref(key: String, value: String) {
    getSharedPreferences().edit().putString(key, value).apply()
}

fun Fragment.saveToPref(key: String, value: Int) {
    getSharedPreferences().edit().putInt(key, value).apply()
}

fun Fragment.saveToPref(key: String, value: Boolean) {
    getSharedPreferences().edit().putBoolean(key, value).apply()
}

fun Fragment.getFromPref(key: String, default: String = ""): String {
    return getSharedPreferences().getString(key, default) ?: default
}

fun Fragment.getFromPref(key: String, default: Int = 0): Int {
    return getSharedPreferences().getInt(key, default)
}

fun Fragment.getFromPref(key: String, default: Boolean = false): Boolean {
    return getSharedPreferences().getBoolean(key, default)
}

fun Fragment.clearPreferences() {
    getSharedPreferences().edit().clear().apply()
}
