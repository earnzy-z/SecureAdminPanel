# Earnzy Android App

A feature-rich earning app for Android built with Jetpack Compose and Kotlin.

## Features

- 📱 **Tasks**: Complete daily tasks to earn coins
- 🎁 **Offerwall**: Claim offers and earn rewards
- 👥 **Referral System**: Refer friends and earn bonuses
- 🎟️ **Promo Codes**: Redeem promo codes for bonus coins
- 💰 **Rewards**: Withdraw coins via multiple payment methods
- 🔔 **Push Notifications**: Real-time notifications via Firebase Cloud Messaging

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM
- **Networking**: Retrofit + OkHttp
- **Dependency Injection**: Hilt
- **Navigation**: Jetpack Navigation Compose
- **Notifications**: Firebase Cloud Messaging

## Project Structure

```
earnzy-android/
├── app/
│   └── src/main/
│       ├── java/com/earnzy/
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   └── models.kt              # Data classes
│       │   ├── api/
│       │   │   └── ApiClient.kt           # Retrofit + interceptors
│       │   ├── ui/
│       │   │   ├── screens/
│       │   │   │   ├── TasksScreen.kt
│       │   │   │   ├── OfferwallScreen.kt
│       │   │   │   ├── ReferralScreen.kt
│       │   │   │   ├── PromoScreen.kt
│       │   │   │   └── RewardsScreen.kt
│       │   │   └── theme/
│       │   │       └── Theme.kt
│       │   └── services/
│       │       └── FCMService.kt          # Firebase notifications
│       └── AndroidManifest.xml
├── build.gradle.kts
└── README.md
```

## Setup

### Prerequisites
- Android Studio 2023.1+
- Android 7.0 (API 24) or higher
- Kotlin 1.9.10+

### Build

1. **Clone repository**
```bash
git clone https://github.com/yourusername/earnzy-android.git
cd earnzy-android
```

2. **Configure API endpoint** (in `ApiClient.kt`):
```kotlin
private const val BASE_URL = "https://api.earnzy.com/"
```

3. **Build APK**
```bash
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build
```

4. **Install on device**
```bash
./gradlew installDebug
```

## API Integration

The app communicates with the Cloudflare Workers backend:

**Base URL**: `https://api.earnzy.com/`

### Authenticated Endpoints

All endpoints except `/auth/register` require:
```
Authorization: Bearer {token}
```

### Key Endpoints

- `POST /api/auth/register` - Register device
- `GET /api/tasks` - Get all tasks
- `POST /api/tasks/{id}/complete` - Complete task
- `GET /api/offers` - Get offers
- `POST /api/offers/{id}/claim` - Claim offer
- `GET /api/referral/code` - Get referral code
- `GET /api/coins/balance` - Get coin balance
- `GET /api/promos` - Get promo codes
- `POST /api/promos/redeem` - Redeem promo
- `GET /api/rewards` - Get withdrawal methods
- `POST /api/rewards/request` - Request redemption

See [earnzy-workers/README.md](../earnzy-workers/README.md) for complete API docs.

## Firebase Cloud Messaging

1. Create Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add Android app to Firebase project
3. Download `google-services.json` and place in `app/` directory
4. Firebase notifications will be received by `FCMService`

## Architecture

### MVVM Pattern
- **Models**: Data classes in `data/models.kt`
- **Views**: Composable functions in `ui/screens/`
- **ViewModels**: (Can be added for complex state management)

### API Layer
- Retrofit service interface in `api/ApiClient.kt`
- OkHttp interceptor for authentication
- Error handling with try-catch

### Navigation
- Bottom navigation between 5 main screens
- Compose Navigation for screen switching

## Testing

```bash
# Unit tests
./gradlew testDebug

# Instrumented tests
./gradlew connectedAndroidTest
```

## Performance Tips

- Lazy loading for lists
- Image caching with Coil
- Coroutine-based async operations
- ViewModel persistence across configuration changes

## Publishing

1. **Generate release key**
```bash
keytool -genkey -v -keystore ~/earnzy-release.keystore -alias earnzy -keyalg RSA -keysize 2048 -validity 10000
```

2. **Sign APK** in `build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("earnzy-release.keystore")
        // Add your credentials
    }
}
```

3. **Build signed APK**
```bash
./gradlew bundleRelease
```

4. **Upload to Google Play Store**
- Use Google Play Console
- Upload `.aab` file (Android App Bundle)

## Troubleshooting

### API Connection Issues
- Check BASE_URL is correct
- Verify Cloudflare Workers backend is running
- Check network permissions in `AndroidManifest.xml`

### Firebase Notifications Not Working
- Verify `google-services.json` is in `app/` directory
- Check Firebase project configuration
- Enable Cloud Messaging in Firebase Console

### Build Errors
- Run `./gradlew clean build`
- Clear Android Studio cache: File → Invalidate Caches

## Contributing

1. Create feature branch: `git checkout -b feature/feature-name`
2. Commit changes: `git commit -m "Add feature"`
3. Push to branch: `git push origin feature/feature-name`
4. Open Pull Request

## License

MIT License - see LICENSE file

## Support

For issues:
- GitHub Issues: Report bugs
- Email: support@earnzy.com
