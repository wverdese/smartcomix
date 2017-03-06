package com.shockdom.api;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Walt on 23/05/2015.
 */
public interface FilesApi {

        public static final String TYPE_IMAGES = "images";
        public static final String TYPE_AUDIO = "audio";

        @GET("/api/files/{type}/{file}")
        public Response getFile(@Path("type") String type, @Path("file") String file);

}
