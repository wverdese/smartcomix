package com.shockdom.api.json;

import java.io.Serializable;

/**
 * Created by Walt on 23/05/2015.
 */
public class Page implements Serializable {

    private String _id;
    private String picture;
    private String audio;
    private int progressive;

    public Page() {
    }

    public Page(String _id, String picture, String audio, int progressive) {
        this._id = _id;
        this.picture = picture;
        this.audio = audio;
        this.progressive = progressive;
    }

    public String getId() {
        return _id;
    }

    public String getPicture() {
        return picture;
    }

    public String getAudio() {
        return audio;
    }

    public int getProgressive() {
        return progressive;
    }
}
