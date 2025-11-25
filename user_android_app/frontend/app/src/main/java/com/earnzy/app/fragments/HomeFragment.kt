package com.earnzy.app.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.earnzy.app.R
import com.earnzy.app.adapters.AdminFeatureAdapter
import com.earnzy.app.adapters.BannerPageAdapter
import com.earnzy.app.models.AdminFeatureItem
import com.earnzy.app.models.BannerItem
import com.earnzy.app.network.FeaturesApiClient
import com.earnzy.app.util.ParallaxPageTransformer
import com.earnzy.app.utils.AnimationUtils
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

class HomeFragment : Fragment() {

    private var shimmerContainer: ShimmerFrameLayout? = null
    private var mainContent: View? = null
    private var bannerPager: ViewPager2? = null
    private var rvQuickActions: RecyclerView? = null
    private var coinsText: MaterialTextView? = null
    private var greetingText: MaterialTextView? = null
    private var userNameText: MaterialTextView? = null
    private var streakText: MaterialTextView? = null
    private var fab: ExtendedFloatingActionButton? = null

    private var featuresAdapter: AdminFeatureAdapter? = null
    private var bannerAdapter: BannerPageAdapter? = null
    private var autoSlideJob: Job? = null
    private var currentCoins = 0
    private val BANNER_URL = "https://banner-cms-worker.dev-prashant-15.workers.dev"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupBannerPager()
        setupClickListeners()
        animateEntrance()
        loadHomeData()
    }

    private fun initViews(view: View) {
        shimmerContainer = view.findViewById(R.id.shimmer_view_container)
        mainContent = view.findViewById(R.id.main_content_container)
        bannerPager = view.findViewById(R.id.banner_pager_enhanced)
        rvQuickActions = view.findViewById(R.id.rv_quick_actions)
        coinsText = view.findViewById(R.id.coins_text)
        greetingText = view.findViewById(R.id.greeting_text)
        userNameText = view.findViewById(R.id.user_name_text)
        streakText = view.findViewById(R.id.streak_text)
        fab = view.findViewById(R.id.fab_quick_earn_enhanced)
        setupGreeting()
    }

    private fun setupGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        greetingText?.text = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        userNameText?.text = FirebaseAuth.getInstance().currentUser?.displayName ?: "Earner"
    }

    private fun setupRecyclerView() {
        featuresAdapter = AdminFeatureAdapter { feature -> 
            AnimationUtils.pressAnimation(fab ?: return@AdminFeatureAdapter)
            handleFeatureClick(feature)
        }
        rvQuickActions?.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = featuresAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupBannerPager() {
        bannerAdapter = BannerPageAdapter(emptyList()) { banner -> 
            Toast.makeText(context, "Banner clicked: ${banner.id}", Toast.LENGTH_SHORT).show()
        }
        bannerPager?.apply {
            adapter = bannerAdapter
            setPageTransformer(ParallaxPageTransformer())
            offscreenPageLimit = 2
        }
    }

    private fun setupClickListeners() {
        fab?.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "Quick Earn - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateEntrance() {
        try {
            coinsText?.let { AnimationUtils.slideUpIn(it, delay = 0) }
            rvQuickActions?.let { AnimationUtils.slideUpIn(it, delay = 100) }
            fab?.let { AnimationUtils.slideUpIn(it, delay = 200) }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Animation error: ${e.message}")
        }
    }

    private fun loadHomeData() {
        showLoading()
        lifecycleScope.launch {
            if (!isAdded) return@launch

            val mockFeatures = getMockFeatures()
            featuresAdapter?.submitList(mockFeatures)

            try {
                val ctx = context ?: run { hideLoading(); return@launch }
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser ?: run { hideLoading(); return@launch }

                val idToken = try { user.getIdToken(false).await().token ?: "" } catch (e: Exception) { "" }
                val deviceID = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val deviceToken = ""
                val isVpn = false
                val isSslProxy = false

                val response = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getUserProfile(ctx, idToken, deviceID, deviceToken, isVpn, isSslProxy)
                }

                if (!isAdded) return@launch

                val status = response?.optString("status")
                if (status == "success") {
                    val userObj = response.optJSONObject("user")
                    val coins = userObj?.optInt("coins", 0) ?: 0
                    val name = userObj?.optString("name", "User") ?: "User"
                    userNameText?.text = name
                    animateCoins(coins)
                } else {
                    Log.d("HomeFragment", "Using mock data due to API response")
                }

                loadBanners()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading home data: ${e.message}")
                Toast.makeText(context, "Loaded mock data", Toast.LENGTH_SHORT).show()
            } finally {
                hideLoading()
            }
        }
    }

    private fun loadBanners() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jsonStr = URL(BANNER_URL).readText()
                val banners = parseBanners(jsonStr)
                withContext(Dispatchers.Main) {
                    if (isAdded && bannerAdapter != null) {
                        bannerAdapter?.updateBanners(banners)
                        startAutoSlide()
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Banner error: ${e.message}")
            }
        }
    }

    private fun parseBanners(json: String): List<BannerItem> {
        return try {
            val arr = JSONArray(json)
            val items = mutableListOf<BannerItem>()
            for (i in 0 until minOf(arr.length(), 6)) {
                val obj = arr.getJSONObject(i)
                items.add(
                    BannerItem(
                        id = obj.optString("id", "banner_$i"),
                        imageUrl = obj.optString("image_url"),
                        clickAction = obj.optString("click_action"),
                        isActive = obj.optBoolean("is_active", true)
                    )
                )
            }
            items
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun animateCoins(newCoins: Int) {
        ValueAnimator.ofInt(currentCoins, newCoins).apply {
            duration = 800
            addUpdateListener { animation ->
                currentCoins = animation.animatedValue as Int
                coinsText?.text = "₹ $currentCoins"
            }
            start()
        }
    }

    private fun getMockFeatures(): List<AdminFeatureItem> = listOf(
        AdminFeatureItem(
            id = "daily", title = "Daily Bonus", lottieUrl = "https://assets10.lottiefiles.com/packages/lf20_w51pcehl.json",
            clickAction = "OPEN_DAILY", isVisible = true, bgColorStart = "#FF9966", bgColorEnd = "#FF5E62", subtitle = "Claim Now", rewardText = "+100"
        ),
        AdminFeatureItem(
            id = "spin", title = "Spin Wheel", lottieUrl = "https://assets10.lottiefiles.com/packages/lf20_tivq6kxx.json",
            clickAction = "OPEN_SPIN", isVisible = true, bgColorStart = "#4facfe", bgColorEnd = "#00f2fe", subtitle = "Luck Awaits", rewardText = "Up to 500"
        ),
        AdminFeatureItem(
            id = "scratch", title = "Scratch Card", lottieUrl = "https://assets2.lottiefiles.com/private_files/lf30_hsabbeks.json",
            clickAction = "OPEN_SCRATCH", isVisible = true, bgColorStart = "#43e97b", bgColorEnd = "#38f9d7", subtitle = "Instant Win", rewardText = "+50"
        ),
        AdminFeatureItem(
            id = "video", title = "Watch Video", lottieUrl = "https://assets5.lottiefiles.com/packages/lf20_bXRg9q.json",
            clickAction = "OPEN_VIDEO", isVisible = true, bgColorStart = "#fa709a", bgColorEnd = "#fee140", subtitle = "Quick Earn", rewardText = "+20"
        )
    )

    private fun handleFeatureClick(feature: AdminFeatureItem) {
        when (feature.clickAction) {
            "OPEN_DAILY" -> Toast.makeText(context, "Daily Bonus: ${feature.rewardText}", Toast.LENGTH_SHORT).show()
            "OPEN_SPIN" -> Toast.makeText(context, "Spin Wheel: ${feature.rewardText}", Toast.LENGTH_SHORT).show()
            "OPEN_SCRATCH" -> Toast.makeText(context, "Scratch Card: ${feature.rewardText}", Toast.LENGTH_SHORT).show()
            "OPEN_VIDEO" -> Toast.makeText(context, "Watch Video: ${feature.rewardText}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAutoSlide() {
        autoSlideJob?.cancel()
        autoSlideJob = lifecycleScope.launch {
            while (isActive && isAdded && bannerAdapter != null) {
                delay(4500)
                if (isAdded) {
                    val nextItem = (bannerPager?.currentItem ?: 0) + 1
                    val itemCount = bannerAdapter?.itemCount ?: 1
                    bannerPager?.setCurrentItem(nextItem % itemCount, true)
                }
            }
        }
    }

    private fun showLoading() {
        shimmerContainer?.startShimmer()
        shimmerContainer?.visibility = View.VISIBLE
        mainContent?.visibility = View.GONE
    }

    private fun hideLoading() {
        shimmerContainer?.stopShimmer()
        shimmerContainer?.visibility = View.GONE
        mainContent?.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        startAutoSlide()
    }

    override fun onPause() {
        super.onPause()
        autoSlideJob?.cancel()
        shimmerContainer?.stopShimmer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoSlideJob?.cancel()
    }
}
