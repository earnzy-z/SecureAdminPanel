# Shimmer Loading Implementation Guide

## Overview
Professional shimmer loading animations added to your Android app for all screens.

## Files Added
1. **shimmer_animation.xml** - Smooth alpha fade animation (1200ms, 40%-100% opacity)
2. **ShimmerHelper.kt** - Utility class for shimmer control
3. **skeleton_card_item.xml** - Card skeleton for list items
4. **skeleton_balance_card.xml** - Balance/header skeleton
5. **skeleton_list_container.xml** - Full list skeleton container

## How to Use in Your Activities/Fragments

### Basic Usage Pattern
```kotlin
// Show skeleton while loading
ShimmerHelper.showSkeleton(skeletonView, contentView)

// When data arrives, hide skeleton and show content
ShimmerHelper.hideSkeleton(skeletonView, contentView)
```

### In Your Activities
Update HomeActivity, EarnFragment, WalletFragment, ProfileFragment:

```kotlin
// In onCreate/onViewCreated
val skeletonLayout = findViewById<ViewGroup>(R.id.skeleton_container)
val contentLayout = findViewById<ViewGroup>(R.id.content_container)

// When starting to load data
ShimmerHelper.showSkeleton(skeletonLayout, contentLayout)

// In API response callback
ShimmerHelper.hideSkeleton(skeletonLayout, contentLayout)
```

### Layout Structure
Your layout XML should have this structure:
```xml
<FrameLayout>
    <!-- Skeleton (hidden by default) -->
    <include 
        android:id="@+id/skeleton_container"
        layout="@layout/skeleton_list_container"
        android:visibility="gone" />
    
    <!-- Content (shown by default) -->
    <NestedScrollView
        android:id="@+id/content_container">
        <!-- Your actual content -->
    </NestedScrollView>
</FrameLayout>
```

## Implementation Checklist

### Core Fragments (Priority 1)
- [ ] HomeFragment - Add skeleton for home cards/balance
- [ ] EarnFragment - Add skeleton for task list
- [ ] WalletFragment - Add skeleton for balance + transactions
- [ ] ProfileFragment - Add skeleton for profile data

### Additional Activities (Priority 2)
- [ ] OfferwallActivity - Add skeleton for offer cards
- [ ] LeaderboardActivity - Add skeleton for rankings
- [ ] ReferralActivity - Add skeleton for referral data
- [ ] AchievementsActivity - Add skeleton for achievement list
- [ ] RewardsHistoryActivity - Add skeleton for reward items
- [ ] SupportChatActivity - Add skeleton for messages

## Professional Features
✅ Smooth 1.2s shimmer animation
✅ Professional alpha fade effect
✅ Proper skeleton placeholders
✅ Easy to implement
✅ Memory efficient
✅ Works with all screen sizes

## Integration Steps

1. Copy ShimmerHelper.kt to your utils package
2. Copy animation XMLs to res/anim/
3. Copy skeleton layouts to res/layout/
4. Update your activity/fragment layouts to include skeleton containers
5. Call ShimmerHelper methods in your data loading code

## Example Implementation
```kotlin
class HomeFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val skeleton = view.findViewById<ViewGroup>(R.id.skeleton_container)
        val content = view.findViewById<ViewGroup>(R.id.content_container)
        
        // Show skeleton while loading
        ShimmerHelper.showSkeleton(skeleton, content)
        
        // Load data
        viewModel.loadData().observe(viewLifecycleOwner) { data ->
            // Hide skeleton and show content
            ShimmerHelper.hideSkeleton(skeleton, content)
            // Bind data to views
            bindData(data)
        }
    }
}
```

## Customization
- Adjust shimmer duration in shimmer_animation.xml (android:duration)
- Change skeleton colors in skeleton layouts (android:background)
- Add more skeleton variations as needed
