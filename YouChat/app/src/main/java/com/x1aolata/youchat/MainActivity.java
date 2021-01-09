package com.x1aolata.youchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.x1aolata.youchat.adapter.FriendListAdapter;
import com.x1aolata.youchat.bean.FriendInfo;
import com.x1aolata.youchat.bean.MessageStatus;
import com.x1aolata.youchat.client.ClientListener;
import com.x1aolata.youchat.util.Constant;
import com.x1aolata.youchat.util.Utils;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 用于数据库转移文件操作
    public static AppCompatActivity appCompatActivity;

    public static Handler handler_main;

    private RecyclerView recyclerView;

    private TextView user_name;
    private TextView current_ip;

    private List<FriendInfo> friendInfos;
    private FriendListAdapter friendListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        init();
        initHandler();
        // 设置昵称和自己的IP
        user_name.setText(Constant.name);
        current_ip.setText(Utils.getIPAddress());

        // 设置当前handler
        ClientListener.setHandler(handler_main);

        friendInfos.add(new FriendInfo(R.drawable.aaa, "群聊", "0.0.0.0", "刚刚"));
//        for (int i = 0; i < 20; i++) {
//            friendListAdapter.addFriends(new FriendInfo(Utils.getRandomImage(), "心如止水", "192.156.5.23", "刚刚"));
//            friendListAdapter.addFriends(new FriendInfo(Utils.getRandomImage(), "海阔天空", "192.232.123.23", "刚刚"));
//            friendListAdapter.addFriends(new FriendInfo(Utils.getRandomImage(), "探索未来", "192.168.5.23", "刚刚"));
//        }


        friendListAdapter.setOnItemClickListener(new FriendListAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                // Toast.makeText(getApplication(), friendListAdapter.getFriendInfoOfPosition(position).toString(), Toast.LENGTH_SHORT).show();
                Constant.friendIP = friendListAdapter.getFriendInfoOfPosition(position).getIp();
                Constant.friendName = friendListAdapter.getFriendInfoOfPosition(position).getName();
                startActivity(new Intent(MainActivity.this, ChatActivity.class));
            }
        });


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 设置当前handler
        ClientListener.setHandler(handler_main);
    }

    public void init() {

        appCompatActivity = this;

        recyclerView = findViewById(R.id.friends_list);
        user_name = findViewById(R.id.main_text_user_name);
        current_ip = findViewById(R.id.main_text_current_ip);

        friendInfos = new ArrayList<>();
        friendListAdapter = new FriendListAdapter(friendInfos);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(friendListAdapter);


    }

    public void initHandler() {
        handler_main = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull android.os.Message msg) {
                switch (msg.what) {
                    case MessageStatus.receiveHeart:
                        JSONObject jsonObject = (JSONObject) msg.obj;
                        String name = jsonObject.get("name").toString();
                        String friendIP = jsonObject.get("source").toString();
                        // 如果不在好友列表中，加入好友列表
                        if (friendListAdapter.notInFriendslist(friendIP)) {
                            friendListAdapter.addFriends(new FriendInfo(Utils.getRandomImage(), name, friendIP, "刚刚"));
                            friendListAdapter.notifyDataSetChanged();
                        }
                        break;
                }
                return false;
            }
        });
    }

}