package com.shockdom.viewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.shockdom.fragment.BaseFragmentManager;
import com.shockdom.R;

/**
 * Created by walt on 31/05/15.
 */
public class ViewerActivity extends AppCompatActivity {

    public static Intent newIntent(Context c, boolean isPreview, String id) {
        Intent intent = new Intent(c, ViewerActivity.class);
        intent.putExtra(ViewerFragment.IS_PREVIEW, isPreview);
        intent.putExtra(ViewerFragment.VOLUME_ID, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);

        Fragment bf = getSupportFragmentManager().findFragmentById(R.id.base_fragment_content);
        if (bf == null) {
            ViewerFragment f = new ViewerFragment();
            if (getIntent() != null && getIntent().getExtras() != null) {
                f.setArguments(getIntent().getExtras());
            }
            setFragment(f, false, false);
        }
    }

    public void setFragment(Fragment f, boolean addToBackStack, boolean animated) {
        BaseFragmentManager.replaceFragment(getSupportFragmentManager(), R.id.base_fragment_content, f, addToBackStack, animated);
    }
}
