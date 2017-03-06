package com.shockdom.purchase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.shockdom.R;
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

import de.greenrobot.event.EventBus;

/**
 * Created by Walt on 20/05/2015.
 */
public class PurchaseActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    public static final String TAG = PurchaseActivity.class.getName();

    protected EventBus bus;

    private BillingProcessor mBillingProcessor = null;
    private boolean hasInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus = GreenRobot.getEventBus();

        String key = getString(R.string.iab_license);
        mBillingProcessor = new BillingProcessor(this, key, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (mBillingProcessor != null)
            mBillingProcessor.release();
        super.onDestroy();
    }

    private void requestOutOfSync(String request) {
        Toast.makeText(this, R.string.generic_error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Request out of sync: " + request);
    }

    public void onEvent(RequestSubscription e) {
        if (hasInitialized)
            bus.post(new OnSubscribed());
        //else wait
    }

    public void onEvent(RequestPurchaseListingDetail e) {
        if (hasInitialized) {
            bus.post(new OnPurchaseListingDetail(e.sku, purchaseListingDetail(e.sku)));
        } else {
            requestOutOfSync("Purchase Listing Detail");
        }
    }

    protected SkuDetails purchaseListingDetail(String sku) {
        return mBillingProcessor.getPurchaseListingDetails(sku);
    }

    public void onEvent(RequestPurchase e) {
        if (hasInitialized) {
            mBillingProcessor.purchase(this, e.sku);
        } else {
            requestOutOfSync("Purchase");
        }
    }

    public void onEvent(RequestPurchasedItemList e) {
        if (hasInitialized) {
            bus.post(new OnPurchasedItemList(mBillingProcessor.listOwnedProducts()));
        } else {
            requestOutOfSync("Purchase Listing Detail");
        }
    }

    protected boolean isPurchased(String sku) {
        return mBillingProcessor.isPurchased(sku);
    }

    protected TransactionDetails purchaseTransactionDetails(String sku) {
        return mBillingProcessor.getPurchaseTransactionDetails(sku);
    }

    public void onEvent(RequestPurchasedItemDetail e) {
        if (hasInitialized) {
            boolean isPurchased = isPurchased(e.sku);
            if (!isPurchased)
                bus.post(new OnPurchasedItemDetail(e.sku, isPurchased, null));
            else
                bus.post(new OnPurchasedItemDetail(e.sku, isPurchased, purchaseTransactionDetails(e.sku)));
        } else {
            requestOutOfSync("Purchase Listing Detail");
        }
    }

    @Override
    public void onBillingInitialized() {
        hasInitialized = true;
        bus.post(new OnSubscribed());
    }

    @Override
    public void onPurchaseHistoryRestored() {
        if (hasInitialized)
            bus.post(new NeedsRefresh());
    }

    @Override
    public void onProductPurchased(String s, TransactionDetails transactionDetails) {
        bus.post(new OnPurchaseCompleted(s, transactionDetails));
    }

    @Override
    public void onBillingError(int i, Throwable throwable) {
        bus.post(new OnPurchaseError(i, throwable));
    }


}
