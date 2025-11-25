package com.earnzy.app.fragments

import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.earnzy.app.R
import com.earnzy.app.adapters.TransactionAdapter
import com.earnzy.app.models.Transaction
import com.earnzy.app.network.FeaturesApiClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class WalletFragment : Fragment() {

    // Main Wallet Views
    private lateinit var balanceAmount: MaterialTextView
    private lateinit var walletCard: MaterialCardView
    private lateinit var btnWithdraw: MaterialButton
    private lateinit var btnHistory: MaterialButton

    // Stats Views
    private lateinit var todayStat: MaterialTextView
    private lateinit var weekStat: MaterialTextView
    private lateinit var monthStat: MaterialTextView
    private lateinit var withdrawnStat: MaterialTextView
    private lateinit var pendingStat: MaterialTextView
    private lateinit var lifetimeStat: MaterialTextView

    // Withdrawal Method Cards
    private lateinit var paypalCard: MaterialCardView
    private lateinit var bankCard: MaterialCardView
    private lateinit var giftCard: MaterialCardView
    private lateinit var cryptoCard: MaterialCardView

    // Transactions
    private lateinit var transactionsRecycler: RecyclerView
    private lateinit var seeAllTransactions: MaterialTextView
    
    // Data & Utils
    private lateinit var securePrefs: SharedPreferences
    private var currentBalance = 0
    private val transactions = mutableListOf<Transaction>()
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)
        
        initializeSecureStorage()
        initViews(view)
        setupTransactions()
        setupClickListeners()
        
        // Initial entry animation
        animateEntrance()
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        loadWalletData()
    }
    
    private fun initializeSecureStorage() {
        if (!isAdded) return
        val ctx = context ?: return
        try {
            val masterKey = MasterKey.Builder(ctx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            securePrefs = EncryptedSharedPreferences.create(
                ctx,
                "SecureEarnzyPrefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("WalletFragment", "Security setup failed", e)
        }
    }

    private fun initViews(view: View) {
        // Main Wallet
        balanceAmount = view.findViewById(R.id.balance_amount_enhanced)
        walletCard = view.findViewById(R.id.wallet_card_enhanced)
        btnWithdraw = view.findViewById(R.id.btn_withdraw_wallet)
        btnHistory = view.findViewById(R.id.btn_history_wallet)

        // Stats Grid
        todayStat = view.findViewById(R.id.today_earning_stat)
        weekStat = view.findViewById(R.id.week_earning_stat)
        monthStat = view.findViewById(R.id.month_earning_stat)
        withdrawnStat = view.findViewById(R.id.withdrawn_stat)
        pendingStat = view.findViewById(R.id.pending_stat)
        lifetimeStat = view.findViewById(R.id.lifetime_stat)

        // Methods
        paypalCard = view.findViewById(R.id.paypal_card)
        bankCard = view.findViewById(R.id.bank_card)
        giftCard = view.findViewById(R.id.giftcard_card)
        cryptoCard = view.findViewById(R.id.crypto_card)

        // Transactions
        transactionsRecycler = view.findViewById(R.id.transactions_recycler_enhanced)
        seeAllTransactions = view.findViewById(R.id.see_all_transactions)
    }

    private fun setupClickListeners() {
        btnWithdraw.setOnClickListener { 
            animateButtonPress(it)
            showWithdrawalDialog(null) // Show all options
        }

        btnHistory.setOnClickListener {
            animateButtonPress(it)
            // Navigate to full history fragment or expand list
            Toast.makeText(context, "Opening full history...", Toast.LENGTH_SHORT).show()
        }
        
        seeAllTransactions.setOnClickListener {
            Toast.makeText(context, "Loading all transactions...", Toast.LENGTH_SHORT).show()
        }

        // Specific Withdrawal Method clicks
        val methodClickListener = View.OnClickListener { view ->
            animateButtonPress(view)
            val methodType = when(view.id) {
                R.id.paypal_card -> "PayPal"
                R.id.bank_card -> "Bank Transfer"
                R.id.giftcard_card -> "Gift Card"
                R.id.crypto_card -> "Crypto"
                else -> null
            }
            if (methodType != null) {
                // In a real app, you might filter the dialog or go straight to input
                // For now, we show the dialog passing the preference
                showWithdrawalDialog(methodType)
            }
        }

        paypalCard.setOnClickListener(methodClickListener)
        bankCard.setOnClickListener(methodClickListener)
        giftCard.setOnClickListener(methodClickListener)
        cryptoCard.setOnClickListener(methodClickListener)
    }
    
    private fun animateEntrance() {
        walletCard.alpha = 0f
        walletCard.translationY = 100f
        
        walletCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
            
        // Staggered animation for method cards
        val cards = listOf(paypalCard, bankCard, giftCard, cryptoCard)
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.scaleX = 0.8f
            card.scaleY = 0.8f
            
            card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(300L + (index * 100))
                .setDuration(400)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }
    
    private fun animateButtonPress(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            .start()
    }

    private fun loadWalletData() {
        if (!isAdded) return
        val ctx = context ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return@launch
            try {
                val idToken = getIdToken()
                val deviceID = getDeviceID()
                val deviceToken = getDeviceToken()
                val isVpn = isVpn()
                val isSslProxy = isSslProxy()

                // Parallel fetching
                val profileDeferred = async(Dispatchers.IO) {
                    FeaturesApiClient.getUserProfile(ctx, idToken, deviceID, deviceToken, isVpn, isSslProxy)
                }
                val transactionsDeferred = async(Dispatchers.IO) {
                    FeaturesApiClient.getTransactionHistory(ctx, idToken, deviceID, deviceToken, 20, 0, isVpn, isSslProxy)
                }
                
                val (profileResponse, transactionsResponse) = awaitAll(profileDeferred, transactionsDeferred)

                if (!isAdded) return@launch
                
                // 1. Parse Profile & Stats
                if (profileResponse.optString("status") == "success") {
                    val user = profileResponse.optJSONObject("user")
                    if(user != null) {
                        // Main Balance
                        currentBalance = user.optInt("coins", 0)
                        animateBalance(currentBalance)

                        // Stats Calculation
                        // Assuming API returns specific fields, otherwise we calculate or default
                        val totalEarnings = user.optInt("total_earnings", 0)
                        val totalWithdrawn = user.optInt("total_withdrawn", 0)
                        val pendingAmount = user.optInt("pending_withdrawals", 0)
                        val todayEarnings = user.optInt("today_earnings", 0)
                        val weekEarnings = user.optInt("week_earnings", 0)
                        val monthEarnings = user.optInt("month_earnings", 0)

                        // Animate Stats
                        animateCounter(todayStat, todayEarnings)
                        animateCounter(weekStat, weekEarnings)
                        animateCounter(monthStat, monthEarnings)
                        animateCounter(withdrawnStat, totalWithdrawn)
                        animateCounter(pendingStat, pendingAmount)
                        animateCounter(lifetimeStat, totalEarnings)
                    }
                }
                
                // 2. Parse Transactions
                if (transactionsResponse.optString("status") == "success") {
                    val transactionsArray = transactionsResponse.optJSONArray("transactions")
                    transactions.clear()
                    
                    if (transactionsArray != null && transactionsArray.length() > 0) {
                        for (i in 0 until transactionsArray.length()) {
                            val txObj = transactionsArray.getJSONObject(i)
                            val transaction = Transaction(
                                description = txObj.optString("description", "N/A"),
                                amount = "${if (txObj.optInt("pointsDelta") > 0) "+" else ""}${txObj.optInt("pointsDelta")}",
                                timestamp = formatTimestamp(txObj.optString("createdAt")),
                                type = if (txObj.optInt("pointsDelta") > 0) "credit" else "debit"
                            )
                            transactions.add(transaction)
                        }
                        transactionAdapter.notifyDataSetChanged()
                    } else {
                        // Handle empty state if needed
                    }
                }
                
            } catch (e: Exception) {
                if (isAdded) {
                    Log.e("WalletFragment", "Error loading wallet data", e)
                    loadDefaultData() // Fallback for offline/error
                }
            }
        }
    }

    private fun animateBalance(balance: Int) {
        if (!isAdded) return
        val animator = ValueAnimator.ofInt(0, balance)
        animator.duration = 1500
        animator.addUpdateListener { animation ->
            if (!isAdded) return@addUpdateListener
            // Format with commas for currency
            balanceAmount.text = NumberFormat.getNumberInstance(Locale.US).format(animation.animatedValue)
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
    
    private fun animateCounter(textView: MaterialTextView, value: Int) {
        if (!isAdded) return
        val startVal = try {
            textView.text.toString().replace(",", "").toInt()
        } catch (e: Exception) { 0 }
        
        if (startVal == value) return

        val animator = ValueAnimator.ofInt(startVal, value)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            if (!isAdded) return@addUpdateListener
            textView.text = NumberFormat.getNumberInstance(Locale.US).format(animation.animatedValue)
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun loadDefaultData() {
        if (!isAdded) return
        balanceAmount.text = "0"
        if (transactions.isEmpty()) {
            // Optional: Add skeleton or empty state here
        }
    }

    private fun setupTransactions() {
        if (!isAdded) return
        transactionsRecycler.layoutManager = LinearLayoutManager(context)
        // Disable nested scrolling for the recycler view to let the NestedScrollView handle it
        transactionsRecycler.isNestedScrollingEnabled = false
        transactionAdapter = TransactionAdapter(transactions)
        transactionsRecycler.adapter = transactionAdapter
    }
    
    private fun showWithdrawalDialog(preSelectedType: String?) {
        if (!isAdded) return
        val ctx = context ?: return
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getWithdrawalMethods(
                        ctx, getIdToken(), getDeviceID(), getDeviceToken(), isVpn(), isSslProxy()
                    )
                }
                
                if (!isAdded) return@launch
                
                if (response.optString("status") == "success") {
                    val methodsArray = response.getJSONArray("methods")
                    val methodNames = mutableListOf<String>()
                    val methodObjects = mutableListOf<JSONObject>()
                    
                    // Filter logic if a specific card was clicked, or show all
                    for (i in 0 until methodsArray.length()) {
                        val m = methodsArray.getJSONObject(i)
                        // If preSelectedType is provided, check if name matches partially
                        if (preSelectedType == null || m.getString("name").contains(preSelectedType, ignoreCase = true)) {
                            methodNames.add("${m.getString("name")} (Min: ${m.getInt("minAmount")})")
                            methodObjects.add(m)
                        }
                    }
                    
                    // If filtering resulted in empty list (e.g. API name doesn't match UI name), fallback to all
                    if (methodNames.isEmpty() && preSelectedType != null) {
                        for (i in 0 until methodsArray.length()) {
                            val m = methodsArray.getJSONObject(i)
                            methodNames.add(m.getString("name"))
                            methodObjects.add(m)
                        }
                    }

                    MaterialAlertDialogBuilder(ctx)
                        .setTitle(if(preSelectedType != null) "Withdraw via $preSelectedType" else "Select Withdrawal Method")
                        .setItems(methodNames.toTypedArray()) { _, which ->
                            processWithdrawal(methodObjects[which])
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    Toast.makeText(ctx, "Failed to load methods", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Connection error", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun processWithdrawal(method: JSONObject) {
        if (!isAdded) return
        val ctx = context ?: return
        val methodId = method.getString("id")
        val minAmount = method.getInt("minAmount")
        val maxAmount = method.getInt("maxAmount")
        
        val inputView = layoutInflater.inflate(R.layout.dialog_withdrawal_input, null)
        val amountInput = inputView.findViewById<android.widget.EditText>(R.id.amount_input)
        val accountInput = inputView.findViewById<android.widget.EditText>(R.id.account_input)
        
        // Pre-fill hints based on method
        if(method.getString("name").contains("PayPal")) {
            accountInput.hint = "Enter PayPal Email"
        } else if (method.getString("name").contains("Bank")) {
            accountInput.hint = "Enter Account Number / IBAN"
        }

        MaterialAlertDialogBuilder(ctx)
            .setTitle("Withdraw Details")
            .setView(inputView)
            .setPositiveButton("Request") { _, _ ->
                val amount = amountInput.text.toString().toIntOrNull() ?: 0
                val account = accountInput.text.toString()
                
                if (amount < minAmount) {
                    Toast.makeText(ctx, "Minimum amount is $minAmount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (amount > currentBalance) {
                    Toast.makeText(ctx, "Insufficient balance", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (account.isEmpty()) {
                    Toast.makeText(ctx, "Account details required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                submitWithdrawal(methodId, amount, account)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitWithdrawal(methodId: String, amount: Int, account: String) {
        val ctx = context ?: return
        lifecycleScope.launch {
            try {
                val accountDetails = JSONObject().put("account", account)
                val response = withContext(Dispatchers.IO) {
                    FeaturesApiClient.requestWithdrawal(
                        ctx, getIdToken(), getDeviceID(), getDeviceToken(), methodId, amount, accountDetails, isVpn(), isSslProxy()
                    )
                }
                
                if (response.optString("status") == "success") {
                    Toast.makeText(ctx, "Withdrawal Requested Successfully!", Toast.LENGTH_LONG).show()
                    loadWalletData() // Refresh balance
                } else {
                    Toast.makeText(ctx, response.optString("message", "Failed"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Request failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            // Simple truncation for display, use SimpleDateFormat for real formatting
            timestamp.split("T")[0]
        } catch (e: Exception) { timestamp }
    }

    // --- Helper Functions for Security & Auth ---
    private suspend fun getIdToken(): String = withContext(Dispatchers.IO) {
        try { FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: "" } 
        catch (e: Exception) { "" }
    }
    private fun getDeviceID(): String = securePrefs.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs.getBoolean("isVpn", false)
    private fun isSslProxy(): Boolean = securePrefs.getBoolean("isSslProxy", false)
}