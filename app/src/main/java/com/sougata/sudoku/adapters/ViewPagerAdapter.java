package com.sougata.sudoku.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sougata.Constants;
import com.sougata.sudoku.fragments.DetailsStatisticsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return DetailsStatisticsFragment.newInstance(Constants.LEVEL_NAME[0], Constants.TYPES[0]);
        } else if (position == 1) {
            return DetailsStatisticsFragment.newInstance(Constants.LEVEL_NAME[1], Constants.TYPES[0]);
        } else if (position == 2) {
            return DetailsStatisticsFragment.newInstance(Constants.LEVEL_NAME[2], Constants.TYPES[0]);
        } else if (position == 3) {
            return DetailsStatisticsFragment.newInstance(Constants.LEVEL_NAME[3], Constants.TYPES[0]);
        } else {
            return DetailsStatisticsFragment.newInstance(Constants.LEVEL_NAME[4], Constants.TYPES[0]);
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
