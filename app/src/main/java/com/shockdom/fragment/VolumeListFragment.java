package com.shockdom.fragment;

import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.shockdom.R;
import com.shockdom.activity.SmartComixActivity;
import com.shockdom.api.WebService;
import com.shockdom.api.json.Volume;
import com.shockdom.events.OnSyncData;
import com.shockdom.fresco.FrescoHelper;
import com.shockdom.model.SharedData;
import com.shockdom.model.SharedPrefsManager;
import com.shockdom.purchase.PurchaseFragment;
import com.shockdom.toolbar.ToolbarCardList;

import java.util.List;

/**
 * Created by Walt on 24/05/2015.
 */
public class VolumeListFragment extends PurchaseFragment {

    protected RecyclerView recyclerView;
    private View progressBar;
    private TextView textHint;

    protected SharedData data;
    protected SharedPrefsManager savedMan;

    protected int filter;

    public static VolumeListFragment newInstance(int filter) {
        VolumeListFragment f = new VolumeListFragment();
        Bundle b = new Bundle();
        b.putInt("filter", filter);
        f.setArguments(b);
        return f;
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_volume_list, container, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflateView(inflater, container, savedInstanceState);

        if (getArguments() != null) {
            filter = getArguments().getInt("filter");
        }

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.cards_columns)));
        progressBar = v.findViewById(R.id.progress);
        textHint = (TextView) v.findViewById(R.id.text_hint);

        data = SharedData.getInstance();
        savedMan = new SharedPrefsManager(getActivity(), SharedData.PREFS_SAVED);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    public void onEvent(OnSyncData e) {
        refresh();
    }

    @Override
    public void needsRefresh() {
        refresh();
    }

    protected void refresh() {
        List<Volume> volumes = loadVolumes();
        if (volumes.isEmpty()) {
            if (data.isRefreshing()) {
                progressBar.setVisibility(View.VISIBLE);
                textHint.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                textHint.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            textHint.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            RecyclerAdapter recyclerAdapter = new RecyclerAdapter(volumes);
            recyclerView.setAdapter(recyclerAdapter);
        }
    }

    protected List<Volume> loadVolumes() {
        return data.getVolumes(savedMan, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Fragment f = getParentFragment();
        if (f != null && f instanceof ToolbarCardList)
            ((ToolbarCardList) f).addScrollableContent(recyclerView);
    }

    @Override
    public void onPause() {
        Fragment f = getParentFragment();
        if (f != null && f instanceof ToolbarCardList)
            ((ToolbarCardList) f).removeScrollableContent(recyclerView);
        super.onPause();
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder> {

        private List<Volume> volumes;

        public RecyclerAdapter(List<Volume> volumes) {
            this.volumes = volumes;
        }

        @Override
        public RecyclerAdapter.RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return new RecyclerHolder(inflater.inflate(R.layout.fragment_volume_card, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerAdapter.RecyclerHolder holder, int position) {
            Volume v = volumes.get(position);

            FrescoHelper.setImageURI(holder.getCover(), Uri.parse(WebService.getImageUri(v.getPicture())));

            holder.getTitle().setText((v.getTitle() != null) ? v.getTitle() : "");
            holder.getVolume().setText((v.getNumber() != null) ? getString(R.string.card_volume, v.getNumber()) : "");
            holder.getAuthors().setText((v.getAuthorsCompact() != null) ? v.getAuthorsCompact() : "");
            holder.getSavedIcon().setImageResource(data.isSaved(savedMan, v.getId()) ? R.drawable.ic_saved : R.drawable.ic_unsaved);
            holder.getSonixIcon().setVisibility(v.isSmartSonix() ? View.VISIBLE : View.GONE);

            String price = (v.isPurchased()) ? getString(R.string.card_owned).toUpperCase()
                         : (v.getPriceText() != null) ? v.getPriceText()
                         : null;
            holder.getPrice().setText(price);
            holder.getPrice().setVisibility((price != null) ? View.VISIBLE : View.GONE);

            holder.getCard().setTag(v.getId());
            holder.getCard().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VolumeDetailFragment f = VolumeDetailFragment.newInstance((String) v.getTag());
                    ((SmartComixActivity) getActivity()).setFragment(f, true, true);
                }
            });
        }

        @Override
        public int getItemCount() {
            return volumes.size();
        }

        class RecyclerHolder extends RecyclerView.ViewHolder {

            private View card;
            private SimpleDraweeView cover;
            private TextView title;
            private TextView volume;
            private TextView authors;
            private ImageView savedIcon;
            private View sonixIcon;
            private TextView price;

            public RecyclerHolder(View itemView) {
                super(itemView);
            }

            public View getCard() {
                if (card == null)
                    card = itemView.findViewById(R.id.card);
                return card;
            }

            public SimpleDraweeView getCover() {
                if (cover == null) {
                    cover = (SimpleDraweeView) itemView.findViewById(R.id.card_cover);
                    cover.getHierarchy().setActualImageFocusPoint(new PointF(0, 0));
                }
                return cover;
            }

            public TextView getTitle() {
                if (title == null)
                    title = (TextView) itemView.findViewById(R.id.card_title);
                return title;
            }

            public TextView getVolume() {
                if (volume == null)
                    volume = (TextView) itemView.findViewById(R.id.card_subtitle);
                return volume;
            }

            public TextView getAuthors() {
                if (authors == null)
                    authors = (TextView) itemView.findViewById(R.id.card_authors);
                return authors;
            }

            public ImageView getSavedIcon() {
                if (savedIcon == null)
                    savedIcon = (ImageView) itemView.findViewById(R.id.card_toggle_saved);
                return savedIcon;
            }

            public View getSonixIcon() {
                if (sonixIcon == null)
                    sonixIcon = itemView.findViewById(R.id.card_smartsonix);
                return sonixIcon;
            }

            public TextView getPrice() {
                if (price == null)
                    price = (TextView) itemView.findViewById(R.id.card_price);
                return price;
            }
        }
    }

}
