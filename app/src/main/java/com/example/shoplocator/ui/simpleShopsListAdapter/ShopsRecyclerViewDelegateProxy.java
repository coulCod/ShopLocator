package com.example.shoplocator.ui.simpleShopsListAdapter;

import android.view.View;

/**
 * Created by {@author yura.savchuk22@gmail.com} on 23.01.17.
 */

public class ShopsRecyclerViewDelegateProxy implements ShopsRecyclerViewDelegate {

    private ShopsRecyclerViewDelegate recyclerViewDelegate;

    public void setRecyclerViewDelegate(ShopsRecyclerViewDelegate recyclerViewDelegate) {
        this.recyclerViewDelegate = recyclerViewDelegate;
    }

    @Override
    public void onItemClick(int position, View itemView) {
        if (recyclerViewDelegate != null) recyclerViewDelegate.onItemClick(position, itemView);
    }
}
