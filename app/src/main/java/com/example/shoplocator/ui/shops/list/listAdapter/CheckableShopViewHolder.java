package com.example.shoplocator.ui.shops.list.listAdapter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;

import com.example.shoplocator.R;
import com.example.shoplocator.ui.simpleShopsListAdapter.ShopViewHolder;
import com.example.shoplocator.ui.simpleShopsListAdapter.ShopsRecyclerViewDelegate;
import com.example.shoplocator.util.sugar.CompareUtil;

import butterknife.BindView;

/**
 * Created by {@author yura.savchuk22@gmail.com} on 22.01.17.
 */

public class CheckableShopViewHolder extends ShopViewHolder {

    @BindView(R.id.viewSelection) View viewSelection;

    private boolean selectionState;

    public CheckableShopViewHolder(View itemView, @NonNull ShopsRecyclerViewDelegate delegate) {
        super(itemView, delegate);
        itemView.setOnClickListener(v -> delegate.onItemClick(getAdapterPosition(), itemView));
    }

    public void updateTransitionName() {
        String transitionName = itemView.getContext().getString(R.string.transition_shop_image) + getAdapterPosition();
        ViewCompat.setTransitionName(imageView, transitionName);
    }

    public void setSelection(boolean selectionState) {
        Log.d("TAG", "was selected: " + this.selectionState + " now selected: " + selectionState);
        if (!CompareUtil.isEqual(this.selectionState, selectionState)) {
            this.selectionState = selectionState;
            makeViewSelected(selectionState);
        }
    }

    private void makeViewSelected(boolean selected) {
        Log.d("TAG", "makeViewSelected: " + selected);
        viewSelection.setVisibility(selected ? View.VISIBLE : View.GONE);
    }
}
