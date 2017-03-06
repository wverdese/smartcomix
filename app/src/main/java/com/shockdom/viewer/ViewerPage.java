package com.shockdom.viewer;

import android.net.Uri;

/**
 * Created by walt on 30/05/15.
 */
public class ViewerPage {

    private Uri imageUri;
    private Uri audioUri;

    public ViewerPage(Uri imageUri, Uri audioUri) {
        this.imageUri = imageUri;
        this.audioUri = audioUri;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public Uri getAudioUri() {
        return audioUri;
    }
}
