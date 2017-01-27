package com.example.shoplocator.ui.createAndEditShop.presenter;

import com.example.shoplocator.ui.createAndEditShop.view.CreateAndEditShopFragment;
import com.example.shoplocator.ui.createAndEditShop.view.ICreateAndEditShopView;

/**
 * Created by seotm on 27.01.17.
 */

public interface ICreateAndEditShopPresenter {

    void bindView(ICreateAndEditShopView view);
    void unbindView();

}
