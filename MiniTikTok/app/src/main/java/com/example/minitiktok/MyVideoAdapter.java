package com.example.minitiktok;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyVideoAdapter extends RecyclerView.Adapter<MyVideoAdapter.VideoViewHolder> {
    private List<VideoMessage> data;
    private IOnItemClickListener mItemClickListener;
    public void setData(List<VideoMessage> messageList){
        data = messageList;
        notifyDataSetChanged();
    }

    public interface IOnItemClickListener {
        void onItemCLick(int position, VideoMessage data);
        void onItemLongCLick(int position, VideoMessage data);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root =LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,parent,false);
        return new VideoViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data==null?0:data.size();
    }

    public void setOnItemClickListener(IOnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder{
        private ImageView iv_video_cover;
        private TextView tv_poster;
        private TextView tv_title;
        private TextView tv_hot;

        private View contentView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            contentView = itemView;
            tv_hot = itemView.findViewById(R.id.tv_hot);
            tv_poster = itemView.findViewById(R.id.tv_poster);
            tv_title = itemView.findViewById(R.id.tv_title);
            iv_video_cover = itemView.findViewById(R.id.video_cover);
        }
        public void bind(VideoMessage videoMessage){
//            iv_video_cover.setImageURI(Uri.parse(videoMessage.getImageUrl()));
            Glide.with(contentView).load(videoMessage.getImageUrl()).into(iv_video_cover);
            tv_title.setText(videoMessage.getUser_name()+"发布的作品");
            tv_poster.setText(videoMessage.getUser_name());
            tv_hot.setText("1111w");

        }
        public void setOnClickListener(View.OnClickListener listener) {
            if (listener != null) {
                contentView.setOnClickListener(listener);
            }
        }

        public void setOnLongClickListener(View.OnLongClickListener listener) {
            if (listener != null) {
                contentView.setOnLongClickListener(listener);
            }
        }
    }

}
