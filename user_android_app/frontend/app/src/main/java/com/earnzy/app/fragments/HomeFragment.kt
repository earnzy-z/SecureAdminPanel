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
import com.earnzy.app.utils.AnimationUtils
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

    private var featuresAdapter: AdminFeatureAdapter? = null
    private var bannerAdapter: BannerPageAdapter? = null

    private var currentCoins = 0
    private var autoSlideJob: Job? = null
    private var coinAnimator: ValueAnimator? = null
    private lateinit var securePrefs: SharedPreferences
    private val autoSlideMillis = 4000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSecureStorage()
        initViews(view)
        setupRecycler()
        setupBannerPager()
        setupFab()
        animateEntrance(view)
        loadHomeData()
    }

    private fun animateEntrance(view: View) {
        try {
            coinsText?.let { AnimationUtils.slideUpIn(it, delay = 0) }
            rvQuickActions?.let { AnimationUtils.slideUpIn(it, delay = 100) }
            fabQuickEarn?.let { AnimationUtils.slideUpIn(it, delay = 200) }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Animation error: ${e.message}")
        }
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
            Log.e("HomeFragment", "Security setup error: ${e.message}")
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
        lifecycleScope.launch {
            if (!isAdded) return@launch

            val adminFeatures = getMockAdminFeatures()
            featuresAdapter?.submitList(adminFeatures.filter { it.isVisible })

            try {
                val ctx = context ?: run { stopLoadingState(); return@launch }
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser ?: run { stopLoadingState(); return@launch }
                
                val idToken = try { currentUser.getIdToken(false).await().token ?: "" } catch (e: Exception) { "" }
                val deviceID = android.provider.Settings.Secure.getString(ctx.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "unknown"
                val deviceToken = "" // FCM token would go here
                val isVpn = false // VPN detection logic
                val isSslProxy = false // SSL proxy detection logic

                val responseJson = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getUserProfile(ctx, idToken, deviceID, deviceToken, isVpn, isSslProxy)
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
                Log.e("HomeFragment", "Load error: ${e.message}")
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
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jsonResponse = URL(BANNER_CMS_URL).readText()
                val banners = parseBanners(jsonResponse)
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        bannerAdapter?.submitList(banners)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Banner load error: ${e.message}")
            }
        }
    }

    private fun parseBanners(json: String): List<BannerItem> {
        return try {
            val arr = JSONArray(json)
            val banners = mutableListOf<BannerItem>()
            for (i in 0 until minOf(arr.length(), 6)) {
                val obj = arr.getJSONObject(i)
                banners.add(
                    BannerItem(
                        id = obj.optString("id", "banner_$i"),
                        imageUrl = obj.optString("image_url"),
                        clickAction = obj.optString("click_action")
                    )
                )
            }
            banners
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun setupBannerPager() {
        bannerAdapter = BannerPageAdapter { banner -> handleBannerClick(banner) }
        bannerPager?.apply {
            adapter = bannerAdapter
            setPageTransformer(ParallaxPageTransformer())
            offscreenPageLimit = 2
        }
    }

    private fun setupFab() {
        fabQuickEarn?.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "Quick Earn started!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateCoins(newCoins: Int) {
        coinAnimator?.cancel()
        coinAnimator = ValueAnimator.ofInt(currentCoins, newCoins).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                currentCoins = animation.animatedValue as Int
                coinsText?.text = "₹ $currentCoins"
            }
            start()
        }
    }

    private fun startLoadingState() {
        shimmerContainer?.startShimmer()
        shimmerContainer?.visibility = View.VISIBLE
        mainContentContainer?.visibility = View.GONE
    }

    private fun stopLoadingState() {
        shimmerContainer?.stopShimmer()
        shimmerContainer?.visibility = View.GONE
        mainContentContainer?.visibility = View.VISIBLE
    }

    private fun startAutoSlide() {
        autoSlideJob?.cancel()
        autoSlideJob = lifecycleScope.launch {
            while (isActive) {
                delay(autoSlideMillis)
                if (isAdded && bannerPager != null) {
                    val nextItem = (bannerPager!!.currentItem + 1) % (bannerAdapter?.itemCount ?: 1)
                    bannerPager?.setCurrentItem(nextItem, true)
                }
            }
        }
    }

    private fun stopAutoSlide() {
        autoSlideJob?.cancel()
    }

    private fun handleFeatureClick(feature: AdminFeatureItem) {
        Toast.makeText(context, "${feature.title} clicked!", Toast.LENGTH_SHORT).show()
    }

    private fun handleBannerClick(banner: BannerItem) {
        Toast.makeText(context, "Banner ${banner.id} clicked", Toast.LENGTH_SHORT).show()
    }
}
