package com.earnzy.ui.fragments

import android.view.View
import androidx.fragment.app.Fragment
import com.earnzy.R
import com.google.android.material.snackbar.Snackbar

open class BaseFragment : Fragment() {
    protected fun showError(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        view?.let {
            Snackbar.make(it, message, duration).apply {
                setBackgroundTint(resources.getColor(R.color.error, null))
                setTextColor(resources.getColor(R.color.on_error, null))
            }.show()
        }
    }

    protected fun showSuccess(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        view?.let {
            Snackbar.make(it, message, duration).apply {
                setBackgroundTint(resources.getColor(R.color.primary, null))
                setTextColor(resources.getColor(R.color.on_primary, null))
            }.show()
        }
    }

    protected fun showWarning(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        view?.let {
            Snackbar.make(it, message, duration).apply {
                setBackgroundTint(resources.getColor(R.color.tertiary, null))
                setTextColor(resources.getColor(R.color.on_tertiary, null))
            }.show()
        }
    }

    protected fun showInfo(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        view?.let {
            Snackbar.make(it, message, duration).apply {
                setBackgroundTint(resources.getColor(R.color.secondary, null))
                setTextColor(resources.getColor(R.color.primary, null))
            }.show()
        }
    }

    protected fun showLoading(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    protected fun showProgress(view: View?, progress: Int) {
        view?.let {
            if (it is android.widget.ProgressBar) {
                it.progress = progress.coerceIn(0, 100)
            }
        }
    }

    protected fun fadeIn(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }

    protected fun fadeOut(view: View, duration: Long = 300) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction { view.visibility = View.GONE }
            .start()
    }
}
