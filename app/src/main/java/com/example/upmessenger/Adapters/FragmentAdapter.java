package com.example.upmessenger.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.upmessenger.Fragments.ChatFragment;
import com.example.upmessenger.Fragments.ProfileFragment;
import com.example.upmessenger.Fragments.StatusFragment;

public class FragmentAdapter extends FragmentPagerAdapter {

    public FragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position==0)
            return new ChatFragment();
        else if(position==1)
            return new StatusFragment();
            else if(position==2)
                return new ProfileFragment();
        return new ChatFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position==0)
            title="CHATS";
        else if(position==1)
                title="STATUS";
            else if (position==2)
                title="PROFILE";

        return title;
    }
}
