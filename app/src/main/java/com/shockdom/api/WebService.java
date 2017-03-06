package com.shockdom.api;

import com.shockdom.BuildConfig;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by Walt on 23/05/2015.
 */
public class WebService {

    public static final String HOST = BuildConfig.SERVER_HOST;
    public static final int TIMEOUT_SECONDS = 60;

    public static final String PLATFORM = "google";

    private static OkHttpClient httpClient = null;
    private static VolumesApi smartcomix = null;
    private static FilesApi files = null;

    protected static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
            httpClient.setReadTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        return httpClient;
    }

    public static VolumesApi getVolumesAsyncApi() {
        if (smartcomix == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(HOST)
                    .setClient(new OkClient(getHttpClient()))
                    .build();
            smartcomix = restAdapter.create(VolumesApi.class);
        }
        return smartcomix;
    }

    public static FilesApi getFilesSyncApi() {
        if (files == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(HOST)
                    .setClient(new OkClient(getHttpClient()))
                    .build();
            files = restAdapter.create(FilesApi.class);
        }
        return files;
    }

    public static String getImageUri(String id) {
        return HOST + "/api/files/images/" + id;
    }

    public static String getAudioUri(String id) {
        return HOST + "/api/files/audio/" + id;
    }

}
