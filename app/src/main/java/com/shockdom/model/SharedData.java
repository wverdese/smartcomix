package com.shockdom.model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.shockdom.api.json.Page;
import com.shockdom.api.json.Volume;
import com.shockdom.download.DownloadUtils;
import com.shockdom.download.fs.SerializableStorageManager;
import com.shockdom.viewer.ViewerPage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by walt on 30/05/15.
 */
public class SharedData {

    private static final String TAG = SharedData.class.getSimpleName();

    private static final String SERIALIZED_VOLUMES_ID = "17d83f46403a4c6db51d097be6759e9c";
    private static final String SERIALIZED_PAGES_ID = "bd17ca61b35b4920b21b7144d253bb5f";

    public static final String PREFS_BOOKMARKS = "bookmarks";
    public static final String PREFS_SAVED="prefs_saved";

    public static final int NO_BOOKMARK = -1;

    public static final int FILTER_NONE = 0;
    public static final int FILTER_SMARTCOMIX = 1;
    public static final int FILTER_SMARTSONIX = 2;
    public static final int FILTER_PURCHASED = 3;
    public static final int FILTER_SAVED = 4;

    private boolean isRefreshing;

    //serialized on the fs as objects to have offline features
    private List<Volume> volumes;
    private Map<String, List<Page>> pages;

    private List<ViewerPage> viewerPages;

    private static SharedData instance;

    public static SharedData getInstance() {
        if (instance == null)
            instance = new SharedData();
        return instance;
    }

    private SharedData() {
        volumes = new ArrayList<>();
        pages = new HashMap<>();

        viewerPages = new ArrayList<>();
    }

    public void serialize(SerializableStorageManager ssman) {
        try {
            DownloadUtils.serializeObject(ssman, SERIALIZED_VOLUMES_ID, (Serializable) volumes);
            DownloadUtils.serializeObject(ssman, SERIALIZED_PAGES_ID, (Serializable) pages);
        } catch (Exception e) {
            Log.e(TAG, "Cannot serialize data.", e);
        }
    }

    public void deserialize(SerializableStorageManager ssman) {
        try {
            List<Volume> volumes = (List<Volume>) DownloadUtils.deserializeObject(ssman, SERIALIZED_VOLUMES_ID);
            if (volumes != null) this.volumes = volumes;
            Map<String, List<Page>> pages = (Map<String, List<Page>>) DownloadUtils.deserializeObject(ssman, SERIALIZED_PAGES_ID);
            if (pages != null) this.pages = pages;
        } catch (Exception e) {
            Log.e(TAG, "Cannot deserialize data.", e);
        }
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setIsRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
    }

    @NonNull
    public List<Volume> getVolumes() {
        return volumes;
    }

    private boolean passFilter(SharedPrefsManager spman, Volume v, int filter) {
        return  filter == FILTER_SMARTCOMIX && !v.isSmartSonix() ||
                filter == FILTER_SMARTSONIX && v.isSmartSonix() ||
                filter == FILTER_PURCHASED && v.isPurchased() ||
                filter == FILTER_SAVED && isSaved(spman, v.getId()) ||
                filter == FILTER_NONE;
    }

    private boolean containsWord(Volume v, String search) {
        return  v.getTitle() != null && v.getTitle().toUpperCase().contains(search.toUpperCase()) ||
                v.getAuthors() != null && v.getAuthors().toUpperCase().contains(search.toUpperCase()) ||
                v.getGenre() != null && v.getGenre().toUpperCase().contains(search.toUpperCase());
    }

    @NonNull
    public String getVolumeTitle(String id) {
        for (Volume v: volumes) {
            if (v.getId().equals(id))
                return v.getTitle();
        }
        return "";
    }

    @NonNull
    public List<Volume> getVolumes(SharedPrefsManager spman, int filter) {

        List<Volume> list = new ArrayList<>();
        for (Volume v: volumes) {
            if (passFilter(spman, v, filter))
                list.add(v);
        }

        return list;
    }

    @NonNull
    public List<Volume> getVolumes(SharedPrefsManager spman, int filter, String search) {
        List<Volume> list = new ArrayList<>();
        if (search != null && !search.equals("")) {
            for (Volume v : volumes) {
                if (passFilter(spman, v, filter) && containsWord(v, search))
                    list.add(v);
            }
        }
        return list;
    }

    public Volume getVolume(String id) {
        for (Volume v: volumes) {
            if (v.getId().equals(id))
                return v;
        }
        return null;
    }

    public void setVolumes(@NonNull List<Volume> volumes) {
        this.volumes = volumes;
    }

    @NonNull
    public List<Page> getPages(@NonNull String volume) {
        List<Page> toret = pages.get(volume);
        if (toret == null) toret = new ArrayList<>();
        return toret;
    }

    public void setPages(@NonNull String volume, @NonNull List<Page> pages) {
        this.pages.put(volume, pages);
    }

    @NonNull
    public List<ViewerPage> getViewerPages() {
        return viewerPages;
    }

    public void setViewerPages(@NonNull List<ViewerPage> pages) {
        viewerPages = pages;
    }

    public boolean isSaved(SharedPrefsManager spman, String volumeId) {
        return spman.loadBoolean(volumeId, false);
    }

    public void setIsSaved(SharedPrefsManager spman, String volumeId, boolean isSaved) {
        if (!isSaved)
            spman.clearPreference(volumeId);
        else
            spman.saveBoolean(volumeId, isSaved);
    }

    public int getBookmark(SharedPrefsManager spman, String volumeId) {
        return spman.loadInt(volumeId, NO_BOOKMARK);
    }

    public void setBookmark(SharedPrefsManager spman, String volumeId, int bookmark) {
        if (bookmark == NO_BOOKMARK)
            spman.clearPreference(volumeId);
        else
            spman.saveInt(volumeId, bookmark);
    }

}
