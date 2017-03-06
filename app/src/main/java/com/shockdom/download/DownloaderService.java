package com.shockdom.download;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.shockdom.model.SharedPrefsManager;
import com.shockdom.api.FilesApi;
import com.shockdom.api.WebService;
import com.shockdom.download.fs.FileStorageManager;

import java.util.ArrayList;

import retrofit.client.Response;

/**
 * Created by Walt on 23/05/2015.
 */
public class DownloaderService extends IntentService {

    public static final String PARAM_VOLUME_ID = "volume_id";
    public static final String PARAM_FILE_IDS = "file_arr";

    public static final String PARAM_IS_COMPLETE = "is_complete";
    public static final String PARAM_PERCENTAGE = "percent";
    public static final String PARAM_ERROR = "error";

    private FileStorageManager fsman;
    private SharedPrefsManager spman;

    public DownloaderService() {
        super("DownloaderService");
    }

    public static boolean sendRequest(Context c, SharedPrefsManager sp, String volume, ArrayList<Download> files) {
        int state = DownloadUtils.getVolumeState(sp, volume);
        if (state != DownloadUtils.STATE_DOWNLOADED) {
            Intent intent = new Intent(c, DownloaderService.class);
            intent.putExtra(PARAM_VOLUME_ID, volume);
            intent.putParcelableArrayListExtra(PARAM_FILE_IDS, files);
            c.startService(intent);
            return  true;
        }
        return false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int index = 0;
        int total = 1;

        String file = null;

        String volume = intent.getStringExtra(PARAM_VOLUME_ID);

        try {
            ArrayList<Download> items = intent.getParcelableArrayListExtra(PARAM_FILE_IDS);

            fsman = new FileStorageManager(getBaseContext());
            spman = new SharedPrefsManager(getBaseContext(), DownloadUtils.PREFS_STATES);

            FilesApi api = WebService.getFilesSyncApi();

            total = Math.max(items.size(), 1);
            for (int i=0; i<items.size(); i++) {
                file = items.get(i).getId();
                String type = items.get(i).getType();

                DownloadUtils.setVolumeState(spman, volume, DownloadUtils.STATE_DOWNLOADING); //just to be sure!

                Response response = api.getFile(type, file);

                if (!DownloadUtils.writeFile(fsman, type, file, response.getBody().in()))
                    throw new RuntimeException("file has not been written.");

                sendResponse(volume, false, i+1, total, null);
            }

            DownloadUtils.setVolumeState(spman, volume, DownloadUtils.STATE_DOWNLOADED);

            sendResponse(volume, true, total, total, null);

        } catch (Exception e) {
            DownloadUtils.setVolumeState(spman, volume, DownloadUtils.STATE_NONE);
            e.printStackTrace();
            String msg = (file != null) ? "Cannot download file "+index+"\\"+total+": "+file : "An error occurred";
            sendResponse(volume, false, index, total, msg);
        }

    }

    private void sendResponse(String volume, boolean isCompleted, int index, int total, String error) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_VOLUME_ID, volume);
        if (!isCompleted) {
            broadcastIntent.putExtra(PARAM_PERCENTAGE, (int) ((float) index*100 / (float) total));
            if (error != null)
                broadcastIntent.putExtra(PARAM_ERROR, error);
        } else {
            broadcastIntent.putExtra(PARAM_IS_COMPLETE, true);
        }
        sendBroadcast(broadcastIntent);
    }

}
