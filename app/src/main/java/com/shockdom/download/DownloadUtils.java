package com.shockdom.download;

import com.shockdom.model.SharedPrefsManager;
import com.shockdom.download.fs.FileStorageManager;
import com.shockdom.download.fs.SerializableStorageManager;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by Walt on 23/05/2015.
 */
public class DownloadUtils {

    public static final String PREFS_STATES = "states";
    public static final String OBJECTS_FOLDER = "objects";

    public static final int STATE_NONE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_DOWNLOADED = 2;

    /******** FILES ********/

    public static String getFilePath(FileStorageManager fsman, String type, String id) {
        return new File(fsman.getStorageDirectory(type), id).getPath();
    }

    public static boolean writeFile(FileStorageManager fsman, String type, String id, InputStream is) throws Exception {
        return fsman.writeInStorage(type, id, is);
    }

    public static File readFile(FileStorageManager fsman, String type, String id) throws Exception {
        return fsman.readFromStorage(type, id);
    }

    /******** OBJECT ********/

    public static boolean serializeObject(SerializableStorageManager ssman, String id, Serializable o) throws Exception {
        return ssman.writeInStorage(OBJECTS_FOLDER, id, o);
    }

    public static Serializable deserializeObject(SerializableStorageManager ssman, String id) throws Exception {
        return ssman.readFromStorage(OBJECTS_FOLDER, id);
    }

    /******** STATES ********/

    public static void setVolumeState(SharedPrefsManager spman, String volume, int state) {
        if (state == STATE_NONE)
            spman.clearPreference(volume);
        else
            spman.saveInt(volume, state);
    }

    public static int getVolumeState(SharedPrefsManager spman, String volume) {
        return spman.loadInt(volume, STATE_NONE);
    }

}
