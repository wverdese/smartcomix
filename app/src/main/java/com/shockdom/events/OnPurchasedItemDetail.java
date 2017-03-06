package com.shockdom.events;

import com.anjlab.android.iab.v3.TransactionDetails;

/**
 * Created by Walt on 20/05/2015.
 */
public class OnPurchasedItemDetail {

    public String sku;
    public boolean isPurchased;
    public TransactionDetails details;

    public OnPurchasedItemDetail(String sku, boolean isPurchased, TransactionDetails details) {
        this.sku = sku;
        this.isPurchased = isPurchased;
        this.details = details;
    }
}
