package com.x1aolata.youchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.RegexUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.x1aolata.youchat.bean.MessageStatus;
import com.x1aolata.youchat.client.Client;
import com.x1aolata.youchat.util.Constant;
import com.x1aolata.youchat.util.Utils;
import com.x1aolata.youchat.widget.SetPermissionDialog;

import org.json.simple.JSONObject;

import butterknife.BindView;
import io.reactivex.functions.Consumer;

import static com.x1aolata.youchat.util.Utils.base642Image;

public class LoginActivity extends AppCompatActivity {

    private Button login_button;
    private EditText login_user_name;
    private EditText login_server_ip;

    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        init();
        getPermission();

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = login_user_name.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(getApplication(), "请正确填写昵称!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Constant.name = name;
                Client.setConfig(handler, Constant.serverIP, 6666, name);
                Client.getInstance().getConnection();
            }
        });
    }

    public void getPermission() {
        RxPermissions rxPermission = new RxPermissions(this);
        rxPermission.request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,//存储权限
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        ).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {
//                    startActivity(new Intent(MainActivity.this, ChatActivity.class));
//                    finish();
                } else {
                    SetPermissionDialog mSetPermissionDialog = new SetPermissionDialog(LoginActivity.this);
                    mSetPermissionDialog.show();
                    mSetPermissionDialog.setConfirmCancelListener(new SetPermissionDialog.OnConfirmCancelClickListener() {
                        @Override
                        public void onLeftClick() {
                            finish();
                        }

                        @Override
                        public void onRightClick() {
                            finish();
                        }
                    });
                }
            }
        });
    }

    public void init() {
        login_button = findViewById(R.id.login_button);
        login_user_name = findViewById(R.id.login_user_name);
        login_server_ip = findViewById(R.id.login_server_ip);
        //服务器的IP地址
        Constant.serverIP = "39.107.25.109";
        login_server_ip.setText(Utils.getIPAddress());

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull android.os.Message msg) {
                switch (msg.what) {
                    case MessageStatus.connection_successful:
                        Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                        break;
                    case MessageStatus.connection_fail:
                        Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }
}