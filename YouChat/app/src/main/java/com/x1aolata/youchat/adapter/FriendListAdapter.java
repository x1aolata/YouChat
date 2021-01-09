package com.x1aolata.youchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.x1aolata.youchat.bean.FriendInfo;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import com.x1aolata.youchat.R;

/**
 * @author x1aolata
 * @date 2020/11/24 11:04
 * @script ...
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {

    private List<FriendInfo> friendInfos;

    public FriendListAdapter(List<FriendInfo> friendInfos) {
        this.friendInfos = friendInfos;
    }

    public void addFriends(FriendInfo friendInfo) {
        friendInfos.add(friendInfo);
    }

    public FriendInfo getFriendInfoOfPosition(int position) {
        return friendInfos.get(position);
    }

    public boolean notInFriendslist(String friendIP) {
        for (FriendInfo friendInfo : friendInfos) {
            if (friendInfo.getIp().equals(friendIP))
                return false;
        }
        return true;
    }

    /**
     * 点击事件
     */
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClicked(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.onItemClickListener = clickListener;
    }


    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

        return new FriendViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendInfo friendInfo = friendInfos.get(position);

        holder.item_friend_image.setImageResource(friendInfo.getImage());
        holder.item_friend_name.setText(friendInfo.getName());
        holder.item_friend_ip.setText(friendInfo.getIp());
        holder.item_friend_time.setText(friendInfo.getTime());
    }

    @Override
    public int getItemCount() {
        return friendInfos.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        CircleImageView item_friend_image;
        TextView item_friend_name;
        TextView item_friend_ip;
        TextView item_friend_time;

        public FriendViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);

            this.item_friend_image = itemView.findViewById(R.id.item_friend_image);
            this.item_friend_name = itemView.findViewById(R.id.item_friend_name);
            this.item_friend_ip = itemView.findViewById(R.id.item_friend_ip);
            this.item_friend_time = itemView.findViewById(R.id.item_friend_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        //确保position值有效
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClicked(v, position);
                        }
                    }
                }
            });
        }


    }



}
