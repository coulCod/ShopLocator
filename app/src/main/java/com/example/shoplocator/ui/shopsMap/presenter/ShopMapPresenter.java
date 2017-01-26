package com.example.shoplocator.ui.shopsMap.presenter;

import android.support.annotation.NonNull;

import com.example.shoplocator.buissines.shopsMap.IShopsMapInteractor;
import com.example.shoplocator.buissines.shopsMap.filtration.ShopListFilterModel;
import com.example.shoplocator.ui.model.ShopModel;
import com.example.shoplocator.ui.shopsMap.view.IShopMapView;

import java.util.List;

import rx.Single;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by {@author yura.savchuk22@gmail.com} on 25.01.17.
 */

public class ShopMapPresenter implements IShopMapPresenter {

    private static final int EMPTY_POSITION = -1;

    private final ShopsMapPresenterCash cash;
    private final IShopsMapInteractor shopsMapInteractor;

    private IShopMapView view;
    private CompositeSubscription compositeSubscription;

    public ShopMapPresenter(IShopsMapInteractor shopsMapInteractor) {
        this.shopsMapInteractor = shopsMapInteractor;
        cash = new ShopsMapPresenterCash();
    }

    @Override
    public void bindView(IShopMapView view) {
        this.view = view;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void unbindView() {
        compositeSubscription.clear();
        view = null;
    }

    @Override
    public void loadShopsAndControlAccessablity(Single<Object> single) {
        view.shopProgress(true);
        Subscription subscription = Single.zip(getShops(), single, (shops, o) -> shops)
                .subscribe(this::handleLoadShopsSuccess, this::handleLoadShopsError);
        compositeSubscription.add(subscription);
    }

    private Single<ShopListFilterModel> getShops() {
        if (cash.isShopListFilterModelExist()) {
            return Single.just(cash.getShopListFilterModel());
        }
        return shopsMapInteractor.getShopListFilterModel();
    }

    private void handleLoadShopsError(Throwable throwable) {
        view.shopProgress(false);
    }

    private void handleLoadShopsSuccess(ShopListFilterModel filterModel) {
        cash.setShopListFilterModel(filterModel);
        view.shopProgress(false);
        view.setupShopsList(filterModel.getUpdatableShops());
        view.setupShopMapkers(filterModel.getUpdatableShops());
        setMapCursorToSelectedPosition();
        view.setShopByPositionOnPager(cash.getSelectedShopPosition());
        if (cash.isQueryExist()) filterShopsWithQuery();
    }

    private void setMapCursorToSelectedPosition() {
        ShopModel shopModel = cash.getShopListFilterModel().getUpdatableShops().get(cash.getSelectedShopPosition());
        view.setMapCursorToCoordinate(shopModel.getCoordinate());
    }

    @Override
    public void onShopPositionChanged(int position) {
        cash.setSelectedShopPosition(position);
        setMapCursorToSelectedPosition();
    }

    @Override
    public void onQueryChanged(@NonNull String query) {
        cash.setQuery(query);
        if (cash.isShopListFilterModelExist()) {
            filterShopsWithQuery();
        }
    }

    private void filterShopsWithQuery() {
        List<ShopModel> shops = cash.getShopListFilterModel().getUpdatableShops();
        long shopId = getSelectedShopId(shops);
        shopsMapInteractor.filterShopList(cash.getShopListFilterModel(), cash.getQuery());
        view.setupShopsList(shops);
        restoreSelectedShopPosition(shops, shopId);
    }

    private long getSelectedShopId(List<ShopModel> shops) {
        if (shops.size() > cash.getSelectedShopPosition()
                && cash.getSelectedShopPosition() > 0) {
            return shops.get(cash.getSelectedShopPosition()).getId();
        }
        return EMPTY_POSITION;
    }

    private void restoreSelectedShopPosition(List<ShopModel> shops, long shopIdBeforeFilter) {
        if (!shops.isEmpty() && shopIdBeforeFilter != EMPTY_POSITION) {
            for (int i = 0; i < shops.size(); i++) {
                ShopModel shopModel = shops.get(i);
                if (shopModel.getId() == shopIdBeforeFilter) {
                    cash.setSelectedShopPosition(i);
                    view.setShopByPositionOnPager(i);
                    break;
                }
            }
        }
    }
}
