package com.shockdom.api;

import android.util.Log;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

/**
 * Created by walt on 14/06/15.
 */
public class ApiErrorHandler {

    /** Fetch the error message and print it to console **/
    public static void printError(String tag, RetrofitError error) {
        Response response = (error != null) ? error.getResponse() : null;
        Object body = (response != null) ? error.getResponse().getBody() : null;
        if (body != null && body instanceof TypedByteArray) {
            String err = new String(((TypedByteArray) body).getBytes());
            Log.e(tag, "Error: " + err + ".", error);
        } else {
            Log.e(tag, "An error occurred.", error);
        }
    }

}
