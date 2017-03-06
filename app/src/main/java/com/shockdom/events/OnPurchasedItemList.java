package com.shockdom.events;

import java.util.List;

/**
 * Created by Walt on 20/05/2015.
 */
public class OnPurchasedItemList {

    public List<String> skus;

    public OnPurchasedItemList(List<String> skus) {
        this.skus = skus;
    }
}
