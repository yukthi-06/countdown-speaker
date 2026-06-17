package com.example.speaktimer.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.speaktimer.fragments.CountdownFragment;
import com.example.speaktimer.fragments.CountupFragment;

public class TimerPagerAdapter extends FragmentStateAdapter {

    public TimerPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new CountdownFragment();
        } else {
            return new CountupFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
