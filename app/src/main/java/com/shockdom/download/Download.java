package com.shockdom.download;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Walt on 24/05/2015.
 */
public class Download implements Parcelable {

    public static final String TYPE_IMAGE = "images";
    public static final String TYPE_AUDIO = "audio";

    private String type;
    private String id;

    public Download(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    // Parcelling part
    public Download(Parcel in) {
        String[] data = new String[2];

        in.readStringArray(data);
        this.id = data[0];
        this.type = data[1];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.id,
                this.type});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Download createFromParcel(Parcel in) {
            return new Download(in);
        }

        public Download[] newArray(int size) {
            return new Download[size];
        }
    };
}
