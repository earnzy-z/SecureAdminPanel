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
import com.earnzy.app.utils.AnimationUtils
import com.earnzy.app.utils.ShimmerHelper
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

    private lateinit var securePrefs: SharedPreferences
    private var currentBalance = 0
    private val transactions = mutableListOf<Transaction>()
    private lateinit var transactionAdapter: TransactionAdapter
    
    // View References
    private lateinit var walletBalanceText: MaterialTextView
    private lateinit var btnWithdraw: MaterialButton
    private lateinit var btnHistory: MaterialButton
    private lateinit var transactionsRecycler: RecyclerView
    
    // Withdrawal Method Cards
    private lateinit var paypalCard: MaterialCardView
    private lateinit var bankCard: MaterialCardView
    private lateinit var giftCardCard: MaterialCardView
    private lateinit var cryptoCard: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wallet_professional, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeSecureStorage()
        initViews(view)
        setupTransactions()
        setupClickListeners()
        animateEntranceSmooth(view)
        loadWalletData()
    }
    
    private fun animateEntranceSmooth(view: View) {
        try {
            val mainCard = view.findViewById<View>(R.id.wallet_balance)
            val statsSection = view.findViewById<MaterialButton>(R.id.btn_withdraw)
            val recyclerView = view.findViewById<RecyclerView>(R.id.transactions_recycler)
            
            if (mainCard != null) {
                AnimationUtils.slideUpIn(mainCard, delay = 0)
            }
            if (statsSection != null) {
                AnimationUtils.slideUpIn(statsSection, delay = 100)
            }
            if (recyclerView != null) {
                AnimationUtils.slideUpIn(recyclerView, delay = 200)
            }
        } catch (e: Exception) {
            Log.e("WalletFragment", "Animation setup error: ${e.message}")
        }
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
            Log.e("WalletFragment", "Security setup failed: ${e.message}")
        }
    }

    private fun initViews(view: View) {
        walletBalanceText = view.findViewById(R.id.wallet_balance)
        btnWithdraw = view.findViewById(R.id.btn_withdraw)
        btnHistory = view.findViewById(R.id.btn_history)
        transactionsRecycler = view.findViewById(R.id.transactions_recycler)
        
        paypalCard = view.findViewById(R.id.paypal_method_card) ?: MaterialCardView(requireContext())
        bankCard = view.findViewById(R.id.bank_method_card) ?: MaterialCardView(requireContext())
        giftCardCard = view.findViewById(R.id.gift_method_card) ?: MaterialCardView(requireContext())
        cryptoCard = view.findViewById(R.id.crypto_method_card) ?: MaterialCardView(requireContext())
        
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(transactions)
        transactionsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    private fun setupTransactions() {
        transactionAdapter = TransactionAdapter(mutableListOf())
    }

    private fun setupClickListeners() {
        btnWithdraw.setOnClickListener { 
            animateButtonPress(it)
            showWithdrawalDialog()
        }

        btnHistory.setOnClickListener {
            animateButtonPress(it)
            Toast.makeText(context, "Opening transaction history...", Toast.LENGTH_SHORT).show()
        }
        
        val methodClickListener = View.OnClickListener { view ->
            animateButtonPress(view)
            val methodType = when(view.id) {
                R.id.paypal_method_card -> "PayPal"
                R.id.bank_method_card -> "Bank Transfer"
                R.id.gift_method_card -> "Gift Card"
                R.id.crypto_method_card -> "Cryptocurrency"
                else -> null
            }
            if (methodType != null) {
                showWithdrawalDialog()
            }
        }

        try {
            paypalCard.setOnClickListener(methodClickListener)
            bankCard.setOnClickListener(methodClickListener)
            giftCardCard.setOnClickListener(methodClickListener)
            cryptoCard.setOnClickListener(methodClickListener)
        } catch (e: Exception) {
            Log.e("WalletFragment", "Click listener setup error: ${e.message}")
        }
    }
    
    private fun animateButtonPress(view: View) {
        AnimationUtils.pressAnimation(view)
    }
    
    private fun loadWalletData() {
        lifecycleScope.launch {
            try {
                // Show shimmer loading state
                ShimmerHelper.showSkeleton(transactionsRecycler, R.layout.skeleton_list_container)
                
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser ?: return@launch
                
                val userId = currentUser.uid
                val response = FeaturesApiClient.getWalletData(userId)
                
                if (response?.has("balance") == true) {
                    currentBalance = response.getInt("balance")
                    updateBalanceUI()
                }
                
                // Load transactions
                val txnArray = response?.optJSONArray("transactions")
                if (txnArray != null) {
                    for (i in 0 until txnArray.length()) {
                        val txnObj = txnArray.getJSONObject(i)
                        transactions.add(
                            Transaction(
                                id = txnObj.getString("id"),
                                amount = txnObj.getInt("amount"),
                                type = txnObj.getString("type"),
                                date = txnObj.getString("date"),
                                status = txnObj.getString("status")
                            )
                        )
                    }
                    transactionAdapter.updateData(transactions)
                }
                
                // Hide shimmer
                ShimmerHelper.hideSkeleton(transactionsRecycler)
                
            } catch (e: Exception) {
                Log.e("WalletFragment", "Load wallet data error: ${e.message}")
                ShimmerHelper.hideSkeleton(transactionsRecycler)
                Toast.makeText(context, "Failed to load wallet data", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateBalanceUI() {
        val formatter = NumberFormat.getInstance(Locale.getDefault())
        walletBalanceText.text = "₹ ${formatter.format(currentBalance)}"
    }
    
    private fun showWithdrawalDialog() {
        try {
            val options = arrayOf("PayPal", "Bank Transfer", "Gift Card", "Cryptocurrency")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Withdrawal Method")
                .setItems(options) { _, which ->
                    val selected = options[which]
                    Toast.makeText(context, "Withdrawal via $selected", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: Exception) {
            Log.e("WalletFragment", "Dialog error: ${e.message}")
        }
    }
}

// Transaction Model
data class TransactionData(
    val id: String,
    val amount: Int,
    val type: String,
    val date: String,
    val status: String
)
