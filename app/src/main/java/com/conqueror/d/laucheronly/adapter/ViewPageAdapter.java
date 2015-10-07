package com.conqueror.d.laucheronly.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.List;


public class ViewPageAdapter extends PagerAdapter {

    List<View> viewLists;

    public ViewPageAdapter(List<View> lists)
    {
        viewLists = lists;
    }


    @Override
    public int getCount() {
        return viewLists.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(View view, int position, Object object)                       //Ïú»ÙItem
    {
        ((ViewPager) view).removeView(viewLists.get(position));
    }

    @Override
    public Object instantiateItem(View view, int position)                                //ÊµÀý»¯Item
    {
        ((ViewPager) view).addView(viewLists.get(position), 0);

        return viewLists.get(position);
    }
}
