package com.shockdom.viewer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.DraweeView;
import com.shockdom.R;
import com.shockdom.events.GreenRobot;
import com.shockdom.events.OnViewPagerTap;
import com.shockdom.fresco.FrescoHelper;

/**
 * Fragment che visualizza un media a fullscreen, all'interno di un paginatore
 */
public class ImageFragment extends Fragment {

    public static final String TAG = ImageFragment.class.getName();

    private static final String RES = "uri";

    public static ImageFragment newInstance(Uri uri) {
        ImageFragment f = new ImageFragment();
        Bundle b = new Bundle();
        b.putParcelable(RES, uri);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_image, container, false);

        DraweeView media = (DraweeView) v.findViewById(R.id.zoomable_view);

        media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GreenRobot.getEventBus().post(new OnViewPagerTap());
            }
        });

        if (getArguments() != null) {
            Uri uri = getArguments().getParcelable(RES);
            FrescoHelper.setImageURI(media, uri);
        }

        return v;
    }

}
