package com.shockdom;

import android.content.Context;

import com.shockdom.api.VolumesApi;
import com.shockdom.api.WebService;
import com.shockdom.api.json.Page;
import com.shockdom.api.json.Volume;
import com.shockdom.fragment.VolumeDetailFragment;

import java.util.List;

import retrofit.Callback;

public class BuildVariantDelegate {

    public static boolean skipPurchase() {
        return true;
    }

    public static void purchase(VolumeDetailFragment f, String sku) {
        f.onPurchaseCompleted(sku, "fake_token");
    }

    public static void fetchPages(Context c, Volume volume, Callback<List<Page>> callback) {
        VolumesApi api = WebService.getVolumesAsyncApi();
        api.getDebugPages(
                volume.getId(),
                BuildConfig.SERVER_ADMIN_USER,
                BuildConfig.SERVER_ADMIN_PASSWORD,
                callback
        );
    }

}
