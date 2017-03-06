package com.shockdom.purchase;

import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.util.List;

/**
 * Created by Walt on 20/05/2015.
 */
public interface PurchaseListener {

    public void requestSubscription(); //is it ready?
    public void requestPurchaseListingDetail(String sku); //how much?
    public void requestPurchase(String sku); //can I buy it?
    public void requestPurchasedItemList(); //what is mine?
    public void requestPurchasedItemDetail(String sku); //is it mine?

    public void onSubscribed(); //now is ready
    public void onPurchaseListingDetail(String sku, SkuDetails detail); //here's the price
    public void onPurchaseCompleted(String sku, TransactionDetails detail); //here you are
    public void onPurchaseError(int errCode, Throwable exception); //there's a problem
    public void onPurchasedItemList(List<String> skus); //you own these
    public void onPurchasedItemDetail(String sku, boolean isPurchased, TransactionDetails detail); //you don't own it / here's the receipt

    public void needsRefresh(); //something has changed!

}
