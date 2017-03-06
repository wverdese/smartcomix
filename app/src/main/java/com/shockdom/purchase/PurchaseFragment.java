package com.shockdom.purchase;

import android.support.v4.app.Fragment;

import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.shockdom.events.GreenRobot;
import com.shockdom.events.NeedsRefresh;
import com.shockdom.events.OnPurchaseCompleted;
import com.shockdom.events.OnPurchaseError;
import com.shockdom.events.OnPurchaseListingDetail;
import com.shockdom.events.OnPurchasedItemDetail;
import com.shockdom.events.OnPurchasedItemList;
import com.shockdom.events.OnSubscribed;
import com.shockdom.events.RequestPurchase;
import com.shockdom.events.RequestPurchaseListingDetail;
import com.shockdom.events.RequestPurchasedItemDetail;
import com.shockdom.events.RequestPurchasedItemList;
import com.shockdom.events.RequestSubscription;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Walt on 20/05/2015.
 */
public class PurchaseFragment extends Fragment implements PurchaseListener {

    protected EventBus bus = GreenRobot.getEventBus();

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);
        requestSubscription();
    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
    }


    @Override
    public void requestSubscription() {
        bus.post(new RequestSubscription());
    }

    @Override
    public void requestPurchaseListingDetail(String sku) {
        bus.post(new RequestPurchaseListingDetail(sku));
    }

    @Override
    public void requestPurchase(String sku) {
        bus.post(new RequestPurchase(sku));
    }

    @Override
    public void requestPurchasedItemList() {
        bus.post(new RequestPurchasedItemList());
    }

    @Override
    public void requestPurchasedItemDetail(String sku) {
        bus.post(new RequestPurchasedItemDetail(sku));
    }

    public void onEvent(OnSubscribed e) {
        onSubscribed();
    }

    public void onEvent(OnPurchaseListingDetail e) {
        onPurchaseListingDetail(e.sku, e.details);
    }

    public void onEvent(OnPurchaseCompleted e) {
        onPurchaseCompleted(e.sku, e.details);
    }

    public void onEvent(OnPurchaseError e) {
        onPurchaseError(e.errCode, e.exception);
    }

    public void onEvent(OnPurchasedItemList e) {
        onPurchasedItemList(e.skus);
    }

    public void onEvent(OnPurchasedItemDetail e) {
        onPurchasedItemDetail(e.sku, e.isPurchased, e.details);
    }

    public void onEvent(NeedsRefresh e) {
        needsRefresh();
    }

    /* Override me! */

    public void onSubscribed() {}

    public void onPurchaseListingDetail(String sku, SkuDetails detail) {}

    public void onPurchaseCompleted(String sku, TransactionDetails detail) {}

    public void onPurchaseError(int errCode, Throwable exception) {}

    public void onPurchasedItemList(List<String> skus) {}

    public void onPurchasedItemDetail(String sku, boolean isPurchased, TransactionDetails detail) {}

    public void needsRefresh() {}
}
