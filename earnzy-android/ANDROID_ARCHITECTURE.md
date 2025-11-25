# Earnzy Android App - Professional Architecture

## Project Structure

```
earnzy-android/
├── app/src/main/
│   ├── java/com/earnzy/
│   │   ├── MainActivity.kt                    # Fragment host activity
│   │   ├── api/
│   │   │   └── ApiClient.kt                   # Retrofit + Hono integration
│   │   ├── data/
│   │   │   └── models.kt                      # Data models & API responses
│   │   ├── ui/
│   │   │   ├── adapters/
│   │   │   │   ├── TaskAdapter.kt             # RecyclerView task adapter
│   │   │   │   └── LeaderboardAdapter.kt      # Leaderboard ranking adapter
│   │   │   ├── fragments/
│   │   │   │   ├── BaseFragment.kt            # Base fragment with utils
│   │   │   │   ├── DashboardFragment.kt       # Home screen with balance
│   │   │   │   ├── TasksFragment.kt           # Daily tasks list
│   │   │   │   ├── OffersFragment.kt          # Offerwall grid
│   │   │   │   ├── ProfileFragment.kt         # User profile & settings
│   │   │   │   └── LeaderboardFragment.kt     # Global rankings
│   │   │   └── theme/
│   │   │       └── Theme.kt                   # Material Design 3 theming
│   │   └── services/
│   │       └── FCMService.kt                  # Firebase notifications
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml              # Main activity layout
│   │   │   ├── fragment_dashboard.xml         # Dashboard UI
│   │   │   ├── fragment_tasks.xml             # Tasks RecyclerView
│   │   │   ├── item_task.xml                  # Task list item
│   │   │   ├── fragment_offers.xml            # Offers grid
│   │   │   ├── item_offer.xml                 # Offer card item
│   │   │   ├── fragment_leaderboard.xml       # Leaderboard UI
│   │   │   ├── item_leaderboard.xml           # Ranking item
│   │   │   └── fragment_profile.xml           # Profile UI
│   │   ├── values/
│   │   │   ├── strings.xml                    # All string resources
│   │   │   ├── colors.xml                     # Color palette
│   │   │   └── themes.xml                     # Theme & style definitions
│   │   ├── anim/
│   │   │   ├── slide_in_left.xml              # Enter animation
│   │   │   └── slide_out_right.xml            # Exit animation
│   │   └── menu/
│   │       └── bottom_nav_menu.xml            # Bottom navigation items
│   └── AndroidManifest.xml
├── build.gradle.kts                           # Build configuration
└── README.md
```

## UI/UX Architecture

### Design System
- **Color Scheme**: Purple primary (#6C5CE7) + Cyan secondary (#00D4FF)
- **Material Design 3**: Full implementation with Light/Dark themes
- **Typography**: 5-level hierarchy (Display, Headline, Title, Body, Label)
- **Spacing**: Consistent 16dp base padding, 8dp component spacing
- **Elevation**: Subtle shadows (2-4dp) for card hierarchy

### Layout Philosophy
- **Fragment-based**: Single Activity with Fragment container
- **XML-first**: Professional layout files instead of Compose for traditional Android feel
- **RecyclerView**: Efficient list rendering for tasks, offers, leaderboard
- **Responsive**: NestedScrollView for smooth scrolling with nested components
- **Animations**: Smooth transitions between screens (300ms slide animations)

### Screens (Fragments)

1. **Dashboard** (DashboardFragment)
   - Animated coins balance card
   - Quick stats boxes (Level, Completed, Streak)
   - Quick action buttons to other screens
   
2. **Tasks** (TasksFragment)
   - RecyclerView with TaskAdapter
   - Task cards with image, reward, category
   - Complete button with loading state
   
3. **Offers** (OffersFragment)
   - GridLayout 2-column offer display
   - Offer cards with image and claim button
   - Category-based organization
   
4. **Leaderboard** (LeaderboardFragment)
   - Top 3 podium with special styling
   - RecyclerView with LeaderboardAdapter
   - Rank badges and coin counts
   
5. **Profile** (ProfileFragment)
   - User info header card
   - Account stats display
   - Settings & logout button

## Data Layer

### API Integration
- **Base URL**: Cloudflare Workers (`https://api.earnzy.com/`)
- **Authentication**: JWT tokens via Authorization header
- **Interceptors**: Auto-attach token, handle errors
- **Response Types**: Strongly typed with Gson serialization

### API Client (Retrofit)
```kotlin
// All endpoints typed and documented
interface EarnzyApiService {
    @POST("auth/register") suspend fun register(body): AuthResponse
    @GET("tasks") suspend fun getTasks(): TasksResponse
    @POST("tasks/{id}/complete") suspend fun completeTask(id, body): ApiResponse
    // ... 20+ endpoints
}
```

## State Management

### Fragment Scope
- **Lifecycle**: Use `viewLifecycleOwner` for coroutine scope
- **Data Binding**: View binding with inflateBinding pattern
- **Adapters**: Pass callbacks to adapters for user interactions
- **Snackbar**: Base fragment provides error/success notifications

### Coroutines
- **Scope**: Fragment's viewLifecycleScope (auto-cancelled on destroy)
- **Async Operations**: API calls launch as suspending functions
- **Error Handling**: try-catch with user feedback

## Styling & Theming

### Material Design 3 Implementation
```kotlin
// Theme.kt defines:
- Color scheme (light/dark variants)
- Typography (5 text styles)
- Component styles (buttons, cards, etc.)
```

### XML Resources Organization
```
res/
├── values/strings.xml      # All UI text
├── values/colors.xml       # Color palette
├── values/themes.xml       # Styles & themes
├── anim/                   # Fragment animations
├── layout/                 # All layouts
└── menu/                   # Navigation menu
```

## Navigation

### Bottom Navigation
- 5 tabs: Home, Tasks, Offers, Leaderboard, Profile
- Fragment replacement on tab selection
- Back stack support with `addToBackStack()`

### Fragment Transitions
- Slide in from left (300ms)
- Slide out to right (300ms)
- Smooth alpha transitions

## Performance Optimizations

1. **RecyclerView**
   - ItemAnimator for smooth updates
   - ViewHolder pattern for efficient rendering
   - Adapter callbacks for user interactions

2. **Image Loading**
   - Coil library for async image loading
   - Memory caching
   - Placeholder & error handling

3. **Network**
   - Retrofit with OkHttp
   - Connection timeout: 30s
   - Request/response logging

4. **Lifecycle**
   - Fragment lifecycle-aware coroutines
   - ViewBinding for null-safe views
   - Proper resource cleanup on destroy

## Build Configuration

```gradle
// Compose + Traditional View support
buildFeatures {
    compose = true
    dataBinding = true
}

// Dependencies
- Fragment KTX + Navigation
- Retrofit + Gson
- Firebase Cloud Messaging
- Material Components 3
- Coroutines
```

## Pro-Level Features

✅ Material Design 3 theming (Light/Dark)
✅ Professional color scheme & typography
✅ Fragment-based architecture with proper lifecycle
✅ XML layouts with responsive design
✅ RecyclerView adapters with animations
✅ Retrofit API client with interceptors
✅ Proper error handling & user feedback
✅ Smooth fragment transitions
✅ Bottom navigation with back stack
✅ ViewBinding for null safety
✅ Coroutine-based async operations
✅ Firebase Cloud Messaging integration
✅ Production-ready code structure

## Deployment

### Debug Build
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Release Build
```bash
./gradlew bundleRelease
# Upload to Play Store
```

### Firebase Setup
1. Create Firebase project
2. Download google-services.json
3. Place in app/ directory
4. Firebase notifications auto-configured
