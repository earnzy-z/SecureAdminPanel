package com.earnzy.app.fragments

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

    private lateinit var balanceText: MaterialTextView
    private lateinit var btnWithdraw: MaterialButton
    private lateinit var btnHistory: MaterialButton
    private lateinit var transactionsRecycler: RecyclerView
    private lateinit var paypalCard: MaterialCardView
    private lateinit var bankCard: MaterialCardView
    private lateinit var giftCard: MaterialCardView
    private lateinit var cryptoCard: MaterialCardView

    private var transactionAdapter: TransactionAdapter? = null
    private val transactions = mutableListOf<Transaction>()
    private var currentBalance = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wallet_professional, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        animateEntrance()
        loadWalletData()
    }

    private fun initViews(view: View) {
        balanceText = view.findViewById(R.id.wallet_balance)
        btnWithdraw = view.findViewById(R.id.btn_withdraw)
        btnHistory = view.findViewById(R.id.btn_history)
        transactionsRecycler = view.findViewById(R.id.transactions_recycler)
        paypalCard = view.findViewById(R.id.paypal_method_card)
        bankCard = view.findViewById(R.id.bank_method_card)
        giftCard = view.findViewById(R.id.gift_method_card)
        cryptoCard = view.findViewById(R.id.crypto_method_card)
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(transactions)
        transactionsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    private fun setupClickListeners() {
        btnWithdraw.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            showWithdrawalDialog()
        }

        btnHistory.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "Transaction History", Toast.LENGTH_SHORT).show()
        }

        paypalCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "PayPal Withdrawal", Toast.LENGTH_SHORT).show()
        }

        bankCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "Bank Transfer", Toast.LENGTH_SHORT).show()
        }

        giftCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "Gift Card", Toast.LENGTH_SHORT).show()
        }

        cryptoCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "Cryptocurrency", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateEntrance() {
        try {
            AnimationUtils.slideUpIn(balanceText, delay = 0)
            AnimationUtils.slideUpIn(btnWithdraw, delay = 50)
            AnimationUtils.slideUpIn(transactionsRecycler, delay = 100)
        } catch (e: Exception) {
            Log.e("WalletFragment", "Animation error: ${e.message}")
        }
    }

    private fun loadWalletData() {
        lifecycleScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser ?: run {
                    setupMockWallet()
                    return@launch
                }

                val ctx = context ?: return@launch
                val idToken = try { user.getIdToken(false).await().token ?: "" } catch (e: Exception) { "" }
                val deviceID = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val deviceToken = ""
                val isVpn = false
                val isSslProxy = false

                val response = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getUserProfile(ctx, idToken, deviceID, deviceToken, isVpn, isSslProxy)
                }

                if (isAdded && response != null) {
                    val status = response.optString("status")
                    if (status == "success") {
                        val userObj = response.optJSONObject("user")
                        currentBalance = userObj?.optInt("balance", 0) ?: 0
                        updateBalanceUI()

                        val txnArray = userObj?.optJSONArray("transactions")
                        if (txnArray != null) {
                            transactions.clear()
                            for (i in 0 until minOf(txnArray.length(), 10)) {
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
                            transactionAdapter?.notifyDataSetChanged()
                        }
                    } else {
                        setupMockWallet()
                    }
                } else {
                    setupMockWallet()
                }
            } catch (e: Exception) {
                Log.e("WalletFragment", "Error loading wallet: ${e.message}")
                setupMockWallet()
            }
        }
    }

    private fun setupMockWallet() {
        currentBalance = 2500
        updateBalanceUI()

        transactions.clear()
        transactions.addAll(listOf(
            Transaction("Daily Bonus", "₹100", "Today 10:30 AM", "credit"),
            Transaction("Video Reward", "₹50", "Today 09:15 AM", "credit"),
            Transaction("Spin Wheel Win", "₹500", "Yesterday 08:45 PM", "credit"),
            Transaction("Withdrawal", "₹1000", "3 days ago", "debit"),
            Transaction("Referral Bonus", "₹200", "1 week ago", "credit")
        ))
        transactionAdapter?.notifyDataSetChanged()
    }

    private fun updateBalanceUI() {
        val formatter = NumberFormat.getInstance(Locale.getDefault())
        balanceText.text = "₹ ${formatter.format(currentBalance)}"
    }

    private fun showWithdrawalDialog() {
        try {
            val methods = arrayOf("PayPal", "Bank Transfer", "Gift Card", "Cryptocurrency")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Withdrawal Method")
                .setItems(methods) { _, which ->
                    Toast.makeText(context, "Withdrawal via ${methods[which]}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .setCancelable(true)
                .show()
        } catch (e: Exception) {
            Log.e("WalletFragment", "Dialog error: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        loadWalletData()
    }
}
