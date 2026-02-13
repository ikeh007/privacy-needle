package com.jaims.privacyneedle.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jaims.privacyneedle.ui.fragments.CategoryFragment;

import java.util.List;

public class CategoryPagerAdapter extends FragmentStateAdapter {

    private final List<Integer> categoryIds;

    public CategoryPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Integer> categoryIds) {
        super(fragmentActivity);
        this.categoryIds = categoryIds;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int categoryId = categoryIds.get(position);
        return CategoryFragment.newInstance(categoryId);
    }

    @Override
    public int getItemCount() {
        return categoryIds.size();
    }
}

