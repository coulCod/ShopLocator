package com.example.shoplocator.data.db.shops;

import android.support.annotation.NonNull;

import com.example.shoplocator.data.db.ListPrinter;
import com.example.shoplocator.data.db.client.IDatabaseClient;
import com.example.shoplocator.data.db.shops.mapper.ShopRealmObjectMapper;
import com.example.shoplocator.data.db.shops.model.ShopRealmObject;
import com.example.shoplocator.data.model.ShopDbModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Single;

/**
 * Created by seotm on 23.01.17.
 */

public class ShopsDBService implements IShopsDBService {

    private static final String FIELD_SHOP_ID = "id";

    private final IDatabaseClient client;

    public ShopsDBService(IDatabaseClient client) {
        this.client = client;
    }

    @Override
    public Single<List<ShopDbModel>> getShops() {
        return Single.fromCallable(() -> client.getRealm().where(ShopRealmObject.class).findAll())
                .map(ShopRealmObjectMapper::mapRealmToDb);
    }

    @Override
    public Single<ShopDbModel> getShopById(@NonNull String shopId) {
        return Single.fromCallable(() -> client.getRealm().where(ShopRealmObject.class).equalTo(FIELD_SHOP_ID, shopId).findFirst())
                .flatMap(shopRealmObject -> {
                    if (shopRealmObject == null) {
                        return Single.error(new RuntimeException("Shop by id: " + shopId + " is missing."));
                    }
                    return Single.just(ShopRealmObjectMapper.mapRealmToDb(shopRealmObject));
                });
    }

    @Override
    public void setShops(@NonNull List<ShopDbModel> shops) {
        List<ShopRealmObject> realmShops = ShopRealmObjectMapper.mapDbToRealm(shops);
        Realm realm = client.getRealm();
        realm.beginTransaction();
        realm.where(ShopRealmObject.class).findAll().deleteAllFromRealm();
        for (ShopRealmObject shop : realmShops) {
            realm.copyToRealm(shop);
        }
        realm.commitTransaction();
    }

    @Override
    public Single<Object> deleteShopsByIds(@NonNull Collection<String> ids) {
        return Single.fromCallable(() -> {
            Realm realm = client.getRealm();
            Collection<ShopRealmObject> shops = getShopsFromRealmByIds(realm, ids);
            realm.beginTransaction();
            for (ShopRealmObject shop : shops) {
                shop.deleteFromRealm();
            }
            realm.commitTransaction();
            return null;
        });
    }

    @NonNull
    private Collection<ShopRealmObject> getShopsFromRealmByIds(@NonNull Realm realm, @NonNull Collection<String> ids) {
        Collection<ShopRealmObject> realmObjects = new ArrayList<>();
        for (String id : ids) {
            ShopRealmObject realmObject = realm.where(ShopRealmObject.class)
                    .equalTo(FIELD_SHOP_ID, id).findFirst();
            if (realmObject != null) realmObjects.add(realmObject);
        }
        return realmObjects;
    }

    @Override
    public Single<Object> addShop(@NonNull ShopDbModel shop) {
        return Single.fromCallable(() -> {
            ShopRealmObject realmShop = ShopRealmObjectMapper.mapDbToRealm(shop);
            Realm realm = client.getRealm();
            realm.beginTransaction();
            deleteShopByIdFromRealmIfExist(realmShop.getId(), realm);
            realm.copyToRealm(realmShop);
            realm.commitTransaction();
            return null;
        });
    }

    private void deleteShopByIdFromRealmIfExist(String shopId, Realm realm) {
        if (!realm.isInTransaction()) throw new RuntimeException("Realm must be in transaction state in this scope.");
        ShopRealmObject oldRealmShop = realm.where(ShopRealmObject.class)
                .equalTo(FIELD_SHOP_ID, shopId).findFirst();
        if (oldRealmShop != null) oldRealmShop.deleteFromRealm();
    }

    @Override
    public Single<Object> addShops(@NonNull Collection<ShopDbModel> shops) {
        return Single.fromCallable(() -> {
            Realm realm = client.getRealm();
            realm.beginTransaction();
            for (ShopDbModel shop : shops) {
                ShopRealmObject realmShop = ShopRealmObjectMapper.mapDbToRealm(shop);
                deleteShopByIdFromRealmIfExist(realmShop.getId(), realm);
                realm.copyToRealm(realmShop);
            }
            realm.commitTransaction();
            return null;
        });
    }

    @Override
    public Single<List<ShopDbModel>> getShopsByUserId(@NonNull String userId) {
        return Single.fromCallable(() -> client.getRealm().where(ShopRealmObject.class)
                .equalTo("ownerId", userId).findAll())
                .map(ShopRealmObjectMapper::mapRealmToDb);
    }

    @Override
    public Single<String> getDbStructure() {
        return Single.fromCallable(() -> {
            Realm realm = client.getRealm();
            RealmResults<ShopRealmObject> shops = realm.where(ShopRealmObject.class).findAll();
            return new ListPrinter(shops).toString();
        });
    }
}
