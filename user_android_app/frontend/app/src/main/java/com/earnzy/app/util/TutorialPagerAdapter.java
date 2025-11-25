package com.earnzy.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * This adapter provides the fragments for the tutorial ViewPager.
 */
public class TutorialPagerAdapter extends FragmentStateAdapter {

    // The total number of tutorial pages.
    private static final int NUM_PAGES = 3;

    public TutorialPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // First slide
                return TutorialStepFragment.newInstance(
                        "Complete Simple Tasks",
                        "Finish easy tasks and surveys to earn points daily.",
                        R.raw.tutorial1
                );
            case 1:
                // Second slide
                return TutorialStepFragment.newInstance(
                        "Invite Your Friends",
                        "Share your referral code and earn bonus points for every friend who joins.",
                        R.raw.tutorial2
                );
            case 2:
                // Third slide
                return TutorialStepFragment.newInstance(
                        "Withdraw Your Earnings",
                        "Redeem your points for real cash and gift cards easily.",
                        R.raw.tutorial3
                );
            default:
                // This should never happen, but as a fallback, return an empty fragment.
                return new Fragment();
        }
    }

    /**
     * Returns the total number of pages in the tutorial.
     *
     * @return The number of tutorial slides.
     */
    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}