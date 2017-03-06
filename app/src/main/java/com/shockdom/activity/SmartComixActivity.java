package com.shockdom.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.shockdom.BuildVariantDelegate;
import com.shockdom.R;
import com.shockdom.Utils;
import com.shockdom.api.VolumesApi;
import com.shockdom.api.WebService;
import com.shockdom.api.json.ApiVersion;
import com.shockdom.api.json.Volume;
import com.shockdom.download.fs.SerializableStorageManager;
import com.shockdom.events.OnSyncData;
import com.shockdom.events.RequestSyncData;
import com.shockdom.fragment.BaseFragmentManager;
import com.shockdom.fragment.SplashFragment;
import com.shockdom.model.SharedData;
import com.shockdom.purchase.PurchaseActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Walt on 21/05/2015.
 */
public class SmartComixActivity extends PurchaseActivity {

    private VolumesApi api;
    private SharedData data;

    private SerializableStorageManager ssman = new SerializableStorageManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);

        api = WebService.getVolumesAsyncApi();
        data = SharedData.getInstance();

        Fragment bf = getSupportFragmentManager().findFragmentById(R.id.base_fragment_content);
        if (bf == null) {
            SplashFragment f = new SplashFragment();
            setFragment(f, false, false);

            onFirstStart(savedInstanceState);
        }
    }

    protected void onFirstStart(Bundle savedInstanceState) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            WebService.getVolumesAsyncApi().checkVersion(WebService.PLATFORM, info.versionCode, new CheckVersionCallback());
        } catch (Exception e) {
            Log.e(TAG, "Failed to send version code.", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        data.deserialize(ssman);
    }

    @Override
    protected void onStop() {
        data.serialize(ssman);
        super.onStop();
    }

    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.base_fragment_content);
    }

    public void setFragment(Fragment f, boolean addToBackStack, boolean animated) {
        BaseFragmentManager.replaceFragment(getSupportFragmentManager(), R.id.base_fragment_content, f, addToBackStack, animated);
    }

    public void removeFragment() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBillingInitialized() {
        super.onBillingInitialized();

        if (data.getVolumes().isEmpty() && !data.isRefreshing()) {
            refreshVolumes(false);
        } else {
            data.setVolumes(updatePrices(data.getVolumes()));
            endUpdate(true);
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        super.onPurchaseHistoryRestored();
        data.setVolumes(updatePrices(data.getVolumes()));
        endUpdate(true);
    }

    public void onEvent(RequestSyncData e) {
        refreshVolumes(e.isReload);
    }

    private void refreshVolumes(boolean isReload) {
        data.setIsRefreshing(true);
        fetchVolumes(new FetchAllVolumesCallback(isReload));
    }

    private void fetchVolumes(Callback<List<Volume>> callback) {
        api.getAllVolumes(callback);
    }

    private List<Volume> updatePrices(List<Volume> volumes) {
        List<Volume> list = new ArrayList<>();
        for (Volume v : volumes) {
            SkuDetails detail = purchaseListingDetail(v.getGooglePlayId());
            if (detail != null) {

                //purchased status
                v.setIsPurchased(isPurchased(v.getGooglePlayId()));
                //v.setIsPurchased(true);

                //prices
                double price = detail.priceValue;
                String priceText = detail.priceText;
                v.setPrice(price);
                v.setPriceText(priceText);

                //transaction id
                TransactionDetails transaction = purchaseTransactionDetails(v.getGooglePlayId());
                if (transaction != null) {
                    v.setTransactionId(transaction.purchaseToken);
                }

                list.add(v);
            } else {
                v.setIsPurchased(BuildVariantDelegate.skipPurchase());
                v.setPriceText(getString(R.string.inactive).toUpperCase());
                list.add(v);
            }
        }
        return list;
    }

    private void endUpdate(boolean success) {
        bus.post(new OnSyncData(success));
    }

    private void showMessage(int msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showUpdateDataDialog()
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.warning_title)
                .setMessage(R.string.update_your_client)
                .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.goto_store, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .show();
    }

    class CheckVersionCallback implements Callback<ApiVersion> {
        @Override
        public void success(ApiVersion apiVersion, Response response) {
            if (!apiVersion.isValidClient()) {
                Log.e(TAG, apiVersion.getMessage() + " (minVersion: "+apiVersion.getMinVersion()+")");
                showUpdateDataDialog();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "Failed to check server api version.", error);
        }
    }

    class FetchAllVolumesCallback implements Callback<List<Volume>> {

        private boolean isReload;

        public FetchAllVolumesCallback(boolean isReload) {
            this.isReload = isReload;
        }

        @Override
        public void success(List<Volume> volumes, Response response) {
            data.setVolumes(updatePrices(volumes));
            data.setIsRefreshing(false);
            endUpdate(true);
            if (isReload) showMessage(R.string.up_to_date);
        }

        @Override
        public void failure(RetrofitError error) {
            error.printStackTrace();
            data.setVolumes(updatePrices(data.getVolumes()));
            data.setIsRefreshing(false);
            endUpdate(false);
            showMessage(Utils.checkConnection(SmartComixActivity.this)
                    ? R.string.error_refresh
                    : R.string.error_no_connection);
        }

    }

}
