package com.samsung.test.audioproejct.play;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.test.audioproejct.R;
import com.samsung.test.audioproejct.support.FormatUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ItemHolder> {
    List<File> pathList;
    Context context;
    AudioGlide audioGlide;
    private final PlayClickListener listener;

    public PlayListAdapter(Context context, List<File> pathList, AudioGlide audioGlide, PlayClickListener listener) {
        this.pathList = pathList;
        this.context = context;
        this.audioGlide = audioGlide;
        this.listener = listener;
    }

    public void refresh(List<File> pathList) {
        this.pathList = pathList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        File file = pathList.get(position);
        String name = file.getName();
        holder.path.setText(name);

        int duration = audioGlide.getPlayTime(file);
        holder.duration.setText(String.format("%s", FormatUtils.formatSecond(duration)));

        holder.idx.setText(String.format("%d", position));
    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.path)
        TextView path;
        @BindView(R.id.duration)
        TextView duration;
        @BindView(R.id.idx)
        TextView idx;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            File file = pathList.get(getAdapterPosition());
            audioGlide.stop();
            audioGlide.play(file.getPath());
            listener.onClick(getAdapterPosition());
        }
    }
}
