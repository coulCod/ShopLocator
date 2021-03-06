package com.example.shoplocator.ui.users.detail.presenter;

import android.support.annotation.NonNull;
import android.view.View;

import com.example.shoplocator.buissines.userDetail.IUserDetailInteractor;
import com.example.shoplocator.ui.model.ShopModel;
import com.example.shoplocator.ui.users.detail.view.IUserDetailView;
import com.example.shoplocator.util.rx.schedulers.RxSchedulersAbs;

import java.util.List;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by {@author yura.savchuk22@gmail.com} on 29.01.17.
 */

public class UserDetailPresenter implements IUserDetailPresenter {

    private final IUserDetailInteractor interactor;
    private final UserDetailPresenterCash cash;
    private final RxSchedulersAbs rxSchedulers;

    private IUserDetailView view;
    private CompositeSubscription compositeSubscription;

    public UserDetailPresenter(IUserDetailInteractor interactor, RxSchedulersAbs rxSchedulers) {
        cash = new UserDetailPresenterCash();
        this.interactor = interactor;
        this.rxSchedulers = rxSchedulers;
    }

    @Override
    public void setUserId(String userId) {
        cash.setUserId(userId);
    }

    @Override
    public void setUserName(String userName) {
        cash.setUserName(userName);
    }

    @Override
    public void bindView(IUserDetailView view) {
        this.view = view;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void unbindView() {
        compositeSubscription.clear();
        view = null;
    }

    @Override
    public void setupTitle() {
        view.setTitle(cash.getUserName());
    }

    @Override
    public void setupUserShops() {
        if (cash.isShopsExist()) {
            setupShopsFromCash();
        } else {
            setupShopsFromInteractor();
        }
    }

    private void setupShopsFromCash() {
        view.setupShopsList(cash.getShops());
    }

    private void setupShopsFromInteractor() {
        view.setProgress(true);
        Subscription subscription = interactor.getShopsByUserId(cash.getUserId(), cash.getUserName())
                .compose(rxSchedulers.getIOToMainTransformerSingle())
                .subscribe(this::handleGetShopsByUserIdSuccesss, this::handleGetShopsByUserIdError);
        compositeSubscription.add(subscription);
    }

    private void handleGetShopsByUserIdSuccesss(List<ShopModel> shops) {
        view.setProgress(false);
        cash.setShops(shops);
        setupShopsFromCash();
    }

    private void handleGetShopsByUserIdError(Throwable throwable) {
        view.setProgress(false);
        view.shopErrorView(true);
    }

    @Override
    public void onItemClick(int position, View itemView) {
        view.showDetailShopView(cash.getShops().get(position).getId(), itemView);
    }

    @Override
    public void onShopHasBeenRemovedFromDetailView(@NonNull String shopId) {
        List<ShopModel> shops = cash.getShops();
        for (int i=0; i<shops.size(); i++) {
            ShopModel item = shops.get(i);
            if (item.getId().equals(shopId)) {
                shops.remove(i);
                view.notifyShopItemRemoved(i);
                break;
            }
        }
    }

    @Override
    public void onShopHasBeenEditedFromDetailView(@NonNull String shopId) {
        view.setProgress(true);
        Subscription subscription = interactor.getShopById(shopId, cash.getUserId(), cash.getUserName())
                .compose(rxSchedulers.getIOToMainTransformerSingle())
                .subscribe(this::handleUpdateShopByIdSuccess, throwable -> view.setProgress(false));
        compositeSubscription.add(subscription);
    }

    private void handleUpdateShopByIdSuccess(@NonNull ShopModel shopModel) {
        view.setProgress(false);
        List<ShopModel> shops = cash.getShops();
        for (int i=0; i<shops.size(); i++) {
            ShopModel item = shops.get(i);
            if (item.getId().equals(shopModel.getId())) {
                shops.set(i, shopModel);
                view.notifyShopItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onRetryButtonClick() {
        view.shopErrorView(false);
        setupUserShops();
    }

}
