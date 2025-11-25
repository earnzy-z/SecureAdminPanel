package com.earnzy.app.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

object AnimationUtils {
    
    /**
     * Entrance animation for cards - slide up with fade
     */
    fun slideUpIn(view: View, duration: Long = 400, delay: Long = 0) {
        view.translationY = 100f
        view.alpha = 0f
        
        val animY = ObjectAnimator.ofFloat(view, "translationY", 100f, 0f)
        val animAlpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        
        AnimatorSet().apply {
            playTogether(animY, animAlpha)
            this.duration = duration
            interpolator = DecelerateInterpolator()
            startDelay = delay
            start()
        }
    }
    
    /**
     * Card press animation with scale
     */
    fun pressAnimation(view: View, scale: Float = 0.95f, duration: Long = 150) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, scale)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, scale)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = duration
            start()
        }
    }
    
    /**
     * Spring animation for pop effect
     */
    fun springPop(view: View, duration: Long = 500) {
        view.scaleX = 0f
        view.scaleY = 0f
        
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = duration
            interpolator = OvershootInterpolator(1.5f)
            start()
        }
    }
    
    /**
     * Rotation + scale for loading effect
     */
    fun spinIn(view: View, duration: Long = 600) {
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f)
        
        AnimatorSet().apply {
            playTogether(rotation, scaleX, scaleY)
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }
    }
    
    /**
     * Fade in animation
     */
    fun fadeIn(view: View, duration: Long = 300) {
        view.alpha = 0f
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            this.duration = duration
            start()
        }
    }
    
    /**
     * Fade out animation
     */
    fun fadeOut(view: View, duration: Long = 300) {
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
            this.duration = duration
            start()
        }
    }
    
    /**
     * Bounce animation
     */
    fun bounce(view: View, distance: Float = 20f, duration: Long = 400) {
        val translateY = ObjectAnimator.ofFloat(view, "translationY", 0f, -distance, 0f)
        translateY.duration = duration
        translateY.start()
    }
    
    /**
     * Pulse animation for attention
     */
    fun pulse(view: View, duration: Long = 600) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = duration
            start()
        }
    }
    
    /**
     * Shake animation for error states
     */
    fun shake(view: View, distance: Float = 10f, duration: Long = 400) {
        val translateX = ObjectAnimator.ofFloat(
            view, "translationX",
            0f, -distance, distance, -distance, distance, 0f
        )
        translateX.duration = duration
        translateX.start()
    }
}
