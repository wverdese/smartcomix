package com.shockdom.events;

import com.anjlab.android.iab.v3.SkuDetails;

/**
 * Created by Walt on 20/05/2015.
 */
public class OnPurchaseListingDetail {

    public String sku;
    public SkuDetails details;

    public OnPurchaseListingDetail(String sku, SkuDetails details) {
        this.sku = sku;
        this.details = details;
    }
}
