package com.earnzy.app.utils

import android.view.View
import android.view.animation.AnimationUtils
import com.earnzy.app.R

object ShimmerHelper {
    /**
     * Apply shimmer loading animation to a view
     */
    fun startShimmer(view: View) {
        try {
            val shimmerAnim = AnimationUtils.loadAnimation(view.context, R.anim.shimmer_animation)
            view.startAnimation(shimmerAnim)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Stop shimmer animation and clear it
     */
    fun stopShimmer(view: View) {
        try {
            view.clearAnimation()
            view.alpha = 1.0f
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Show skeleton layout, hide content
     */
    fun showSkeleton(skeleton: View?, content: View?) {
        skeleton?.visibility = View.VISIBLE
        content?.visibility = View.GONE
        skeleton?.let { startShimmer(it) }
    }

    /**
     * Hide skeleton layout, show content
     */
    fun hideSkeleton(skeleton: View?, content: View?) {
        skeleton?.visibility = View.GONE
        skeleton?.let { stopShimmer(it) }
        content?.visibility = View.VISIBLE
    }

    /**
     * Fade out and hide with animation
     */
    fun fadeOutAndHide(view: View?, duration: Long = 300) {
        view?.let {
            it.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction { it.visibility = View.GONE }
                .start()
        }
    }

    /**
     * Fade in and show with animation
     */
    fun fadeInAndShow(view: View?, duration: Long = 300) {
        view?.let {
            it.alpha = 0f
            it.visibility = View.VISIBLE
            it.animate()
                .alpha(1f)
                .setDuration(duration)
                .start()
        }
    }
}
