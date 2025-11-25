package com.earnzy.app.glide

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

/**
 * Glide configuration module for the Earnzy app.
 * This class configures Glide image loading library with optimized settings.
 */
@GlideModule
class EarnzyGlideModule : AppGlideModule() {
    
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Set default request options
        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565) // Use RGB_565 to reduce memory usage
        )
    }
    
    override fun isManifestParsingEnabled(): Boolean {
        // Disable manifest parsing for better performance
        return false
    }
}
