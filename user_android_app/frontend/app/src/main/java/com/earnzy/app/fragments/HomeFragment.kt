// fragments/HomeFragment.kt
package com.earnzy.app.fragments

import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.earnzy.app.R
import com.earnzy.app.adapters.AdminFeatureAdapter
import com.earnzy.app.adapters.BannerPageAdapter
import com.earnzy.app.models.AdminFeatureItem
import com.earnzy.app.models.BannerItem
import com.earnzy.app.models.HomeResponse
import com.earnzy.app.network.FeaturesApiClient
import com.earnzy.app.util.ParallaxPageTransformer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
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

    // UI Views
    private var shimmerContainer: ShimmerFrameLayout? = null
    private var mainContentContainer: View? = null
    private var bannerPager: ViewPager2? = null
    private var rvQuickActions: RecyclerView? = null
    private var coinsText: MaterialTextView? = null
    private var profileAvatar: LottieAnimationView? = null
    private var greetingText: MaterialTextView? = null
    private var userNameText: MaterialTextView? = null
    private var notificationBadge: View? = null
    private var streakText: MaterialTextView? = null
    private var fabQuickEarn: ExtendedFloatingActionButton? = null
    private var BANNER_CMS_URL = "https://banner-cms-worker.dev-prashant-15.workers.dev"

    // Adapters
    private var featuresAdapter: AdminFeatureAdapter? = null
    private var bannerAdapter: BannerPageAdapter? = null

    // State
    private var currentCoins = 0
    private var autoSlideJob: Job? = null
    private var coinAnimator: ValueAnimator? = null
    private lateinit var securePrefs: SharedPreferences
    private val autoSlideMillis = 4000L

    

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initializeSecureStorage()
        initViews(view)
        setupRecycler()
        setupBannerPager()
        setupFab()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadHomeData()
        loadBanners()
        startAutoSlide()
    }

    override fun onPause() {
        super.onPause()
        stopAutoSlide()
    }

    private fun initializeSecureStorage() {
        if (!isAdded || context == null) return
        val ctx = requireContext()
        try {
            val masterKey = MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            securePrefs = EncryptedSharedPreferences.create(
                ctx, "SecureEarnzyPrefs", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Security setup failed", e)
        }
    }

    private fun initViews(view: View) {
        shimmerContainer = view.findViewById(R.id.shimmer_view_container)
        mainContentContainer = view.findViewById(R.id.main_content_container)
        bannerPager = view.findViewById(R.id.banner_pager_enhanced)
        rvQuickActions = view.findViewById(R.id.rv_quick_actions)
        coinsText = view.findViewById(R.id.coins_text)
        profileAvatar = view.findViewById(R.id.profile_avatar)
        greetingText = view.findViewById(R.id.greeting_text)
        userNameText = view.findViewById(R.id.user_name_text)
        notificationBadge = view.findViewById(R.id.notification_badge)
        streakText = view.findViewById(R.id.streak_text)
        fabQuickEarn = view.findViewById(R.id.fab_quick_earn_enhanced)
        setupGreeting()
    }

    private fun setupGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        greetingText?.text = when (hour) {
            in 0..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
        userNameText?.text = FirebaseAuth.getInstance().currentUser?.displayName ?: "Earner"
    }

    private fun setupRecycler() {
        featuresAdapter = AdminFeatureAdapter { feature -> handleFeatureClick(feature) }
        rvQuickActions?.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = featuresAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun loadHomeData() {
        startLoadingState()
        viewLifecycleOwner.lifecycleScope.launch {
            if (!isAdded) return@launch

            val adminFeatures = getMockAdminFeatures()
            featuresAdapter?.submitList(adminFeatures.filter { it.isVisible })

            try {
                val ctx = context ?: run { stopLoadingState(); return@launch }
                val responseJson = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getUserProfile(ctx, getIdToken(), getDeviceID(), getDeviceToken(), isVpn(), isSslProxy())
                }

                if (!isAdded) return@launch

                val response = HomeResponse.fromJson(responseJson)

                if (response != null) {
                    response.user?.let { user ->
                        val coins = user.optInt("coins", 0)
                        val name = user.optString("name", "User")
                        userNameText?.text = name
                        animateCoins(coins)
                    }

                } else {
                    Log.e("HomeFragment", "API failed, using mocks")
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Load error", e)
            } finally {
                stopLoadingState()
            }
        }
    }

    private fun getMockAdminFeatures(): List<AdminFeatureItem> = listOf(
        AdminFeatureItem(
            id = "daily", title = "Daily Bonus", lottieUrl = "https://assets10.lottiefiles.com/packages/lf20_w51pcehl.json",
            clickAction = "OPEN_DAILY", isVisible = true, bgColorStart = "#FF9966", bgColorEnd = "#FF5E62",
            subtitle = "Claim Now", rewardText = "+100 Coins"
        ),
        AdminFeatureItem(
            id = "spin", title = "Spin Wheel", lottieUrl = "https://assets10.lottiefiles.com/packages/lf20_tivq6kxx.json",
            clickAction = "OPEN_SPIN", isVisible = true, bgColorStart = "#4facfe", bgColorEnd = "#00f2fe",
            subtitle = "Luck Awaits", rewardText = "Up to 500"
        ),
        AdminFeatureItem(
            id = "scratch", title = "Scratch Card", lottieUrl = "https://assets2.lottiefiles.com/private_files/lf30_hsabbeks.json",
            clickAction = "OPEN_SCRATCH", isVisible = true, bgColorStart = "#43e97b", bgColorEnd = "#38f9d7",
            subtitle = "Instant Win", rewardText = "+50 Coins"
        ),
        AdminFeatureItem(
            id = "video", title = "Watch Video", lottieUrl = "https://assets5.lottiefiles.com/packages/lf20_bXRg9q.json",
            clickAction = "OPEN_VIDEO", isVisible = true, bgColorStart = "#fa709a", bgColorEnd = "#fee140",
            subtitle = "Quick Earn", rewardText = "+20 Coins"
        )
    )

    private fun loadBanners() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jsonResponse = URL(BANNER_CMS_URL).readText()
                val banners = parseBanners(jsonResponse)
                withContext(Dispatchers.Main) {
                    bannerAdapter?.let {
                        it.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to load banners: ${e.message}")
            }
        }
    }

    private fun parseBanners(json: String): List<BannerItem> {
        val banners = mutableListOf<BannerItem>()
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val bannerObject = jsonArray.getJSONObject(i)
            banners.add(
                BannerItem(
                    id = bannerObject.getString("id"),
                    imageUrl = bannerObject.getString("imageUrl"),
                    clickAction = bannerObject.getString("clickAction"),
                    deepLink = bannerObject.optString("deepLink", null),
                    title = bannerObject.getString("title")
                )
            )
        }
        return banners
    }

    private fun handleFeatureClick(item: AdminFeatureItem) {
        when (item.clickAction) {
            "OPEN_DAILY" -> showLimitReachedBottomSheet()
            "OPEN_SPIN" -> Toast.makeText(context, "Spin Wheel Loading... ${item.rewardText}", Toast.LENGTH_SHORT).show()
            "OPEN_SCRATCH" -> Toast.makeText(context, "Scratch Card Loading... ${item.subtitle}", Toast.LENGTH_SHORT).show()
            "OPEN_VIDEO" -> Toast.makeText(context, "Loading Video Ad... Earn ${item.rewardText}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleBannerClick(banner: BannerItem) {
        when (banner.clickAction) {
            "OPEN_OFFER" -> Toast.makeText(context, "Opening Offer: ${banner.title}", Toast.LENGTH_SHORT).show()
            "OPEN_WEB" -> {
                banner.deepLink?.let { link ->
                    Toast.makeText(context, "Navigating to $link", Toast.LENGTH_SHORT).show()
                    // TODO: Launch URL
                }
            }
            "OPEN_EVENT" -> showQuickEarnOptions()
            else -> Toast.makeText(context, "Banner Tapped: ${banner.id}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLoadingState() {
        shimmerContainer?.apply { visibility = View.VISIBLE; startShimmer() }
        mainContentContainer?.visibility = View.GONE
        fabQuickEarn?.hide()
    }

    private fun stopLoadingState() {
        shimmerContainer?.let {
            if (it.visibility == View.GONE) return
            it.stopShimmer()
            it.visibility = View.GONE
        }
        mainContentContainer?.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(600).start()
        }
        fabQuickEarn?.show()
    }

    private fun animateCoins(targetValue: Int) {
        coinAnimator?.cancel()
        val startValue = currentCoins
        coinAnimator = ValueAnimator.ofInt(startValue, targetValue).apply {
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                if (isAdded) coinsText?.text = "${animation.animatedValue} Coins"
            }
            start()
        }
        currentCoins = targetValue
    }

    private fun setupBannerPager() {
        val pager = bannerPager ?: return
        bannerAdapter = BannerPageAdapter(emptyList()) { banner -> handleBannerClick(banner) }

        pager.apply {
            adapter = bannerAdapter
            offscreenPageLimit = 3
            getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER
            setPageTransformer(ParallaxPageTransformer())
        }
    }

    private fun setupFab() {
        fabQuickEarn?.setOnClickListener { showQuickEarnOptions() }
    }

    private fun showQuickEarnOptions() {
        context ?: return
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_quick_earn, null)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun showLimitReachedBottomSheet() {
        context ?: return
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_limit_reached, null)
        view.findViewById<MaterialButton>(R.id.close_button)?.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun startAutoSlide() {
        stopAutoSlide()
        autoSlideJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(autoSlideMillis)
                bannerPager?.let { pager ->
                    pager.setCurrentItem(pager.currentItem + 1, true)
                }
            }
        }
    }

    private fun stopAutoSlide() {
        autoSlideJob?.cancel()
        autoSlideJob = null
    }

    private suspend fun getIdToken(): String = withContext(Dispatchers.IO) {
        try { FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: "" } catch (e: Exception) { "" }
    }

    private fun getDeviceID(): String = securePrefs.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs.getBoolean("isVpn", false)
    private fun isSslProxy(): Boolean = securePrefs.getBoolean("isSslProxy", false)
}