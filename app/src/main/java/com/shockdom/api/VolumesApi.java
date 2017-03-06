package com.shockdom.api;

import com.shockdom.api.json.ApiVersion;
import com.shockdom.api.json.Page;
import com.shockdom.api.json.Volume;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Walt on 23/05/2015.
 */
public interface VolumesApi {

    @GET("/api/checkVersion")
    public void checkVersion(@Query("platform") String platform, @Query("version") int versionCode, Callback<ApiVersion> callback);

    @GET("/api/volumes")
    public void getAllVolumes(Callback<List<Volume>> callback);

    @GET("/api/volumes/{volume}")
    public void getVolume(@Path("volume") String volume, Callback<Volume> callback);

    @GET("/api/volumes/{volume}/previews")
    public void getVolumePreviews(@Path("volume") String volume, Callback<List<Page>> callback);

    @GET("/api/volumes/{volume}/pages")
    public void getVolumePages(@Path("volume") String volume, @Query("platform") String platform, @Query("receipt") String receipt, @Query("product") String product, @Query("package_name") String package_name, Callback<List<Page>> callback);

    @GET("/api/volumes/{volume}/pages/{page}")
    public void getVolumePage(@Path("volume") String volume, @Path("page") String page, @Query("platform") String platform, @Query("receipt") String receipt, @Query("product") String product, @Query("package_name") String package_name, Callback<Page> callback);

    @GET("/api/volumes/{volume}/debug")
    public void getDebugPages(@Path("volume") String volume, @Query("username") String username, @Query("password") String password, Callback<List<Page>> callback);

}
