package com.earnzy.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Base64
import android.view.Gravity
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Timer
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Cipher

class MainActivity : AppCompatActivity() {

    private var timer: Timer? = null
    private var isInternetDialogShowing = false
    private var isVpnDialogShowing = false
    private var isUnderMaintenance = false
    private var hasNavigated = AtomicBoolean(false)
    private var hasShownPermissionSheet = AtomicBoolean(false)
    private var isMaintenanceChecked = false
    private var isVpnChecked = false
    private val isChecking = AtomicBoolean(false)
    private var dialog: AlertDialog? = null
    private var lastCheckTime: Long = 0
    private val CHECK_COOLDOWN = 5000L
    private val SPLASH_DURATION = 4000L
    private val PERMISSION_DELAY = 2000L
    private val MAX_RETRIES = 3
    private lateinit var lottie1: LottieAnimationView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var securePrefs: SharedPreferences 

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasShownPermissionSheet.set(true)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!hasNavigated.get()) {
                checkTutorialAndProceed()
            }
        }, PERMISSION_DELAY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        initialize()
        FirebaseApp.initializeApp(this)
        initializeLogic()
    }

    private fun initialize() {
        lottie1 = findViewById(R.id.lottie1)
        firebaseAuth = FirebaseAuth.getInstance()
        securePrefs = getSecurePrefs(this) 
        timer = Timer()
    }

    private fun getSecurePrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        try {
            return EncryptedSharedPreferences.create(
                context,
                "SecureEarnzyPrefs", 
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            throw RuntimeException("CRITICAL: Secure storage initialization failed. App cannot proceed.", e)
        }
    }

    private fun encryptRSA(data: String): String? {
        try {
            val rsaPublicKeyPem = getString(R.string.server_rsa_public_key_pem2)
            val keyBytes = Base64.decode(
                rsaPublicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("\\s".toRegex(), ""),
                Base64.DEFAULT
            )
            val spec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey: PublicKey = keyFactory.generatePublic(spec)

            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding") 
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            return null
        }
    }

    private fun initializeLogic() {
        _setupWindowStyling()
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // No logging in release mode
                }
            }
        _createNotificationChannel()
        startSplashSequence()
    }

    private fun startSplashSequence() {
        lottie1.playAnimation()
        lottie1.repeatCount = 0

        Handler(Looper.getMainLooper()).postDelayed({
            if (!hasNavigated.get() && !hasShownPermissionSheet.get()) {
                checkNetworkAndProceed()
            }
        }, SPLASH_DURATION)
    }

    private fun checkNetworkAndProceed() {
        if (hasNavigated.get() || hasShownPermissionSheet.get()) return

        if (!isNetworkAvailable()) {
            showNoInternetPopup()
            return
        }

        checkVpnAndMaintenance(attempt = 1)
    }

    private fun checkNetworkAndShowPopup() {
        if (hasNavigated.get() || hasShownPermissionSheet.get()) return
        val networkAvailable = isNetworkAvailable()
        if (networkAvailable && isInternetDialogShowing && dialog?.isShowing == true) {
            dialog?.dismiss()
            isInternetDialogShowing = false
        } else if (!networkAvailable && !isInternetDialogShowing && !hasNavigated.get() && !hasShownPermissionSheet.get()) {
            showNoInternetPopup()
        }
    }

    private fun showNoInternetPopup() {
        if (isFinishing || isDestroyed || hasNavigated.get() || hasShownPermissionSheet.get()) return
        isInternetDialogShowing = true

        val view = layoutInflater.inflate(R.layout.popup_internet, null)
        view.background = GradientDrawable().apply {
            cornerRadius = 21 * resources.displayMetrics.density
            setColor(Color.WHITE)
        }

        val button1 = view.findViewById<Button>(R.id.button1)
        button1.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(0xFF3949AB.toInt(), 0xFF5C6BC0.toInt())
        ).apply {
            cornerRadius = resources.displayMetrics.density * 10
            setStroke((2 * resources.displayMetrics.density).toInt(), 0xFF616161.toInt())
        }

        dialog = AlertDialog.Builder(this).setView(view).create()
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!isFinishing && !isDestroyed) {
            try {
                dialog?.show()
                dialog?.window?.setLayout(
                    (300 * resources.displayMetrics.density).toInt(),
                    (400 * resources.displayMetrics.density).toInt()
                )
                dialog?.window?.attributes?.gravity = Gravity.CENTER
            } catch (e: Exception) {
                isInternetDialogShowing = false
            }
        } else {
            isInternetDialogShowing = false
        }

        button1.setOnClickListener {
            if (isNetworkAvailable()) {
                dialog?.dismiss()
                isInternetDialogShowing = false
                Handler(Looper.getMainLooper()).postDelayed({
                    checkNetworkAndProceed()
                }, 1000)
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkVpnAndMaintenance(attempt: Int) {
        if (isChecking.get() || hasNavigated.get() || !isNetworkAvailable() || hasShownPermissionSheet.get()) return
        if (!isChecking.compareAndSet(false, true)) return

        lastCheckTime = System.currentTimeMillis()

        Thread {
            val deviceId = _getDeviceId(applicationContext) 
            val json = JSONObject().apply {
                put("deviceId", deviceId)
                put("timestamp", System.currentTimeMillis())
                put("ip", "") 
                put("userAgent", "EarnzyApp/1.0 (Android)")
            }
            
            val encryptedPayload = encryptRSA(json.toString())
            
            if (encryptedPayload == null) {
                runOnUiThread { showError("Security check failed") }
                isChecking.set(false)
                return@Thread
            }

            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val apiKey = String(Base64.decode(getString(R.string.api_key_base64), Base64.DEFAULT))
                val requestBody = encryptedPayload.toRequestBody("application/octet-stream".toMediaType())
                
                val request = Request.Builder()
                    .url("https://vn.earnzy.workers.dev/")
                    .post(requestBody)
                    .addHeader("User-Agent", "EarnzyApp/1.0 (Android)")
                    .addHeader("X-API-Key", apiKey)
                    .build()

                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val obj = JSONObject(responseBody)
                    val token = obj.optString("token", "")
                    if (token.isNotEmpty()) {
                        securePrefs.edit().putString("deviceToken", token).apply()
                    }
                    runOnUiThread {
                        handleWorkerResponse(
                            obj.optBoolean("maintenance", false),
                            obj.optBoolean("isVpn", false),
                            obj.optBoolean("isSslProxy", false),
                            token,
                            obj
                        )
                        isChecking.set(false)
                    }
                } else {
                    if (attempt < MAX_RETRIES) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            checkVpnAndMaintenance(attempt + 1)
                        }, 2000)
                    } else {
                        runOnUiThread {
                            isVpnChecked = true
                            isMaintenanceChecked = true
                            checkNetworkAndStartFlow()
                            isChecking.set(false)
                        }
                    }
                }
            } catch (e: Exception) {
                if (attempt < MAX_RETRIES) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        checkVpnAndMaintenance(attempt + 1)
                    }, 2000)
                } else {
                    runOnUiThread {
                        isVpnChecked = true
                        isMaintenanceChecked = true
                        checkNetworkAndStartFlow()
                        isChecking.set(false)
                    }
                }
            }
        }.start()
    }

    private fun handleWorkerResponse(
        maintenance: Boolean,
        isVpn: Boolean,
        isSslProxy: Boolean,
        token: String,
        obj: JSONObject
    ) {
        if (hasNavigated.get() || hasShownPermissionSheet.get()) return

        securePrefs.edit()
            .putBoolean("isVpn", isVpn)
            .putBoolean("isSslProxy", isSslProxy)
            .apply()

        when {
            maintenance -> {
                isUnderMaintenance = true
                if (!securePrefs.getBoolean("maintenance_notif_sent", false)) {
                    showMaintenanceNotification()
                    securePrefs.edit().putBoolean("maintenance_notif_sent", true).apply()
                }
                hasNavigated.set(true)
                timer?.cancel()
                timer = null
                val intent = Intent(this, FullMaintenanceActivity::class.java).apply {
                    putExtra("message", obj.optString("message", ""))
                }
                startActivity(intent)
                finish()
            }
            isVpn || isSslProxy -> {
                showVpnPopup()
            }
            else -> {
                isVpnChecked = true
                isMaintenanceChecked = true
                if (token.isNotEmpty()) {
                    securePrefs.edit().putString("deviceToken", token).apply()
                } else {
                    checkVpnAndMaintenance(1)
                    return
                }
                checkNetworkAndStartFlow()
            }
        }
    }

    private fun showVpnPopup() {
        if (isVpnDialogShowing || hasNavigated.get() || hasShownPermissionSheet.get()) return
        isVpnDialogShowing = true

        val view = layoutInflater.inflate(R.layout.popup_vpn, null)
        view.background = GradientDrawable().apply {
            cornerRadius = 21 * resources.displayMetrics.density
            setColor(Color.WHITE)
        }

        val button1 = view.findViewById<Button>(R.id.button1)
        button1.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(0xFF3949AB.toInt(), 0xFF5C6BC0.toInt())
        ).apply {
            cornerRadius = resources.displayMetrics.density * 10
            setStroke((2 * resources.displayMetrics.density).toInt(), 0xFF616161.toInt())
        }

        dialog = AlertDialog.Builder(this).setView(view).create()
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!isFinishing && !isDestroyed) {
            try {
                dialog?.show()
                dialog?.window?.setLayout(
                    (300 * resources.displayMetrics.density).toInt(),
                    (400 * resources.displayMetrics.density).toInt()
                )
                dialog?.window?.attributes?.gravity = Gravity.CENTER
            } catch (e: Exception) {
                isVpnDialogShowing = false
                return
            }
        } else {
            isVpnDialogShowing = false
            return
        }

        button1.setOnClickListener {
            isVpnDialogShowing = false
            dialog?.dismiss()
            Handler(Looper.getMainLooper()).postDelayed({
                checkVpnAndMaintenance(1)
            }, 1000)
        }
    }

    private fun checkNetworkAndStartFlow() {
        if (hasNavigated.get() || isInternetDialogShowing || isVpnDialogShowing || hasShownPermissionSheet.get()) return
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (hasNavigated.get() || hasShownPermissionSheet.get()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                showNotificationPermissionBottomSheet()
            } else {
                checkTutorialAndProceed()
            }
        } else {
            checkTutorialAndProceed()
        }
    }

    private fun checkTutorialAndProceed() {
        if (hasNavigated.get()) return
        
        val isTutorialCompleted = securePrefs.getBoolean("isTutorialCompleted", false)
        if (!isTutorialCompleted) {
            navigateToTutorial()
        } else {
            proceedToAuthFlow()
        }
    }

    private fun navigateToTutorial() {
        if (hasNavigated.get()) return
        hasNavigated.set(true)
        timer?.cancel()
        timer = null
        securePrefs.edit().putBoolean("isTutorialCompleted", true).apply()
        startActivity(Intent(this, TutorialActivity::class.java))
        finish()
    }

    private fun proceedToAuthFlow() {
        if (hasNavigated.get()) return
        hasNavigated.set(true)
        timer?.cancel()
        timer = null

        val intent = if (firebaseAuth.currentUser != null) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
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
    val hash = java.security.MessageDigest.getInstance("SHA-256")
        .digest(deviceInfo.toByteArray(Charsets.UTF_8))

    return "ANDROID_" + android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
        .replace("/", "_")
        .replace("+", "-")
        .replace("=", "")
        .take(16)
 }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    )
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }

    private fun showNotificationPermissionBottomSheet() {
        if (isFinishing || isDestroyed || hasNavigated.get() || hasShownPermissionSheet.get()) return
        hasShownPermissionSheet.set(true)

        timer?.cancel()
        timer = null

        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_notifications, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val btnAllow = bottomSheetView.findViewById<Button>(R.id.btn_allow_notifications)
        val tvLater = bottomSheetView.findViewById<android.widget.TextView>(R.id.tv_later)

        btnAllow.setOnClickListener {
            bottomSheetDialog.dismiss()
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        tvLater.setOnClickListener {
            bottomSheetDialog.dismiss()
            Handler(Looper.getMainLooper()).postDelayed({
                if (!hasNavigated.get()) {
                    checkTutorialAndProceed()
                }
            }, 300)
        }

        bottomSheetDialog.setCancelable(false)
        if (!isFinishing && !isDestroyed) {
            try {
                bottomSheetDialog.show()
            } catch (e: Exception) {
                if (!hasNavigated.get()) {
                    checkTutorialAndProceed()
                }
            }
        }
    }

    private fun _createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "earnzy_fcm_channel",
                "General Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Channel for general app notifications" }
            getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun showMaintenanceNotification() {
        val channelId = "maintenance_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Maintenance",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("App Maintenance")
            .setContentText("The app is currently under maintenance. Please try again later.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(2, builder.build())
            }
        } catch (e: Exception) {
            // No logging in release mode
        }
    }

    private fun showError(message: String) {
        if (!isFinishing && !isDestroyed) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (hasShownPermissionSheet.get()) {
            timer?.cancel()
            timer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        timer = null
        dialog?.dismiss()
        isChecking.set(true)
    }
}