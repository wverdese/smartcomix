package com.shockdom.viewer;

import android.animation.Animator;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.shockdom.R;
import com.shockdom.model.SharedPrefsManager;
import com.shockdom.events.GreenRobot;
import com.shockdom.events.OnViewPagerTap;
import com.shockdom.model.SharedData;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Walt on 05/04/2015.
 */
public class ViewerFragment extends Fragment {

    private static final int STATE_HIDDEN = 0;
    private static final int STATE_ANIMATING = 1;
    private static final int STATE_VISIBLE = 2;

    private static final int MSG_RESET_TOOLBAR = 1;

    private static final long DELAY_ANIM_TOOLBAR = 200;
    private static final long DELAY_HIDE_TOOLBAR = 1500;

    public static final String IS_PREVIEW = "isPreview";
    public static final String VOLUME_ID = "id";

    private boolean isPreview;
    private String volume;
    private List<ViewerPage> pages;
    private int lastPos = -1;
    private int currentTrack = 0;

    private Toolbar toolbar;
    private ViewPager mViewPager;
    private MediaPlayer mediaPlayer;
    private MenuItem mMenuBookmark;

    private int toolbarState = STATE_VISIBLE;
    private int toolbarHeight = 0;

    private EventBus bus;
    private SharedPrefsManager spman;
    private SharedData data;

    public static ViewerFragment newInstance(boolean isPreview, String id) {
        ViewerFragment f = new ViewerFragment();
        Bundle b = new Bundle();
        b.putBoolean(IS_PREVIEW, isPreview);
        b.putString(VOLUME_ID, id);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //create UI
        View v = inflater.inflate(R.layout.fragment_viewer, container, false);
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        mViewPager = (ViewPager) v.findViewById(R.id.viewer);

        //fetch args
        bus = GreenRobot.getEventBus();
        data = SharedData.getInstance();
        spman = new SharedPrefsManager(getActivity(), SharedData.PREFS_BOOKMARKS);

        try {
            if (getArguments() != null) {
                volume = getArguments().getString(VOLUME_ID);
                isPreview = getArguments().getBoolean(IS_PREVIEW, false);
                pages = data.getViewerPages();
                toolbar.setTitle(data.getVolumeTitle(volume));
            }
        } catch (Exception e) {
            showMessage(R.string.error_pages_not_found);
            e.printStackTrace();
            return v;
        }

        if (savedInstanceState != null) {
            lastPos = savedInstanceState.getInt("lastPos", lastPos);
            currentTrack = savedInstanceState.getInt("currentTrack", currentTrack);
        }

        //init toolbar
        toolbar.getBackground().mutate();
        toolbar.setBackgroundColor(getResources().getColor(R.color.alpha_black));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        int[] actionBarAttr = new int[]{android.R.attr.actionBarSize};
        TypedArray a = getActivity().obtainStyledAttributes(actionBarAttr);
        toolbarHeight = (int) a.getDimension(0, 0) + 10;
        a.recycle();

        toolbar.inflateMenu(R.menu.menu_toolbar_viewer);
        mMenuBookmark = toolbar.getMenu().findItem(R.id.action_bookmark);
        if (isPreview) {
            for (int i=0; i < toolbar.getMenu().size(); i++)
                toolbar.getMenu().getItem(i).setVisible(false);
        }
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_restart) {
                    if (mViewPager.getCurrentItem() > 0) {
                        mViewPager.setCurrentItem(0, true);
                    }
                } else if (item.getItemId() == R.id.action_goto_bookmark) {
                    int bookmark = data.getBookmark(spman, volume);
                    if (bookmark != SharedData.NO_BOOKMARK)
                        mViewPager.setCurrentItem(bookmark, true);
                    else
                        showMessage(R.string.error_bookmark_not_found);
                } else if (item.getItemId() == R.id.action_bookmark) {
                    int bookmark = data.getBookmark(spman, volume);
                    int current = mViewPager.getCurrentItem();
                    if (bookmark == current) {
                        data.setBookmark(spman, volume, SharedData.NO_BOOKMARK);
                        showMessage(R.string.bookmark_removed);
                    } else {
                        data.setBookmark(spman, volume, current);
                        showMessage(R.string.bookmark_added);
                    }
                    redrawBookmark(current);
                }
                return true;
            }
        });

        //init pager
        mViewPager.setAdapter(new PagerAdapter(getChildFragmentManager()));
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                startMediaPlayer(position);
                redrawBookmark(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mViewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                startMediaPlayer(lastPos);
                requestHideToolbar();
            }
        });

        return v;

    }

    private void redrawBookmark(int current) {
        int bookmark = data.getBookmark(spman, volume);
        mMenuBookmark.setIcon((bookmark == current) ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_outline);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mediaPlayer != null) {
            outState.putInt("currentTrack", mediaPlayer.getCurrentPosition());
        }
        outState.putInt("lastPos", lastPos);
    }

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);

        if (!isPreview) {
            int current = data.getBookmark(spman, volume);
            int startPos = (lastPos != -1) ? lastPos
                    : (current != SharedData.NO_BOOKMARK) ? current
                    : 0;
            mViewPager.setCurrentItem(startPos, false);
            redrawBookmark(startPos);
            startMediaPlayer(startPos);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
        stopMediaPlayer();
    }

    public void onEvent(OnViewPagerTap e) {
        if (toolbarState == STATE_HIDDEN) {
            showToolbar();
        }
    }

    private void showToolbar() {
        toolbarState = STATE_ANIMATING;
        toolbar.animate()
                .translationY(0)
                .setDuration(DELAY_ANIM_TOOLBAR)
                .setListener(new Animator.AnimatorListener() {

                    public void onAnimationStart(Animator animation) {
                    }

                    public void onAnimationCancel(Animator animation) {
                    }

                    public void onAnimationRepeat(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isAdded()) {
                            toolbarState = STATE_VISIBLE;
                            requestHideToolbar();
                        }
                    }
                })
                .start();
    }

    private void requestHideToolbar() {
        new DelayHandler().sendEmptyMessageDelayed(MSG_RESET_TOOLBAR, DELAY_HIDE_TOOLBAR);
    }

    private void hideToolbar() {
        toolbarState = STATE_ANIMATING;
        toolbar.animate()
                .translationY(-toolbarHeight)
                .setDuration(DELAY_ANIM_TOOLBAR)
                .setListener(new Animator.AnimatorListener() {

                    public void onAnimationStart(Animator animation) {
                    }

                    public void onAnimationCancel(Animator animation) {
                    }

                    public void onAnimationRepeat(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isAdded()) {
                            toolbarState = STATE_HIDDEN;
                        }
                    }
                })
                .start();
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void startMediaPlayer(int pos) {

        if (pages == null || pos == -1) return;

        //disable if you want audio on rotation, return from bg
        //if (pos == lastPos) return;

        //just to prevent
        stopMediaPlayer();

        lastPos = pos;

        Uri uri = pages.get(pos).getAudioUri();
        if (uri != null) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(getActivity().getApplicationContext(), uri);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.seekTo(currentTrack);
                        currentTrack = 0;
                        mediaPlayer.start();
                    }
                });
            } catch (Exception e) {
                Log.e("TaskDetails", "Could not open file for playback.", e);
            }
        }
    }

    private void showMessage(int msgId) {
        Toast.makeText(getActivity(), getString(msgId), Toast.LENGTH_SHORT).show();
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Uri img = pages.get(position).getImageUri();
            return ImageFragment.newInstance(img);
        }

        @Override
        public int getCount() {
            return pages.size();
        }

    }

    private class DelayHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == MSG_RESET_TOOLBAR && isAdded()) {
                hideToolbar();
            }
        }
    }
}
