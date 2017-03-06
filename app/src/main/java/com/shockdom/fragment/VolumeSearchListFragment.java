package com.shockdom.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shockdom.R;
import com.shockdom.activity.SmartComixActivity;
import com.shockdom.api.json.Volume;

import java.util.List;

/**
 * Created by walt on 06/06/15.
 */
public class VolumeSearchListFragment extends VolumeListFragment {

    private EditText mSearchBar;

    private String search;

    public static VolumeSearchListFragment newInstance(int filter) {
        VolumeSearchListFragment f = new VolumeSearchListFragment();
        Bundle b = new Bundle();
        b.putInt("filter", filter);
        f.setArguments(b);
        return f;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        WindowUtils.changeStatusBarColor(getActivity(), R.color.colorPrimaryDark);
        return inflater.inflate(R.layout.fragment_volume_list_search, container, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        mSearchBar = (EditText) v.findViewById(R.id.search_edtx);
        mSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                    String s = mSearchBar.getText().toString();
                    if (!s.isEmpty()) {
                        search = s;
                        refresh();
                        setImeVisibility(false);
                        return true;
                    }
                }

                return false;
            }
        });

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar_list);
        toolbar.setTitle(R.string.action_search);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImeVisibility(false);
                ((SmartComixActivity) getActivity()).removeFragment();
            }
        });

        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mSearchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                setImeVisibility(hasFocus);
            }
        });
        mSearchBar.requestFocus();

        return v;
    }

    @Override
    protected List<Volume> loadVolumes() {
        return data.getVolumes(savedMan, filter, search);
    }

    private Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showSoftInput(mSearchBar, 0);
            }
        }
    };

    private void setImeVisibility(final boolean visible) {
        if (visible) {
            mSearchBar.post(mShowImeRunnable);
        } else {
            mSearchBar.removeCallbacks(mShowImeRunnable);
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);
            }
        }
    }

}
