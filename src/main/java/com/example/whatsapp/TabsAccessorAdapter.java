package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

@SuppressWarnings("deprecation")
public class TabsAccessorAdapter extends FragmentPagerAdapter
{

    @SuppressWarnings("deprecation")
    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }


    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Fragment getItem(int i) {
        switch (i)
        {
            case 0:
                return new ChatsFragment();

            case 1:
                return new GroupsFragment();

            case 2:
                return new ContactsFragment();

            case 3:
                return new RequestFragment();

            case 4:
                return new StatusFragment();

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 5;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chats";

            case 1:
                return "Groups";

            case 2:
                return "Calls";

            case 3:
                return "Request";

            case 4:
                return "Status";

            default:
                return null;
        }
    }
}
