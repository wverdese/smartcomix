package com.shockdom.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.TransactionDetails;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shockdom.BuildVariantDelegate;
import com.shockdom.R;
import com.shockdom.activity.SmartComixActivity;
import com.shockdom.api.ApiErrorHandler;
import com.shockdom.api.VolumesApi;
import com.shockdom.api.WebService;
import com.shockdom.api.json.Page;
import com.shockdom.api.json.Volume;
import com.shockdom.dialogs.AlertDialogs;
import com.shockdom.download.Download;
import com.shockdom.download.DownloadUtils;
import com.shockdom.download.DownloaderService;
import com.shockdom.download.ResponseReceiver;
import com.shockdom.download.fs.FileStorageManager;
import com.shockdom.events.OnSyncData;
import com.shockdom.fresco.FrescoHelper;
import com.shockdom.model.SharedData;
import com.shockdom.model.SharedPrefsManager;
import com.shockdom.purchase.PurchaseFragment;
import com.shockdom.purchase.PurchaseUtils;
import com.shockdom.viewer.ViewerActivity;
import com.shockdom.viewer.ViewerPage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Walt on 25/05/2015.
 */
public class VolumeDetailFragment extends PurchaseFragment {

    public static final String HTML_PAGES = "<strong>%s:</strong> %d<br/>";
    public static final String HTML_YEAR = "<strong>%s:</strong> %s<br/>";
    public static final String HTML_TYPE = "<strong>%s:</strong> %s<br/>";
    public static final String HTML_GENRE = "<strong>%s:</strong> %s<br/>";
    public static final String HTML_AUTHORS = "<p><strong>%s:</strong><br/>%s</p>";
    public static final String HTML_DESCRIPTION = "<p><strong>%s:</strong><br/>%s</p>";

    private Toolbar toolbar;
    private SimpleDraweeView mHeader;
    private TextView mTitle;
    private TextView mSubtitle;
    private View mSmartSonix;
    private TextView mText;
    private ProgressBar mProgressBar;

    private View mBtnSave;
    private View mBtnPreview;
    private View mBtnBuy;
    private View mBtnDownload;
    private View mBtnProgress;
    private View mBtnRead;

    private MenuItem mBtnDelete;
    private AlertDialog dialog;

    private VolumesApi api;
    private SharedData data;
    private SharedPrefsManager savedMan;
    private SharedPrefsManager stateMan;
    private DownloadReceiver downloadReceiver;
    private FileStorageManager filesMan;

    private String id;
    private Volume volume;

    private boolean showPreview;
    private boolean isPurchaseReady;

    public static VolumeDetailFragment newInstance(String id) {
        VolumeDetailFragment f = new VolumeDetailFragment();
        Bundle b = new Bundle();
        b.putString("id", id);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        WindowUtils.changeStatusBarColor(getActivity(), R.color.colorPrimaryDark);

        View v = inflater.inflate(R.layout.fragment_volume_detail, container, false);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_layout);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mHeader = (SimpleDraweeView) v.findViewById(R.id.header);
        mTitle = (TextView) v.findViewById(R.id.text_title);
        mSubtitle = (TextView) v.findViewById(R.id.text_volume);
        mText = (TextView) v.findViewById(R.id.text_detail);
        mSmartSonix = v.findViewById(R.id.smartsonix);
        mProgressBar = (ProgressBar) v.findViewById(R.id.download_progress);

        mBtnSave = v.findViewById(R.id.button_save);
        mBtnPreview = v.findViewById(R.id.button_preview);
        mBtnBuy = v.findViewById(R.id.button_buy);
        mBtnDownload = v.findViewById(R.id.button_download);
        mBtnProgress = v.findViewById(R.id.button_progress);
        mBtnRead = v.findViewById(R.id.button_read);

        if (getArguments() != null) {
            id = getArguments().getString("id");
        }

        if (savedInstanceState != null) {
            showPreview = savedInstanceState.getBoolean("showPreview", false);
        }

        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SmartComixActivity) getActivity()).removeFragment();
            }
        });

        toolbar.inflateMenu(R.menu.menu_volumes_detail);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int state = DownloadUtils.getVolumeState(stateMan, id);
                if (item.getItemId() == R.id.action_delete && state == DownloadUtils.STATE_DOWNLOADED) {
                    confirmDelete();
                }
                return true;
            }
        });
        mBtnDelete = toolbar.getMenu().findItem(R.id.action_delete);

        api = WebService.getVolumesAsyncApi();
        data = SharedData.getInstance();
        savedMan = new SharedPrefsManager(getActivity(), SharedData.PREFS_SAVED);
        stateMan = new SharedPrefsManager(getActivity(), DownloadUtils.PREFS_STATES);
        filesMan = new FileStorageManager(getActivity());

        refresh();

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("showPreview", showPreview);
    }


    @Override
    public void onResume() {
        super.onResume();
        downloadReceiver = new DownloadReceiver();
        ResponseReceiver.register(getActivity(), downloadReceiver);
    }

    @Override
    public void onPause() {
        ResponseReceiver.unregister(getActivity(), downloadReceiver);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        super.onPause();
    }

    @Override
    public void onSubscribed() {
        super.onSubscribed();
        isPurchaseReady = true;
    }

    public void onEvent(OnSyncData e) {
        refresh();
    }

    @Override
    public void needsRefresh() {
        refresh();
    }

    private void refresh() {
        volume = data.getVolume(id);
        if (volume != null) {
            updateTextInfos();
            updateButtons();
            updateHeader();
        }
    }

    private void updateTextInfos() {
        if (toolbar != null)
            toolbar.setTitle(volume.getTitle());

        if (volume.getSubtitle() != null) mTitle.setText(volume.getSubtitle());
        else mTitle.setText(volume.getTitle());

        if (volume.getNumber() != null)
            mSubtitle.setText(getString(R.string.card_volume, volume.getNumber()));
        else
            mSubtitle.setVisibility(View.GONE);

        mSmartSonix.setVisibility(volume.isSmartSonix() ? View.VISIBLE : View.GONE);

        StringBuilder html = new StringBuilder();
        if (volume.getPagesSize() > 0)
            html.append(String.format(HTML_PAGES, getString(R.string.detail_pages), volume.getPagesSize()));
        if (volume.getYear() != null)
            html.append(String.format(HTML_YEAR, getString(R.string.detail_year), volume.getYear()));
        if (volume.getType() != null)
            html.append(String.format(HTML_TYPE, getString(R.string.detail_type), volume.getType()));
        if (volume.getGenre() != null)
            html.append(String.format(HTML_GENRE, getString(R.string.detail_genre), volume.getGenre()));

        String authors = (volume.getAuthors() != null) ? volume.getAuthors()
                : (volume.getAuthorsCompact() != null) ? volume.getAuthorsCompact()
                : null;
        if (authors != null)
            html.append(String.format(HTML_AUTHORS, getString(R.string.detail_authors), authors));

        if (volume.getDescription() != null)
            html.append(String.format(HTML_DESCRIPTION, getString(R.string.detail_description), volume.getDescription()));

        mText.setText(Html.fromHtml(html.toString()));
    }

    private void updateButtons() {

        mBtnSave.setVisibility(View.VISIBLE);
        boolean isSaved = data.isSaved(savedMan, volume.getId());
        setupButtonImage(mBtnSave, (isSaved) ? R.drawable.ic_saved : R.drawable.ic_unsaved);
        setupButtonText(mBtnSave, (isSaved) ? getString(R.string.saved) : getString(R.string.save));
        mBtnSave.setOnClickListener(new OnButtonSaveListener());

        setupButtonImage(mBtnPreview, (showPreview) ? R.drawable.ic_wait : R.drawable.ic_preview);
        setupButtonText(mBtnPreview, (showPreview) ? getString(R.string.open_preview) : getString(R.string.preview));
        mBtnPreview.setOnClickListener(new OnButtonPreviewListener());

        setupButtonText(mBtnBuy, volume.getPriceText());
        mBtnBuy.setOnClickListener(new OnButtonBuyListener());
        mBtnDownload.setOnClickListener(new OnButtonDownloadListener());
        mBtnRead.setOnClickListener(new OnButtonReadListener());

        setDownloadState();
    }

    private void updateHeader() {
        mHeader.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mHeader.getViewTreeObserver().removeOnPreDrawListener(this);
                ViewGroup.LayoutParams lp = mHeader.getLayoutParams();
                lp.height = getResources().getDimensionPixelSize(R.dimen.detail_header_height);
                mHeader.setLayoutParams(lp);

                Uri uri = Uri.parse(WebService.getImageUri(volume.getPicture()));
                FrescoHelper.setImageURI(mHeader, uri);
                return false;
            }
        });
        mHeader.setOnClickListener(new OnCoverPreviewListener());
    }

    private void setDownloadState() {

        mBtnBuy.setVisibility(View.GONE);
        mBtnDownload.setVisibility(View.GONE);
        mBtnProgress.setVisibility(View.GONE);
        mBtnRead.setVisibility(View.GONE);

        mBtnPreview.setVisibility(View.VISIBLE);
        if (!volume.isPurchased()) {
            mBtnBuy.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        } else {
            mBtnBuy.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        if (mBtnDelete != null) mBtnDelete.setVisible(false);

        int state = DownloadUtils.getVolumeState(stateMan, id);
        switch (state) {
            case DownloadUtils.STATE_NONE:
                mBtnDownload.setVisibility(View.VISIBLE);
                mProgressBar.setIndeterminate(false);
                mProgressBar.setProgress(0);
                mProgressBar.setMax(100);
                break;
            case DownloadUtils.STATE_DOWNLOADING:
                mBtnProgress.setVisibility(View.VISIBLE);
                mProgressBar.setIndeterminate(true);
                break;
            case DownloadUtils.STATE_DOWNLOADED:
                mBtnPreview.setVisibility(View.GONE);
                mBtnRead.setVisibility(View.VISIBLE);
                if (mBtnDelete != null) mBtnDelete.setVisible(true);
                mProgressBar.setIndeterminate(false);
                mProgressBar.setProgress(100);
                mProgressBar.setMax(100);
                break;
        }
    }

    private void setupButtonImage(View v, int resId) {
        if (v instanceof TextView) {
            ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
        }
    }

    private void setupButtonText(View v, String txt) {
        if (v instanceof TextView) {
            ((TextView) v).setText(txt);
        }
    }

    private void startDownload() {
        DownloadUtils.setVolumeState(stateMan, id, DownloadUtils.STATE_DOWNLOADING);
        setDownloadState();
        fetchPages(getActivity(), volume, new PagesCallback());
    }

    private void fetchPages(Context c, Volume volume, Callback<List<Page>> callback) {
        BuildVariantDelegate.fetchPages(
                c,
                volume,
                callback
        );
    }

    @Override
    public void onPurchaseCompleted(String sku, TransactionDetails detail) {
        onPurchaseCompleted(sku, detail.purchaseToken);
    }

    public void onPurchaseCompleted(String sku, String token) {
        volume.setIsPurchased(true);
        volume.setTransactionId(token);
        setDownloadState();
    }

    @Override
    public void onPurchaseError(int errCode, Throwable exception) {
        Log.e("VolumeDetailFragment", PurchaseUtils.getErrorExplanation(errCode, exception));
        Toast.makeText(getActivity().getApplicationContext(), R.string.error_buy, Toast.LENGTH_SHORT).show();
    }

    private List<ViewerPage> createOnlineViewerPages(List<Page> pages) {
        List<ViewerPage> vp = new ArrayList<>();
        for (Page p : pages) {
            vp.add(new ViewerPage(
                    (p.getPicture() != null) ? Uri.parse(WebService.getImageUri(p.getPicture())) : null,
                    (p.getAudio() != null) ? Uri.parse(WebService.getAudioUri(p.getAudio())) : null
            ));
        }
        return vp;
    }

    private List<ViewerPage> createOfflineViewerPages(List<Page> pages) {
        List<ViewerPage> vp = new ArrayList<>();
        for (Page p : pages) {
            vp.add(new ViewerPage(
                    (p.getPicture() != null) ? Uri.fromFile(new File(DownloadUtils.getFilePath(filesMan, Download.TYPE_IMAGE, p.getPicture()))) : null,
                    (p.getAudio() != null) ? Uri.fromFile(new File(DownloadUtils.getFilePath(filesMan, Download.TYPE_AUDIO, p.getAudio()))) : null
            ));
        }
        return vp;
    }

    private void saveViewerPages(List<ViewerPage> viewerPages) {
        data.setViewerPages(viewerPages);
    }

    private void showViewer(boolean inPreview) {
        startActivity(ViewerActivity.newIntent(getActivity(), inPreview, volume.getId()));
    }

    private void showViewer(List<Page> pages, boolean inPreview, String ifError) {
        if (pages != null && pages.size() > 0) {
            List<ViewerPage> viewerPages = inPreview ? createOnlineViewerPages(pages) : createOfflineViewerPages(pages);
            saveViewerPages(viewerPages);
            showViewer(inPreview);
        } else {
            onError(ifError);
        }
    }

    private void onError(String error) {
        Toast.makeText(getActivity().getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete() {
        dialog = AlertDialogs.showSimpleChoiceDialog(
                getActivity(),
                getString(R.string.warning_title),
                getString(R.string.warning_delete_local_content),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        dialog.dismiss();
                        dialog = null;
                        deleteVolume();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        dialog.dismiss();
                        dialog = null;
                    }
                }
        );
    }

    private void deleteVolume() {
        //TODO perform real delete
        DownloadUtils.setVolumeState(stateMan, id, DownloadUtils.STATE_NONE);
        data.setBookmark(new SharedPrefsManager(getActivity(), SharedData.PREFS_BOOKMARKS), id, SharedData.NO_BOOKMARK);
        setDownloadState();
    }

    private class OnButtonSaveListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            boolean isSaved = !data.isSaved(savedMan, volume.getId());
            data.setIsSaved(savedMan, volume.getId(), isSaved);
            setupButtonImage(mBtnSave, (isSaved) ? R.drawable.ic_saved : R.drawable.ic_unsaved);
            setupButtonText(mBtnSave, (isSaved) ? getString(R.string.saved) : getString(R.string.save));
            Toast.makeText(getActivity().getApplicationContext(), (isSaved) ? R.string.msg_saved : R.string.msg_unsaved, Toast.LENGTH_SHORT).show();
        }
    }

    private class OnButtonPreviewListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!showPreview) {
                showPreview = true;
                setupButtonImage(mBtnPreview, R.drawable.ic_wait);
                setupButtonText(mBtnPreview, getString(R.string.open_preview));
                fetchPreviews(volume.getId(), new PreviewCallback());
            }
        }
    }

    private void fetchPreviews(String id, Callback<List<Page>> callback) {
        api.getVolumePreviews(id, callback);
    }

    private class OnButtonBuyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isPurchaseReady) {
                BuildVariantDelegate.purchase(VolumeDetailFragment.this, volume.getGooglePlayId());
            }
        }
    }

    private class OnButtonDownloadListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int downloadState = DownloadUtils.getVolumeState(stateMan, volume.getId());
            if (downloadState == DownloadUtils.STATE_NONE) {
                startDownload();
            }
        }
    }

    private class OnButtonReadListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int downloadState = DownloadUtils.getVolumeState(stateMan, volume.getId());
            String error = getString(R.string.error_read);
            if (downloadState == DownloadUtils.STATE_DOWNLOADED) {
                try {
                    List<Page> pages = data.getPages(volume.getId());
                    if (pages.size() > 0) {
                        showViewer(pages, false, error);
                    } else {
                        DownloadUtils.setVolumeState(stateMan, volume.getId(), DownloadUtils.STATE_NONE);
                        startDownload();
                    }
                } catch (Exception e) {
                    onError(error);
                }
            } else {
                onError(error);
            }
        }
    }

    private class OnCoverPreviewListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            List<Page> pages = new ArrayList<>();
            pages.add(volume.getCoverPreview());
            String error = getString(R.string.error_preview_cover);
            try {
                showViewer(pages, true, error);
            } catch (Exception e) {
                e.printStackTrace();
                onError(error);
            }
        }
    }

    private class PreviewCallback implements Callback<List<Page>> {

        String error = getString(R.string.error_preview);

        @Override
        public void success(List<Page> pages, Response response) {
            if (isAdded()) {
                if (pages != null && !pages.isEmpty()) {

                    try {
                        showViewer(pages, true, error);
                    } catch (Exception e) {
                        e.printStackTrace();
                        onError(error);
                    }

                } else {
                    onError(error);
                }

                showPreview = false;
                setupButtonImage(mBtnPreview, R.drawable.ic_preview);
                setupButtonText(mBtnPreview, getString(R.string.preview));
            }
        }

        @Override
        public void failure(RetrofitError e) {
            e.printStackTrace();
            if (isAdded()) {
                onError(error);
                showPreview = false;
                setupButtonImage(mBtnPreview, R.drawable.ic_preview);
                setupButtonText(mBtnPreview, getString(R.string.preview));
            }
        }
    }

    private class PagesCallback implements Callback<List<Page>> {

        @Override
        public void success(List<Page> pages, retrofit.client.Response response) {
            try {
                data.setPages(volume.getId(), pages);
                ArrayList<Download> files = new ArrayList<>();
                for (Page p : pages) {
                    if (p.getPicture() != null)
                        files.add(new Download(Download.TYPE_IMAGE, p.getPicture()));
                    if (p.getAudio() != null)
                        files.add(new Download(Download.TYPE_AUDIO, p.getAudio()));
                }
                DownloaderService.sendRequest(getActivity(), stateMan, id, files);
            } catch (Exception e) {
                e.printStackTrace();
                DownloadUtils.setVolumeState(stateMan, id, DownloadUtils.STATE_NONE);
                if (isAdded()) {
                    setDownloadState();
                }
                Toast.makeText(getActivity().getApplicationContext(), R.string.error_download, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            ApiErrorHandler.printError(this.getClass().getSimpleName(), error);
            DownloadUtils.setVolumeState(stateMan, id, DownloadUtils.STATE_NONE);
            if (isAdded()) {
                setDownloadState();
            }
            Toast.makeText(getActivity().getApplicationContext(), R.string.error_download, Toast.LENGTH_SHORT).show();
        }

    }

    private class DownloadReceiver extends ResponseReceiver {


        @Override
        public void onProgress(String volume_id, int percentage) {
            if (isAdded()) {
                mProgressBar.setIndeterminate(false);
                mProgressBar.setProgress(percentage);
                setupButtonText(mBtnProgress, percentage + "%");
            }
        }

        @Override
        public void onComplete(String volume_id) {
            if (isAdded()) {
                setDownloadState();
            }
            Toast.makeText(getActivity().getApplicationContext(), R.string.download_completed, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String volume_id, String error) {
            if (isAdded()) {
                setDownloadState();
            }
            Toast.makeText(getActivity().getApplicationContext(), R.string.error_download, Toast.LENGTH_SHORT).show();
        }

    }

}