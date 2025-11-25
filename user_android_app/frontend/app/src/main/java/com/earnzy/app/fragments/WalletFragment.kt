package com.earnzy.app.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class WalletFragment : Fragment() {

    private lateinit var securePrefs: SharedPreferences
    private var currentBalance = 0
    private val transactions = mutableListOf<Transaction>()
    private lateinit var transactionAdapter: TransactionAdapter

    private lateinit var walletBalanceText: MaterialTextView
    private lateinit var btnWithdraw: MaterialButton
    private lateinit var btnHistory: MaterialButton
    private lateinit var transactionsRecycler: RecyclerView

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
            view.findViewById<MaterialTextView?>(R.id.wallet_balance)?.let {
                AnimationUtils.slideUpIn(it, delay = 0)
            }
            view.findViewById<MaterialButton?>(R.id.btn_withdraw)?.let {
                AnimationUtils.slideUpIn(it, delay = 100)
            }
            view.findViewById<RecyclerView?>(R.id.transactions_recycler)?.let {
                AnimationUtils.slideUpIn(it, delay = 200)
            }
        } catch (e: Exception) {
            Log.e("WalletFragment", "Animation error: ${e.message}")
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
            Log.e("WalletFragment", "Security error: ${e.message}")
        }
    }

    private fun initViews(view: View) {
        walletBalanceText = view.findViewById(R.id.wallet_balance)
        btnWithdraw = view.findViewById(R.id.btn_withdraw)
        btnHistory = view.findViewById(R.id.btn_history)
        transactionsRecycler = view.findViewById(R.id.transactions_recycler)
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
            AnimationUtils.pressAnimation(it)
            showWithdrawalDialog()
        }

        btnHistory.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "Opening transaction history...", Toast.LENGTH_SHORT).show()
        }

        try {
            view?.findViewById<MaterialCardView?>(R.id.paypal_method_card)?.setOnClickListener {
                AnimationUtils.pressAnimation(it)
                showWithdrawalDialog()
            }
            view?.findViewById<MaterialCardView?>(R.id.bank_method_card)?.setOnClickListener {
                AnimationUtils.pressAnimation(it)
                showWithdrawalDialog()
            }
            view?.findViewById<MaterialCardView?>(R.id.gift_method_card)?.setOnClickListener {
                AnimationUtils.pressAnimation(it)
                showWithdrawalDialog()
            }
            view?.findViewById<MaterialCardView?>(R.id.crypto_method_card)?.setOnClickListener {
                AnimationUtils.pressAnimation(it)
                showWithdrawalDialog()
            }
        } catch (e: Exception) {
            Log.e("WalletFragment", "Click setup error: ${e.message}")
        }
    }

    private fun loadWalletData() {
        lifecycleScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser ?: return@launch
                val userId = currentUser.uid

                val ctx = context ?: return@launch
                val idToken = try { currentUser.getIdToken(false).await().token ?: "" } catch (e: Exception) { "" }
                val deviceID = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val deviceToken = ""
                val isVpn = false
                val isSslProxy = false

                val response = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getUserProfile(ctx, idToken, deviceID, deviceToken, isVpn, isSslProxy)
                }

                if (isAdded && response != null) {
                    currentBalance = response.optInt("balance", 0)
                    updateBalanceUI()

                    val txnArray = response.optJSONArray("transactions")
                    if (txnArray != null) {
                        transactions.clear()
                        for (i in 0 until txnArray.length()) {
                            val txnObj = txnArray.getJSONObject(i)
                            transactions.add(
                                Transaction(
                                    description = txnObj.optString("description", "Transaction"),
                                    amount = txnObj.optString("amount", "₹0"),
                                    timestamp = txnObj.optString("timestamp", ""),
                                    type = txnObj.optString("type", "credit")
                                )
                            )
                        }
                        transactionAdapter = TransactionAdapter(transactions)
                        transactionsRecycler.adapter = transactionAdapter
                    }
                }
            } catch (e: Exception) {
                Log.e("WalletFragment", "Load error: ${e.message}")
                if (isAdded) {
                    Toast.makeText(context, "Failed to load wallet", Toast.LENGTH_SHORT).show()
                }
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
                    Toast.makeText(context, "Withdrawal via ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: Exception) {
            Log.e("WalletFragment", "Dialog error: ${e.message}")
        }
    }
}
