package com.earnzy.app.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.earnzy.app.R
import com.earnzy.app.adapters.EarnTaskAdapter
import com.earnzy.app.models.EarnTask
import com.earnzy.app.network.FeaturesApiClient
import com.earnzy.app.utils.AnimationUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import kotlin.math.abs

class EarnFragment : Fragment() {

    // UI Components (nullable to respect Fragment view lifecycle)
    private var searchInput: EditText? = null
    private var filterButton: MaterialCardView? = null
    private var sortButton: MaterialButton? = null
    private var featuredPager: ViewPager2? = null
    private var categoriesChipGroup: ChipGroup? = null
    private var opportunitiesRecycler: RecyclerView? = null
    private var loadingIndicator: CircularProgressIndicator? = null
    private var emptyStateLayout: LinearLayout? = null
    private var fabRefresh: FloatingActionButton? = null

    // Data & State
    private var securePrefs: SharedPreferences? = null
    private var taskAdapter: EarnTaskAdapter? = null
    private var featuredAdapter: EarnTaskAdapter? = null

    private val allTasks = mutableListOf<EarnTask>()
    private val filteredTasks = mutableListOf<EarnTask>()
    private val featuredTasks = mutableListOf<EarnTask>()

    private var currentCategory: String = "All"
    private var currentSearchQuery: String = ""
    private var currentSortMode: SortMode = SortMode.RECOMMENDED

    enum class SortMode { RECOMMENDED, REWARD_HIGH, REWARD_LOW, TIME_SHORT }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_earn, container, false)

        initializeSecureStorage()
        initViews(view)
        setupAdapters()
        setupListeners()
        setupViewPagerAnimation()

        // Initial Load
        loadTasksFromBackend()
        
        // Add entrance animations
        animateEntrance(view)

        return view
    }
    
    private fun animateEntrance(view: View) {
        try {
            AnimationUtils.slideUpIn(searchInput ?: return, duration = 300, delay = 0)
            AnimationUtils.slideUpIn(featuredPager ?: return, duration = 400, delay = 100)
            AnimationUtils.slideUpIn(opportunitiesRecycler ?: return, duration = 400, delay = 200)
        } catch (e: Exception) {
            Log.e("EarnFragment", "Animation error", e)
        }
    }

    override fun onResume() {
        super.onResume()
        if (securePrefs == null) {
            initializeSecureStorage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Detach adapters to avoid leaks
        opportunitiesRecycler?.adapter = null
        featuredPager?.adapter = null

        // Null out adapters and views to respect view lifecycle
        taskAdapter = null
        featuredAdapter = null

        searchInput = null
        filterButton = null
        sortButton = null
        featuredPager = null
        categoriesChipGroup = null
        opportunitiesRecycler = null
        loadingIndicator = null
        emptyStateLayout = null
        fabRefresh = null
    }

    private fun initViews(view: View) {
        searchInput = view.findViewById(R.id.search_input)
        filterButton = view.findViewById(R.id.filter_button)
        sortButton = view.findViewById(R.id.sort_button)
        featuredPager = view.findViewById(R.id.featured_pager)
        categoriesChipGroup = view.findViewById(R.id.categories_chip_group)
        opportunitiesRecycler = view.findViewById(R.id.opportunities_recycler)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        emptyStateLayout = view.findViewById(R.id.empty_state)
        fabRefresh = view.findViewById(R.id.fab_refresh)
    }

    private fun initializeSecureStorage() {
        val context = context ?: return
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            securePrefs = EncryptedSharedPreferences.create(
                context,
                "SecureEarnzyPrefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("EarnFragment", "Security setup failed", e)
            if (isAdded) {
                Toast.makeText(requireContext(), "Security initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAdapters() {
        if (!isAdded) return
        val ctx = requireContext()

        // Adapter for the main list of opportunities
        taskAdapter = EarnTaskAdapter(filteredTasks) { task ->
            completeTask(task)
        }
        opportunitiesRecycler?.layoutManager = LinearLayoutManager(ctx)
        opportunitiesRecycler?.adapter = taskAdapter

        // Adapter for the featured tasks ViewPager
        featuredAdapter = EarnTaskAdapter(featuredTasks) { task ->
            Toast.makeText(ctx, "Featured: ${task.title}", Toast.LENGTH_SHORT).show()
        }
        featuredPager?.adapter = featuredAdapter

        // If data already loaded earlier, render now
        populateFeaturedList()
        applyFiltersAndSort()
    }

    private fun setupViewPagerAnimation() {
        featuredPager?.clipToPadding = false
        featuredPager?.clipChildren = false
        featuredPager?.offscreenPageLimit = 3
        val pagerView = featuredPager?.getChildAt(0)
        if (pagerView is RecyclerView) {
            pagerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        featuredPager?.setPageTransformer(compositePageTransformer)
    }

    private fun setupListeners() {
        searchInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim().orEmpty()
                applyFiltersAndSort()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        categoriesChipGroup?.setOnCheckedChangeListener { _, checkedId ->
            currentCategory = when (checkedId) {
                R.id.chip_surveys -> "surveys"
                R.id.chip_videos -> "videos"
                R.id.chip_offers -> "offers"
                R.id.chip_tasks -> "tasks"
                R.id.chip_games -> "games"
                else -> "All"
            }
            applyFiltersAndSort()
        }

        sortButton?.setOnClickListener {
            showSortDialog()
        }

        filterButton?.setOnClickListener {
            if (isAdded) {
                Toast.makeText(requireContext(), "Advanced filters coming soon!", Toast.LENGTH_SHORT).show()
            }
        }

        fabRefresh?.setOnClickListener {
            loadTasksFromBackend()
        }
    }

    private fun showSortDialog() {
        if (!isAdded) return
        val options = arrayOf("Recommended", "Highest Reward", "Lowest Reward", "Shortest Time")
        val currentSelection = when (currentSortMode) {
            SortMode.RECOMMENDED -> 0
            SortMode.REWARD_HIGH -> 1
            SortMode.REWARD_LOW -> 2
            SortMode.TIME_SHORT -> 3
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sort Opportunities")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                currentSortMode = when (which) {
                    1 -> SortMode.REWARD_HIGH
                    2 -> SortMode.REWARD_LOW
                    3 -> SortMode.TIME_SHORT
                    else -> SortMode.RECOMMENDED
                }
                applyFiltersAndSort()
                sortButton?.text = options[which]
                dialog.dismiss()
            }
            .show()
    }

    private fun loadTasksFromBackend() {
        if (!isAdded) return

        loadingIndicator?.visibility = View.VISIBLE
        opportunitiesRecycler?.visibility = View.GONE
        emptyStateLayout?.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = FeaturesApiClient.getEarnTasks(
                    requireContext(),
                    getIdToken(),
                    getDeviceID(),
                    getDeviceToken(),
                    isVpn(),
                    isSslProxy()
                )

                if (response.getString("status") == "success") {
                    parseTasks(response.getJSONArray("tasks"))
                } else {
                    Log.w("EarnFragment", "API status not success: ${response.optString("message")}")
                    loadDefaultTasksSafely()
                }
            } catch (e: Exception) {
                Log.e("EarnFragment", "API Error", e)
                loadDefaultTasksSafely()
            } finally {
                if (isAdded) {
                    loadingIndicator?.visibility = View.GONE
                    populateFeaturedList()
                    applyFiltersAndSort()
                }
            }
        }
    }

    private fun parseTasks(tasksArray: JSONArray) {
        allTasks.clear()
        for (i in 0 until tasksArray.length()) {
            val taskObj = tasksArray.getJSONObject(i)
            allTasks.add(
                EarnTask(
                    id = taskObj.getInt("id"),
                    title = taskObj.getString("title"),
                    reward = taskObj.getString("reward"),
                    duration = taskObj.optString("duration", "N/A"),
                    category = taskObj.optString("category", "general"),
                    completed = taskObj.optBoolean("completed", false),
                    iconUrl = taskObj.optString("iconUrl", ""),
                    actionUrl = taskObj.optString("actionUrl", "")
                )
            )
        }
    }

    private fun loadDefaultTasksSafely() {
        if (!isAdded) return

        Toast.makeText(requireContext(), "Showing sample tasks", Toast.LENGTH_SHORT).show()
        allTasks.clear()
        allTasks.addAll(listOf(
            EarnTask(1, "Watch & Earn", "+50 ₹", "5 min", "videos"),
            EarnTask(2, "Market Survey", "+200 ₹", "10 min", "surveys"),
            EarnTask(3, "Install Game", "+500 ₹", "Instant", "games"),
            EarnTask(4, "Play Chess", "+150 ₹", "15 min", "games"),
            EarnTask(5, "Refer Friend", "+100 ₹", "Invite", "tasks"),
            EarnTask(6, "Product Review", "+75 ₹", "2 min", "surveys"),
            EarnTask(7, "Daily Login", "+50 ₹", "Daily", "tasks"),
            EarnTask(8, "Shop Online", "+300 ₹", "Varies", "offers")
        ))
    }

    private fun populateFeaturedList() {
        val adapter = featuredAdapter ?: return
        if (!isAdded) return

        val sortedByReward = allTasks.sortedByDescending { parseRewardValue(it.reward) }
        featuredTasks.clear()
        featuredTasks.addAll(sortedByReward.take(5))

        featuredPager?.visibility = if (featuredTasks.isEmpty()) View.GONE else View.VISIBLE
        adapter.notifyDataSetChanged()
    }

    private fun applyFiltersAndSort() {
        val adapter = taskAdapter ?: return
        if (!isAdded) return

        val result = allTasks.filter { task ->
            val matchesCategory = if (currentCategory == "All") true else {
                if (currentCategory == "offers") {
                    task.category == "apps" || task.category == "shopping" || task.category == "offers"
                } else {
                    task.category.equals(currentCategory, ignoreCase = true)
                }
            }
            val matchesSearch = task.title.contains(currentSearchQuery, ignoreCase = true) ||
                                task.category.contains(currentSearchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        val sortedResult = when (currentSortMode) {
            SortMode.REWARD_HIGH -> result.sortedByDescending { parseRewardValue(it.reward) }
            SortMode.REWARD_LOW -> result.sortedBy { parseRewardValue(it.reward) }
            SortMode.TIME_SHORT -> result.sortedBy { parseDurationValue(it.duration) }
            SortMode.RECOMMENDED -> result
        }

        filteredTasks.clear()
        filteredTasks.addAll(sortedResult)
        adapter.notifyDataSetChanged()

        if (filteredTasks.isEmpty()) {
            emptyStateLayout?.visibility = View.VISIBLE
            opportunitiesRecycler?.visibility = View.GONE
        } else {
            emptyStateLayout?.visibility = View.GONE
            opportunitiesRecycler?.visibility = View.VISIBLE
        }
    }

    private fun parseRewardValue(reward: String): Int {
        return try {
            reward.replace(Regex("[^0-9]"), "").toInt()
        } catch (e: NumberFormatException) { 0 }
    }

    private fun parseDurationValue(duration: String): Int {
        return try {
            if (duration.contains("Instant", true)) 0
            else duration.replace(Regex("[^0-9]"), "").toInt()
        } catch (e: NumberFormatException) { 999 }
    }

    private fun completeTask(task: EarnTask) {
        if (task.completed) {
            if (isAdded) {
                Toast.makeText(requireContext(), "Already completed!", Toast.LENGTH_SHORT).show()
            }
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            if (!isAdded) return@launch
            try {
                val response = FeaturesApiClient.completeTask(
                    requireContext(),
                    getIdToken(),
                    getDeviceID(),
                    getDeviceToken(),
                    task.id.toString(),
                    isVpn(),
                    isSslProxy()
                )

                if (response.getString("status") == "success") {
                    Toast.makeText(requireContext(), "Reward Added: ${task.reward}", Toast.LENGTH_LONG).show()
                    task.completed = true
                    allTasks.find { it.id == task.id }?.completed = true
                    taskAdapter?.notifyDataSetChanged()
                    featuredAdapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), response.optString("message", "Failed"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EarnFragment", "Network error", e)
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getIdToken(): String = try {
        FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: ""
    } catch (e: Exception) {
        Log.w("EarnFragment", "Could not get ID token", e)
        ""
    }

    private fun getDeviceID(): String = securePrefs?.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs?.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs?.getBoolean("isVpn", false) ?: false
    private fun isSslProxy(): Boolean = securePrefs?.getBoolean("isSslProxy", false) ?: false
}