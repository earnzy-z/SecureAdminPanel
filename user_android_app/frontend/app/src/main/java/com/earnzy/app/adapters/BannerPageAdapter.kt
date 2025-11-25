// adapters/BannerPageAdapter.kt (Advanced: Admin limit via update, URL priority load, duration caching, analytics callback)
package com.earnzy.app.adapters

import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.earnzy.app.R
import com.earnzy.app.models.BannerItem
import com.google.android.material.card.MaterialCardView
import java.lang.Runnable

class BannerPageAdapter(
    private var items: List<BannerItem>,
    private val onClick: ((BannerItem) -> Unit)? = null,
    private val onAnalytics: ((BannerItem) -> Unit)? = null  // New: Admin analytics hook
) : RecyclerView.Adapter<BannerPageAdapter.VH>() {

    init {
        if (items.isEmpty()) {
            // Total 3 default images as per request (admin fallback)
            items = listOf(
                BannerItem(id = "default_1", drawableRes = R.drawable.sample_banner_1),
                BannerItem(id = "default_2", drawableRes = R.drawable.sample_banner_2),
                BannerItem(id = "default_3", drawableRes = R.drawable.sample_banner_3)  // Assuming R.drawable.sample_banner_3 exists
            )
        } else if (items.size < 3) {
            items = items + List(3 - items.size) { index ->
                BannerItem(id = "fallback_$index", drawableRes = R.drawable.sample_banner_3)  // Default to 3rd image
            }
        }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.bannerCard)
        val image: ImageView = itemView.findViewById(R.id.bannerImage)
        val titleOverlay: TextView? = itemView.findViewById(R.id.banner_title_overlay)
        var pendingRunnable: Runnable? = null
        var startTime: Long = 0L
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_banner_pager, parent, false)
        // Advanced: Dynamic card radius based on admin config (placeholder)
        view.findViewById<MaterialCardView>(R.id.bannerCard).radius = 20f
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val realPos = if (items.isNotEmpty()) position % items.size else 0
        val item = items[realPos]

        if (!item.isActive) return

        holder.pendingRunnable?.let { holder.image.removeCallbacks(it) }
        holder.pendingRunnable = null
        Glide.with(holder.image).clear(holder.image)
        holder.image.setImageDrawable(null)
        holder.startTime = SystemClock.uptimeMillis()

        fun finishAfterMinDelay(action: () -> Unit) {
            val elapsed = SystemClock.uptimeMillis() - holder.startTime
            val minDelay = 300L
            if (elapsed < minDelay) {
                val r = Runnable { action() }
                holder.image.postDelayed(r, minDelay - elapsed)
                holder.pendingRunnable = r
            } else action()
        }

        // Advanced: Priority load - URL first if priority > 0, else local
        val loadUrl = item.priority > 0 && !item.imageUrl.isNullOrBlank()
        if (loadUrl) {
            Glide.with(holder.image.context)
                .load(item.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)  // Advanced: Full caching for admin URLs
                .transition(DrawableTransitionOptions.withCrossFade(500))  // Smooth transition
                .error(item.drawableRes ?: R.drawable.sample_banner_3)  // Default to 3
                .placeholder(R.drawable.sample_banner_3)  // Loading placeholder
                .into(holder.image)
        } else {
            finishAfterMinDelay {
                holder.image.setImageResource(item.drawableRes ?: R.drawable.sample_banner_3)
            }
        }

        // Title overlay
        holder.titleOverlay?.apply {
            visibility = if (item.title.isNullOrBlank()) View.GONE else View.VISIBLE
            text = item.title
        }

        holder.itemView.setOnClickListener {
            onClick?.invoke(item)
            if (item.enableAnalytics) onAnalytics?.invoke(item)  // Admin analytics
        }
    }

    override fun onViewRecycled(holder: VH) {
        holder.pendingRunnable?.let { holder.image.removeCallbacks(it) }
        holder.pendingRunnable = null
        Glide.with(holder.image).clear(holder.image)
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = if (items.isEmpty()) 0 else Int.MAX_VALUE  // Infinite, but safe empty check

    // Advanced: Admin update with limit (3-6), filters active/priority
    fun updateBanners(newItems: List<BannerItem>, limit: Int = 4) {
        items = newItems
            .filter { it.isActive }
            .sortedByDescending { it.priority }  // Admin priority sort
            .take(limit.coerceIn(3, 6))
        if (items.size < 3) {
            // Pad with total 3 default images
            items += listOf(
                BannerItem(id = "pad_1", drawableRes = R.drawable.sample_banner_1),
                BannerItem(id = "pad_2", drawableRes = R.drawable.sample_banner_2),
                BannerItem(id = "pad_3", drawableRes = R.drawable.sample_banner_3)
            ).take(3 - items.size)
        }
        notifyDataSetChanged()
        Log.d("BannerAdapter", "Updated to ${items.size} banners (limit: $limit)")
    }
}