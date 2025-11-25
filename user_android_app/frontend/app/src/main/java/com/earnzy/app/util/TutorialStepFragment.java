package com.earnzy.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;

public class TutorialStepFragment extends Fragment {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_ANIMATION_RES_ID = "arg_animation_res_id";

    private TextView tvTitle;
    private TextView tvDescription;
    private LottieAnimationView lottieAnimationView;

    public static TutorialStepFragment newInstance(String title, String description, int animationResId) {
        TutorialStepFragment fragment = new TutorialStepFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putInt(ARG_ANIMATION_RES_ID, animationResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_step, container, false);

        tvTitle = view.findViewById(R.id.tv_title);
        tvDescription = view.findViewById(R.id.tv_description);
        lottieAnimationView = view.findViewById(R.id.lottie_animation_view);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString(ARG_TITLE));
            tvDescription.setText(getArguments().getString(ARG_DESCRIPTION));
            lottieAnimationView.setAnimation(getArguments().getInt(ARG_ANIMATION_RES_ID));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start animations when the fragment becomes visible to the user.
        lottieAnimationView.playAnimation();
    }

}    