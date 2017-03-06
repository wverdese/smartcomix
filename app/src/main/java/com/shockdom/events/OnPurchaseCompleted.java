package com.shockdom.events;

import com.anjlab.android.iab.v3.TransactionDetails;

/**
 * Created by Walt on 20/05/2015.
 */
public class OnPurchaseCompleted {

    public String sku;
    public TransactionDetails details;

    public OnPurchaseCompleted(String sku, TransactionDetails details) {
        this.sku = sku;
        this.details = details;
    }
}
