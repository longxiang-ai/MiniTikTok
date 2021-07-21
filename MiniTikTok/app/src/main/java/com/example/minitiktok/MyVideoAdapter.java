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

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

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


    @Override
    public MyVideoAdapter.VideoViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        return new VideoViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, final int position) {
        holder.onBind(position, data.get(position));
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemCLick(position, data.get(position));
                }
            }
        });
        holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemLongCLick(position, data.get(position));
                }
                return false;
            }

        });
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
        public void onBind(int position ,VideoMessage videoMessage){
            // 显示图片
            Glide.with(contentView)
                    .load(videoMessage.getImageUrl())
                    .placeholder(R.drawable.btn_post_image) // 兜底占位图
                    .error(R.drawable.btn_post_image)       // 加载错误图
                    .fallback(R.drawable.btn_post_image)    // loading图片
                    .transition(withCrossFade())
                    .into(iv_video_cover)
            ;
            tv_title.setText(videoMessage.getUser_name()+"发布的作品");
            tv_poster.setText(videoMessage.getUser_name());
            tv_hot.setText(videoMessage.getCreatedAt().toString());

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
