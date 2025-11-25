# Earnzy Android App - Deployment Guide

## Build Instructions

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
# Create release keystore
keytool -genkey -v -keystore earnzy-release.keystore -alias earnzy \
  -keyalg RSA -keysize 2048 -validity 10000

# Build signed APK
./gradlew assembleRelease

# Or build App Bundle for Play Store
./gradlew bundleRelease
```

## Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project
3. Add Android app
4. Download `google-services.json`
5. Place in `app/` directory
6. Enable Cloud Messaging API

## Cloudflare Workers API

Update API endpoint in `ApiClient.kt`:
```kotlin
private const val BASE_URL = "https://your-workers-domain.com/"
```

## Admin Panel Integration

The Android app connects to:
- **Cloudflare Workers**: For real-time data (tasks, offers, referrals)
- **Admin Panel**: Backend manages user data, withdrawals, bans

All admin actions reflect in the app within seconds.

## Testing

### Local Testing
```bash
./gradlew installDebug
adb shell am start -n com.earnzy/.MainActivity
```

### Firebase Notifications Test
Send test notification from Firebase Console to verify delivery.

### API Testing
```bash
# Test Workers API
curl https://api.earnzy.com/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Monitoring

- **Firebase Crashlytics**: Automatic crash reporting
- **Firebase Analytics**: User behavior tracking
- **Admin Panel**: User management and support

All data flows to admin panel for decision making.
