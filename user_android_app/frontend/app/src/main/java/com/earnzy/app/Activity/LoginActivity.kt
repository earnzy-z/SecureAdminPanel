package com.earnzy.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.Timer
import java.util.TimerTask
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class LoginActivity : AppCompatActivity() {

    // Unified backend with authentication and all features - hybrid encrypted, multiple DB connections
    private val SERVER_URL = "https://earnzy-auth.earnzy.workers.dev/"
    private val _auth = FirebaseAuth.getInstance()
    private lateinit var credentialManager: CredentialManager
    private lateinit var securePrefs: SharedPreferences // This is the EncryptedSharedPreferences
    private var deviceID = ""
    private var deviceToken = ""
    private var fcmToken = ""
    private var isVpn = false
    private var isSslProxy = false
    private var isProcessingClipboard = false

    private lateinit var background_viewpager: ViewPager2
    private lateinit var card_layout: LinearLayout
    private lateinit var textview_title: TextView
    private lateinit var textview_subtitle: TextView
    private lateinit var divider: View
    private lateinit var terms_checkbox: CheckBox
    private lateinit var textview_error: TextView
    private lateinit var button_frame: FrameLayout
    private lateinit var google_btn: Button
    private lateinit var loading_progressbar: ProgressBar
    private val _timer = Timer()
    private var activeDialog: AlertDialog? = null
    private var isInternetDialogShowing = false
    private var isVpnDialogShowing = false

    // ---
    // --- CRASH FIX ANALYSIS ---
    // ---
    // The stack trace points to a crash in a function called `getClientPublicB64` at line 187,
    // caused by a `PatternSyntaxException` at line 183.
    // The bug was using an invalid regex: `"+".toRegex()`.
    //
    // This function below, `getRsaPublicKey`, appears to be the fixed version of that
    // crashing function.
    //
    // 1. THE FIX (Line 136): You are now correctly using `".replace("\\s".toRegex(), "")`
    //    to remove whitespace. This is a valid regex and solves the `PatternSyntaxException`.
    //
    // 2. BETTER ERROR HANDLING (Line 142): The crashing code threw a new `RuntimeException`
    //    inside its `catch` block (at line 187), which is what killed the app.
    //    This function correctly returns `null` instead. This is much safer, as the
    //    calling function (`encryptHybrid`) can handle the `null` case without crashing.
    // ---
    private fun getRsaPublicKey(context: Context): PublicKey? {
        return try {
            val keyFactory = KeyFactory.getInstance("RSA")
            val pem = context.getString(R.string.server_rsa_public_key_pem)
            val cleanKey = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "") // <-- THIS IS THE FIX
            val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
            val spec = X509EncodedKeySpec(keyBytes)
            keyFactory.generatePublic(spec)
        } catch (e: Exception) {
            Log.e("LoginActivity", "RSA Key Error", e) // Keep critical error logs
            null // <-- THIS IS SAFER (prevents the RuntimeException crash)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        initialize(savedInstanceState)
        FirebaseApp.initializeApp(this)
        credentialManager = CredentialManager.create(this)
        initializeSecureStorage() // Initialize secure storage
        initializeLogic()
    }

    private fun initializeSecureStorage() {
        try {
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            securePrefs = EncryptedSharedPreferences.create(
                this,
                "SecureEarnzyPrefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("SecurePrefs", "Security setup failed. App cannot continue.", e)
            Toast.makeText(this, "Security setup failed. Please restart app.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initialize(savedInstanceState: Bundle?) {
        background_viewpager = findViewById(R.id.background_viewpager)
        card_layout = findViewById(R.id.card_layout)
        textview_title = findViewById(R.id.textview_title)
        textview_subtitle = findViewById(R.id.textview_subtitle)
        divider = findViewById(R.id.divider)
        terms_checkbox = findViewById(R.id.terms_checkbox)
        textview_error = findViewById(R.id.textview_error)
        button_frame = findViewById(R.id.button_frame)
        google_btn = findViewById(R.id.google_btn)
        loading_progressbar = findViewById(R.id.loading_progressbar)
    }

    private fun initializeLogic() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        _setupWindowStyling()
        _animateViewsIn()
        _setupBackgroundSlider()
        _setupClickableTerms()
        _setupLoginButton()
        _startNetworkChecks()

        val cardBackground = GradientDrawable()
        cardBackground.cornerRadii = floatArrayOf(60f, 60f, 60f, 60f, 0f, 0f, 0f, 0f)
        cardBackground.setColor(0xFFFFFFFF.toInt())
        card_layout.background = cardBackground

        initializeDeviceCredentials()

        fcmToken = securePrefs.getString("fcmToken", "") ?: ""
        if (fcmToken.isNotEmpty()) {
            google_btn.isEnabled = true
            google_btn.text = "Sign in with Google"
            loading_progressbar.visibility = View.GONE
        } else {
            google_btn.isEnabled = false
            google_btn.text = ""
            loading_progressbar.visibility = View.VISIBLE
            fetchFcmTokenWithRetry(maxRetries = 3, retryDelayMs = 2000)
        }

        isVpn = securePrefs.getBoolean("isVpn", false)
        isSslProxy = securePrefs.getBoolean("isSslProxy", false)

        if (deviceToken.isEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                initializeDeviceCredentials()
                if (deviceToken.isEmpty()) {
                    _showErrorAndReset("Device registration failed. Please try again.")
                }
                // Removed auto-click for security/UX reasons. User should initiate login.
            }, 5000)
        }
    }

 private fun initializeDeviceCredentials() {
    // Secure way to generate a unique ID using hardware info and a fallback
    deviceID = _getDeviceId(this)
    
    // FIXED: Persist deviceID to securePrefs (idempotent: save only if missing, else reload)
    if (securePrefs.getString("deviceID", "").isNullOrEmpty()) {
        securePrefs.edit().putString("deviceID", deviceID).apply()
    } else {
        deviceID = securePrefs.getString("deviceID", "") ?: deviceID  // Fallback to generated if reload fails
    }

    if (securePrefs.getString("deviceToken", "").isNullOrEmpty()) {
        deviceToken = generateSecureDeviceToken()
        securePrefs.edit().putString("deviceToken", deviceToken).apply()
    } else {
        deviceToken = securePrefs.getString("deviceToken", "") ?: ""
    }
 }

    // ADDED: Consistent Device ID functions
    private fun _getDeviceId(context: Context): String {
        return try {
            val hardwareProperties = Build.BRAND +
                    Build.MANUFACTURER +
                    Build.MODEL +
                    Build.DEVICE +
                    Build.PRODUCT +
                    Build.HARDWARE

            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(hardwareProperties.toByteArray(Charsets.UTF_8))
            android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)

        } catch (e: Exception) {
            try {
                val androidId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                ) ?: "unknown_device"

                android.util.Base64.encodeToString(androidId.toByteArray(), android.util.Base64.NO_WRAP)

            } catch (inner: Exception) {
                generateFallbackDeviceId()
            }
        }
    }
    
    @Suppress("DEPRECATION")
    private fun generateFallbackDeviceId(): String {
        val serial = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()
            } else {
                Build.SERIAL
            }
        } catch (e: Exception) {
            "UNKNOWN"
        }

        val deviceInfo = "${Build.MANUFACTURER}${Build.MODEL}$serial"
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(deviceInfo.toByteArray(Charsets.UTF_8))

        return "ANDROID_" + android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
            .replace("/", "_")
            .replace("+", "-")
            .replace("=", "")
            .take(16)
    }


    private fun generateSecureDeviceToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
            .replace("+", "-")
            .replace("/", "_")
            .replace("=", "")
            .substring(0, 32)
    }

    private fun fetchFcmTokenWithRetry(maxRetries: Int, retryDelayMs: Long, attempt: Int = 1) {
        val timeoutTask = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    _showErrorAndReset("Unable to initialize. Please try again.")
                    google_btn.text = "Sign in with Google"
                    loading_progressbar.visibility = View.GONE
                    google_btn.isEnabled = true
                }
            }
        }

        val timer = Timer()
        timer.schedule(timeoutTask, retryDelayMs)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            timeoutTask.cancel()
            timer.cancel()
            if (task.isSuccessful) {
                fcmToken = task.result
                securePrefs.edit().putString("fcmToken", fcmToken).apply()
                runOnUiThread {
                    google_btn.isEnabled = true
                    google_btn.text = "Sign in with Google"
                    loading_progressbar.visibility = View.GONE
                }
            } else {
                if (attempt < maxRetries) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        fetchFcmTokenWithRetry(maxRetries, retryDelayMs, attempt + 1)
                    }, retryDelayMs)
                } else {
                    runOnUiThread {
                        _showErrorAndReset("Unable to initialize. Please try again.")
                        google_btn.text = "Sign in with Google"
                        loading_progressbar.visibility = View.GONE
                        google_btn.isEnabled = true
                    }
                }
            }
        }
    }

 private fun _setupWindowStyling() {
    // Enable edge-to-edge layout (supported on all versions via AndroidX)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    
    // Enable system bar drawing
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }
    
    // Configure system bars appearance (light icons)
    WindowCompat.getInsetsController(window, window.decorView)?.apply {
        isAppearanceLightStatusBars = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isAppearanceLightNavigationBars = true
        }
    }
    
    // Apply transparent system bars
    applyTransparentSystemBars()
}

@Suppress("DEPRECATION")
private fun applyTransparentSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = Color.TRANSPARENT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.TRANSPARENT
        }
    }
}


    private fun _animateViewsIn() {
        card_layout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                card_layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                card_layout.translationY = card_layout.height.toFloat()
                card_layout.visibility = View.VISIBLE
                card_layout.animate()
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(android.view.animation.PathInterpolator(0.25f, 1f, 0.5f, 1f))
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            val viewsToFade = arrayOf(textview_title, textview_subtitle, divider, terms_checkbox, button_frame)
                            var delay: Long = 0
                            for (view in viewsToFade) {
                                view.alpha = 0f
                                view.animate()
                                    .alpha(1f)
                                    .setDuration(400)
                                    .setStartDelay(delay)
                                    .start()
                                delay += 50
                            }
                        }
                    }).start()
            }
        })
    }

    private fun _setupBackgroundSlider() {
        val imageNames = arrayOf("bg_image_1", "bg_image_2", "bg_image_3")
        val imageResources = ArrayList<Int>()
        for (name in imageNames) {
            val resourceId = resources.getIdentifier(name, "drawable", packageName)
            if (resourceId != 0) {
                imageResources.add(resourceId)
            }
        }
        if (imageResources.isEmpty()) return
        background_viewpager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                val imageView: ImageView = itemView as ImageView
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val imageView = ImageView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                return SliderViewHolder(imageView)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                Glide.with(this@LoginActivity)
                    .load(imageResources[position % imageResources.size])
                    .apply(RequestOptions().centerCrop())
                    .into((holder as SliderViewHolder).imageView)
            }

            override fun getItemCount(): Int = Int.MAX_VALUE
        }
        background_viewpager.isUserInputEnabled = false
        background_viewpager.setCurrentItem(Int.MAX_VALUE / 2, false)
        _timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    background_viewpager.currentItem = background_viewpager.currentItem + 1
                }
            }
        }, 5000, 5000)
    }

    private fun _setupClickableTerms() {
        val fullText = "I agree to the Terms of Service and Privacy Policy"
        val spannableString = SpannableString(fullText)
        val termsSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.cancelPendingInputEvents()
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://earnzy.com.in/terms")))
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = Color.BLUE
                ds.isFakeBoldText = true
            }
        }
        val policySpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.cancelPendingInputEvents()
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://earnzy.com.in/privacy")))
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = Color.BLUE
                ds.isFakeBoldText = true
            }
        }
        spannableString.setSpan(termsSpan, 15, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(policySpan, 36, 50, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        terms_checkbox.text = spannableString
        terms_checkbox.movementMethod = LinkMovementMethod.getInstance()
        terms_checkbox.highlightColor = Color.TRANSPARENT
    }

    private fun _setupLoginButton() {
        google_btn.setOnClickListener {
            if (!google_btn.isEnabled || isProcessingClipboard) return@setOnClickListener
            if (!_isNetworkConnected()) {
                _showInternetPopup()
                return@setOnClickListener
            }
            if (isVpn || isSslProxy) {
                _showVpnPopup()
                return@setOnClickListener
            }
            if (!terms_checkbox.isChecked) {
                textview_error.text = "Please accept the Terms of Service and Privacy Policy."
                textview_error.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (deviceToken.isEmpty()) {
                textview_error.text = "Device registration not complete. Please wait."
                textview_error.visibility = View.VISIBLE
                return@setOnClickListener
            }
            google_btn.isEnabled = false
            textview_error.visibility = View.GONE
            google_btn.text = ""
            loading_progressbar.visibility = View.VISIBLE
            lifecycleScope.launch { signInWithGoogle() }
        }
    }

    private fun _startNetworkChecks() {
        _timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (!google_btn.isEnabled || isProcessingClipboard) return@runOnUiThread
                    if (!_isNetworkConnected()) {
                        _showInternetPopup()
                    } else if (isVpn || isSslProxy) {
                        _showVpnPopup()
                    } else if ((isInternetDialogShowing || isVpnDialogShowing) && activeDialog != null && activeDialog!!.isShowing) {
                        activeDialog!!.dismiss()
                        isInternetDialogShowing = false
                        isVpnDialogShowing = false
                    }
                }
            }
        }, 1000, 2000)
    }

    private fun _isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    private fun _showConnectivityPopup(layoutResId: Int, toastMessage: String, onDismiss: () -> Unit) {
        if (activeDialog != null && activeDialog!!.isShowing || isFinishing) return

        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(layoutResId, null)
        val button1 = view.findViewById<Button>(R.id.button1)

        val sketchUi = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(0xFF3949AB.toInt(), 0xFF5C6BC0.toInt())
        )
        sketchUi.cornerRadius = resources.displayMetrics.density * 10
        button1.background = sketchUi

        button1.setOnClickListener {
            if (_isNetworkConnected() && !isVpn && !isSslProxy) {
                activeDialog?.dismiss()
                onDismiss()
            }
        }

        activeDialog = builder.setView(view).create()
        activeDialog!!.setCancelable(false)
        activeDialog!!.setCanceledOnTouchOutside(false)

        val background = GradientDrawable()
        background.setColor(Color.WHITE)
        background.cornerRadius = 40f

        activeDialog!!.window?.apply {
            setBackgroundDrawable(background)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes.dimAmount = 0.6f
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        activeDialog!!.show()
    }

    private fun _showInternetPopup() {
        if (isInternetDialogShowing) return
        isInternetDialogShowing = true
        _showConnectivityPopup(R.layout.popup_internet, "No internet connection") { isInternetDialogShowing = false }
    }

    private fun _showVpnPopup() {
        if (isVpnDialogShowing) return
        isVpnDialogShowing = true
        _showConnectivityPopup(R.layout.popup_vpn, "Please disable VPN") { isVpnDialogShowing = false }
    }

    private suspend fun signInWithGoogle() {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(this, request)
            handleSignIn(result)
        } catch (e: GetCredentialException) {
            _showErrorAndReset("Sign-in failed. Please try again.")
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is GoogleIdTokenCredential -> {
                _firebaseAuthWithGoogle(credential.idToken)
            }
            else -> {
                _showErrorAndReset("Sign-in failed. Please try again.")
            }
        }
    }

    private fun _firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                val user = _auth.currentUser
                if (user != null) {
                    user.getIdToken(true)
                        .addOnSuccessListener { result ->
                            val firebaseIdToken = result.token ?: ""
                            val name = user.displayName ?: ""
                            val email = user.email ?: ""
                            val photo = user.photoUrl?.toString() ?: ""
                            _handleReferralCode(isNewUser, firebaseIdToken, name, email, photo, user.uid)
                        }
                        .addOnFailureListener {
                            _showErrorAndReset("Sign-in failed. Please try again.")
                        }
                } else {
                    _showErrorAndReset("Sign-in failed. Please try again.")
                }
            }
            .addOnFailureListener {
                _showErrorAndReset("Sign-in failed. Please try again.")
            }
    }

    private fun _checkClipboardForReferral(
        idToken: String,
        name: String,
        email: String,
        photo: String,
        uid: String
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipItem = clipboard.primaryClip?.getItemAt(0)
                val clipText = clipItem?.text?.toString()?.trim()?.uppercase()

                if (clipText.isNullOrEmpty()) {
                    _proceedWithReferral(idToken, name, email, photo, uid, null)
                    return@launch
                }

                isProcessingClipboard = true
                google_btn.text = ""
                loading_progressbar.visibility = View.VISIBLE

                val json = JSONObject().apply {
                    put("action", "validateReferral")
                    put("referralCode", clipText)
                    put("deviceID", deviceID)
                    put("deviceToken", deviceToken)
                    put("idToken", idToken)
                    put("isVpn", isVpn)
                    put("isSslProxy", isSslProxy)
                }

                val response = sendEncryptedPostSuspend(SERVER_URL, json)

                try {
                    if (response.getString("status") == "success" && response.getBoolean("valid")) {
                        // Clear clipboard after successful validation
                        clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                        _sendToServer(idToken, name, email, photo, uid, clipText, false, null)
                    } else {
                        _proceedWithReferral(idToken, name, email, photo, uid, clipText)
                    }
                } catch (e: Exception) {
                    _proceedWithReferral(idToken, name, email, photo, uid, clipText)
                } finally {
                    isProcessingClipboard = false
                    _resetLoginButtonState()
                }
            } catch (e: Exception) {
                _proceedWithReferral(idToken, name, email, photo, uid, null)
                isProcessingClipboard = false
                _resetLoginButtonState()
            }
        }
    }

    private fun validateReferralCode(code: String): Boolean {
        // More robust validation, e.g., EARNZY + 5 digits
        return code.length == 11 && code.startsWith("EARNZY") && code.substring(6).all { it.isDigit() }
    }

    private fun getPossibleCodes(): ArrayList<String> {
        val possibleCodes = arrayListOf<String>()
        val savedRefCode = securePrefs.getString("refCodeInput", "") ?: ""
        if (savedRefCode.isNotEmpty()) {
            possibleCodes.add(savedRefCode)
        }

        val appLinkCode = getAppLinkCode()
        if (appLinkCode != null) {
            possibleCodes.add(appLinkCode)
        }
        return possibleCodes
    }

    private fun getAppLinkCode(): String? {
        val data = intent.data
        if (data != null && "https" == data.scheme && "earnzy-refer.earnzy.workers.dev" == data.host) {
            val code = data.getQueryParameter("code")?.uppercase()
            if (code != null && validateReferralCode(code)) {
                return code
            }
        }
        return null
    }

    private fun _proceedWithReferral(
        idToken: String,
        name: String,
        email: String,
        photo: String,
        uid: String,
        prefillCode: String?
    ) {
        val hasShownReferralPopup = securePrefs.getBoolean("hasShownReferralPopup", false)
        val possibleCodes = getPossibleCodes()
        if (hasShownReferralPopup || possibleCodes.isNotEmpty()) {
            _sendToServer(idToken, name, email, photo, uid, null, false, possibleCodes)
            return
        }

        securePrefs.edit().putBoolean("hasShownReferralPopup", true).apply()
        _showReferralInputDialog(
            "Please enter a referral code to get started.",
            idToken, name, email, photo, uid,
            prefillCode
        )
    }

    private fun _handleReferralCode(
        isNewUser: Boolean,
        idToken: String,
        name: String,
        email: String,
        photo: String,
        uid: String
    ) {
        if (!isNewUser) {
            _sendToServer(idToken, name, email, photo, uid, null, false, null)
            return
        }

        _checkClipboardForReferral(idToken, name, email, photo, uid)
    }

private fun _sendToServer(
    idToken: String,
    name: String,
    email: String,
    photo: String,
    uid: String,
    referralCode: String?,
    skipReferral: Boolean,
    possibleCodes: ArrayList<String>?
) {
    lifecycleScope.launch(Dispatchers.Main) {
        google_btn.text = ""
        loading_progressbar.visibility = View.VISIBLE

        val json = JSONObject().apply {
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("fcmToken", fcmToken)
            put("name", name)
            put("email", email)
            put("photo", photo)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
            if (referralCode != null) put("referralCode", referralCode)
            if (skipReferral) put("skipReferral", true)
            if (possibleCodes != null && possibleCodes.isNotEmpty()) {
                val arr = JSONArray()
                for (c in possibleCodes) arr.put(c)
                put("possibleReferrals", arr)
            }
        }

        try {
            val response = sendEncryptedPostSuspend(SERVER_URL, json)

            val status = response.getString("status")
            if (status == "prompt_referral") {
                _showReferralInputDialog(response.getString("message"), idToken, name, email, photo, uid, null)
            } else if (status == "success") {
                val action = response.getString("action")
                if (action == "register") {
                    val usedRef = response.optString("usedReferralCode", "")
                    securePrefs.edit().putString("refCodeInput", usedRef).apply()
                    Toast.makeText(this@LoginActivity, "Sign up successful!", Toast.LENGTH_SHORT).show()
                } else if (action == "login") {
                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                }
                _goToHomeAfterDelay()
            } else {
                _showErrorAndReset(response.optString("message", "Unable to sign in. Please try again."))
                if (response.has("existingAccounts")) {
                    val existingAccounts = response.getJSONArray("existingAccounts")
                    _showLimitReachedPopup(existingAccounts)
                }
            }
        } catch (e: Exception) {
            // Check if the exception is the one we're looking for, although at this point
            // the getRsaPublicKey function should prevent it by returning null.
            // This is just a defensive check.
            if (e.cause is java.util.regex.PatternSyntaxException) {
                _showErrorAndReset("A security error occurred. Please update your app.")
            } else {
                _showErrorAndReset("Unable to sign in. Network error.")
            }
        }
    }
}


    private fun encryptHybrid(context: Context, data: String): String {
        return try {
            val rsaPublicKey = getRsaPublicKey(context)
            if (rsaPublicKey == null) {
                // This will be null if getRsaPublicKey fails, preventing the crash
                throw Exception("Failed to load RSA Public Key.")
            }
            val sessionAesKey = ByteArray(32).apply { SecureRandom().nextBytes(this) }
            val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
            val aesKeySpec = SecretKeySpec(sessionAesKey, "AES")
            val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(iv)
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec)
            val encryptedBody = aesCipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))

            // Use secure padding
            val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
            val encryptedSessionKey = rsaCipher.doFinal(sessionAesKey)

            val combined = Base64.encodeToString(encryptedSessionKey, Base64.NO_WRAP) + "|" +
                    Base64.encodeToString(iv, Base64.NO_WRAP) + "|" +
                    Base64.encodeToString(encryptedBody, Base64.NO_WRAP)

            // Final transport encoding (base64 URL safe)
            Base64.encodeToString(combined.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "")

        } catch (e: Exception) {
            Log.e("EncryptHybrid", "Encryption failed", e)
            ""
        }
    }

    private suspend fun sendEncryptedPostSuspend(
        url: String,
        data: JSONObject
    ): JSONObject = withContext(Dispatchers.IO) {
        try {
            val u = URL(url)
            val conn = u.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "text/plain") // Send as plain text
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 30000

            val encrypted = encryptHybrid(this@LoginActivity, data.toString())
            if (encrypted.isEmpty()) {
                // This will be triggered if getRsaPublicKey returned null
                throw Exception("Encryption failed. Hybrid key exchange failed.")
            }

            conn.outputStream.use { os ->
                OutputStreamWriter(os, StandardCharsets.UTF_8).use { writer ->
                    writer.write(encrypted)
                }
            }

            val responseCode = conn.responseCode
            val inputStream = if (responseCode < 400) conn.inputStream else conn.errorStream

            val responseBody = inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { br ->
                    br.readText()
                }
            }

            JSONObject(responseBody)

        } catch (e: Exception) {
            Log.e("NetworkError", "Network/Response Error", e)
            throw e
        }
    }

    private fun _showLimitReachedPopup(existingAccounts: JSONArray) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Account Limit Reached")
            .setCancelable(false)
            .create()

        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_limit_reached, null)
        val accountsContainer = dialogView.findViewById<LinearLayout>(R.id.accounts_container)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.accounts_progressbar)
        val closeButton = dialogView.findViewById<View>(R.id.close_button)

        progressBar.visibility = View.GONE
        accountsContainer.visibility = View.VISIBLE
        accountsContainer.removeAllViews()

        try {
            for (i in 0 until existingAccounts.length()) {
                val account = existingAccounts.getJSONObject(i)
                val accountItemView = layoutInflater.inflate(R.layout.list_item_registered_account, accountsContainer, false)
                accountItemView.findViewById<TextView>(R.id.user_name).text = account.getString("name")
                accountItemView.findViewById<TextView>(R.id.user_email).text = account.getString("email")
                val profileImage = accountItemView.findViewById<ImageView>(R.id.profile_image)
                Glide.with(this)
                    .load(account.getString("photo"))
                    .placeholder(R.drawable.ic_launcher_background) // Ensure these drawables exist
                    .error(R.drawable.ic_launcher_background)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage)
                accountsContainer.addView(accountItemView)
            }
        } catch (e: Exception) {
            _showErrorAndReset("Unable to display accounts. Please try again.")
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
            _resetLoginButtonState()
        }

        dialog.setView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun _showReferralInputDialog(
        message: String,
        idToken: String,
        name: String,
        email: String,
        photo: String,
        uid: String,
        prefillCode: String?
    ) {
        runOnUiThread {
            val view = layoutInflater.inflate(R.layout.custom_input_dialog, null)
            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()

            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes.windowAnimations = R.style.DialogAnimation
            }

            val input = view.findViewById<EditText>(R.id.edittext_referral)
            val continueButton = view.findViewById<Button>(R.id.button_continue)
            val skipButton = view.findViewById<Button>(R.id.button_skip)
            val subtitle = view.findViewById<TextView>(R.id.textview_subtitle)
            val loadingIndicator = view.findViewById<ProgressBar>(R.id.loading_indicator)
            val buttonContainer = view.findViewById<LinearLayout>(R.id.button_container)

            subtitle.text = message

            if (!prefillCode.isNullOrEmpty()) {
                input.setText(prefillCode)
                input.selectAll()
            }

            continueButton.setOnClickListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    val codeToValidate = input.text.toString().trim().uppercase()
                    buttonContainer.visibility = View.GONE
                    loadingIndicator.visibility = View.VISIBLE
                    input.error = null

                    if (codeToValidate.isEmpty()) {
                        input.error = "Code cannot be empty"
                        loadingIndicator.visibility = View.GONE
                        buttonContainer.visibility = View.VISIBLE
                        return@launch
                    }

                    if (!validateReferralCode(codeToValidate)) {
                        input.error = "Invalid code format. E.g., EARNZY12345"
                        loadingIndicator.visibility = View.GONE
                        buttonContainer.visibility = View.VISIBLE
                        return@launch
                    }

                    val json = JSONObject().apply {
                        put("action", "validateReferral")
                        put("referralCode", codeToValidate)
                        put("deviceID", deviceID)
                        put("deviceToken", deviceToken)
                        put("idToken", idToken)
                        put("isVpn", isVpn)
                        put("isSslProxy", isSslProxy)
                    }

                    try {
                        val response = sendEncryptedPostSuspend(SERVER_URL, json)

                        if (response.getString("status") == "success" && response.getBoolean("valid")) {
                            _sendToServer(idToken, name, email, photo, uid, codeToValidate, false, null)
                            dialog.dismiss()
                        } else {
                            input.error = response.optString("message", "Invalid referral code")
                            loadingIndicator.visibility = View.GONE
                            buttonContainer.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        input.error = "Unable to validate code"
                        loadingIndicator.visibility = View.GONE
                        buttonContainer.visibility = View.VISIBLE
                    }
                }
            }

            skipButton.setOnClickListener {
                dialog.dismiss()
                _sendToServer(idToken, name, email, photo, uid, null, true, null)
            }

            dialog.show()
        }
    }

    private fun _resetLoginButtonState() {
        google_btn.text = "Sign in with Google"
        loading_progressbar.visibility = View.GONE
        google_btn.isEnabled = true
        isProcessingClipboard = false
    }

    private fun _showErrorAndReset(message: String) {
        runOnUiThread {
            textview_error.text = message
            textview_error.visibility = View.VISIBLE
            _resetLoginButtonState()
        }
    }

    private fun _goToHomeAfterDelay() {
        runOnUiThread {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _timer.cancel()
        activeDialog?.dismiss()
    }
}