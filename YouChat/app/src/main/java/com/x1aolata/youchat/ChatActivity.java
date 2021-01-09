package com.x1aolata.youchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;

import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.x1aolata.youchat.adapter.ChatAdapter;
import com.x1aolata.youchat.bean.MessageStatus;
import com.x1aolata.youchat.bean.MsgType;
import com.x1aolata.youchat.client.Client;
import com.x1aolata.youchat.client.ClientListener;
import com.x1aolata.youchat.util.Constant;
import com.x1aolata.youchat.util.LogUtil;
import com.x1aolata.youchat.bean.Message;
import com.x1aolata.youchat.bean.AudioMsgBody;
import com.x1aolata.youchat.bean.FileMsgBody;
import com.x1aolata.youchat.bean.ImageMsgBody;
import com.x1aolata.youchat.bean.MsgSendStatus;
import com.x1aolata.youchat.bean.TextMsgBody;
import com.x1aolata.youchat.bean.VideoMsgBody;
import com.x1aolata.youchat.util.ChatUiHelper;
import com.x1aolata.youchat.util.FileUtils;
import com.x1aolata.youchat.util.Utils;
import com.x1aolata.youchat.widget.MediaManager;
import com.x1aolata.youchat.widget.RecordButton;
import com.x1aolata.youchat.widget.StateButton;
import com.wildma.pictureselector.*;
import com.x1aolata.youchat.widget.wave.Util;


import org.json.simple.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.x1aolata.youchat.util.Utils.base642Image;

public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    @BindView(R.id.llContent)
    LinearLayout mLlContent;
    @BindView(R.id.rv_chat_list)
    RecyclerView mRvChat;
    @BindView(R.id.et_content)
    EditText mEtContent;
    @BindView(R.id.bottom_layout)
    RelativeLayout mRlBottomLayout;//表情,添加底部布局
    @BindView(R.id.ivAdd)
    ImageView mIvAdd;
    @BindView(R.id.ivEmo)
    ImageView mIvEmo;
    @BindView(R.id.btn_send)
    StateButton mBtnSend;//发送按钮
    @BindView(R.id.ivAudio)
    ImageView mIvAudio;//录音图片
    @BindView(R.id.btnAudio)
    RecordButton mBtnAudio;//录音按钮
    @BindView(R.id.rlEmotion)
    LinearLayout mLlEmotion;//表情布局
    @BindView(R.id.llAdd)
    LinearLayout mLlAdd;//添加布局
    @BindView(R.id.swipe_chat)
    SwipeRefreshLayout mSwipeRefresh;//下拉刷新

    @BindView(R.id.common_toolbar_title)
    TextView toolbar_title;


    private ChatAdapter mAdapter;
    public static Handler handler_chat;

    public static final String mSenderId = "right";
    public static final String mTargetId = "left";
    public static final int REQUEST_CODE_IMAGE = 0000;
    public static final int REQUEST_CODE_VEDIO = 1111;
    public static final int REQUEST_CODE_FILE = 2222;


    private String friendIP;
    ImageView title_back;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initContent();
//        初始化handler
        initHandler();
        resetInfo();
    }

    public void resetInfo() {
        // 设置当前名称和朋友ip
        setFriendIP(Constant.friendIP);
        setToolbarTitle(Constant.friendName);
        // 设置朋友IP destinationIP
        Client.setFriendIP(friendIP);
        // 设置当前handler
        ClientListener.setHandler(handler_chat);
    }

    @Override
    protected void onStart() {
        super.onStart();
        resetInfo();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        resetInfo();
    }

    public boolean isMe(Object obj) {
        JSONObject jsonObject = (JSONObject) obj;
        if (friendIP.equals("0.0.0.0") && ((String) jsonObject.get("destination")).equals("0.0.0.0"))
            return true;
        if (((String) jsonObject.get("source")).equals(friendIP)) {
            if (((String) jsonObject.get("destination")).equals(Utils.getIPAddress())) {
                return true;
            }
        }
        return false;
    }

    public void initHandler() {
        handler_chat = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull android.os.Message msg) {

                switch (msg.what) {
                    case MessageStatus.connection_successful:
                        Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_SHORT).show();
                        break;
                    case MessageStatus.connection_fail:
                        Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case MessageStatus.receiveText:
                        if (isMe(msg.obj)) {
                            String s = (String) ((JSONObject) msg.obj).get("data");
                            receiveText(s);
                        }
                        break;
                    case MessageStatus.receiveImage:
                        if (isMe(msg.obj)) {
                            Bitmap bitmap = base642Image((String) ((JSONObject) msg.obj).get("data"));
                            receiveImage(bitmap);
                        }
                        break;
                    case MessageStatus.receiveAudio:
                        if (isMe(msg.obj)) {
                            String base64Code = (String) ((JSONObject) msg.obj).get("data");
                            String audioPath = Utils.base642Audio(base64Code);
                            int time = (int) ((JSONObject) msg.obj).get("audiotime");
                            receiveAudioMessage(audioPath, time);
                        }
                        break;
                }
                return false;
            }
        });

    }

    /**
     * start
     */
    public void setFriendIP(String friendIP) {
        this.friendIP = friendIP;
    }

    /**
     * 设置Toolbar_title
     */
    public void setToolbarTitle(String title) {
        toolbar_title.setText(title);
    }


    /**
     * 收到文字消息
     * 显示在屏幕上
     *
     * @param message
     */
    public void receiveText(String message) {
        List<Message> mReceiveMsgList = new ArrayList<Message>();
        //构建文本消息
        Message mMessgaeText = getBaseReceiveMessage(MsgType.TEXT);
        TextMsgBody mTextMsgBody = new TextMsgBody();
        mTextMsgBody.setMessage(message);
        mMessgaeText.setBody(mTextMsgBody);
        mReceiveMsgList.add(mMessgaeText);
        mAdapter.addData(mReceiveMsgList);
    }

    /**
     * 收到图片消息
     * 显示在屏幕上
     *
     * @param bitmap
     */
    public void receiveImage(Bitmap bitmap) {

        List<Message> mReceiveMsgList = new ArrayList<Message>();
        String path = saveMyBitmap(Utils.getRandomString(12), bitmap);
        //构建图片消息
        Message mMessgaeImage = getBaseReceiveMessage(MsgType.IMAGE);
        ImageMsgBody mImageMsgBody = new ImageMsgBody();
        //mImageMsgBody.setThumbUrl("https://c-ssl.duitang.com/uploads/item/201208/30/20120830173930_PBfJE.thumb.700_0.jpeg");
        mImageMsgBody.setThumbUrl(path);
        mMessgaeImage.setBody(mImageMsgBody);
        mReceiveMsgList.add(mMessgaeImage);
        mAdapter.addData(mReceiveMsgList);
    }

    public void receiveAudioMessage(String audioPath, int time) {

        List<Message> mReceiveMsgList = new ArrayList<Message>();
        //构建语音消息
        Message mMessgaeAudio = getBaseReceiveMessage(MsgType.AUDIO);
        AudioMsgBody audioMsgBody = new AudioMsgBody();
        audioMsgBody.setLocalPath(audioPath);
        audioMsgBody.setDuration(time);
        mMessgaeAudio.setBody(audioMsgBody);
        mReceiveMsgList.add(mMessgaeAudio);

        mAdapter.addData(mReceiveMsgList);
    }


    /**
     * 转存bitmap为png
     * 返回文件地址
     *
     * @param bitName
     * @param mBitmap
     * @return
     */
    public String saveMyBitmap(String bitName, Bitmap mBitmap) {
        File f = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.x1aolata.youchat/files/Pictures/" + bitName + ".png");

        try {
            f.createNewFile();
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f.getPath();

    }


    /**
     * end
     */

    private ImageView ivAudio;

    protected void initContent() {
        ButterKnife.bind(this);
        mAdapter = new ChatAdapter(this, new ArrayList<Message>());
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mRvChat.setLayoutManager(mLinearLayout);
        mRvChat.setAdapter(mAdapter);
        mSwipeRefresh.setOnRefreshListener(this);
        initChatUi();
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                final boolean isSend = mAdapter.getItem(position).getSenderId().equals(ChatActivity.mSenderId);
                if (ivAudio != null) {
                    if (isSend) {
                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                    } else {
                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                    }
                    ivAudio = null;
                    MediaManager.reset();
                } else {
                    ivAudio = view.findViewById(R.id.ivAudio);
                    MediaManager.reset();
                    if (isSend) {
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_right_list);
                    } else {
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_left_list);
                    }
                    AnimationDrawable drawable = (AnimationDrawable) ivAudio.getBackground();
                    drawable.start();
                    MediaManager.playSound(ChatActivity.this, ((AudioMsgBody) mAdapter.getData().get(position).getBody()).getLocalPath(), new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (isSend) {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                            } else {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                            }

                            MediaManager.release();
                        }
                    });
                }
            }
        });

        title_back = findViewById(R.id.title_back);
        title_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


    /**
     * 下拉列表刷新
     */
    @Override
    public void onRefresh() {
        //下拉刷新模拟获取历史消息
        List<Message> mReceiveMsgList = new ArrayList<Message>();
        Toast.makeText(this, "没有历史消息", Toast.LENGTH_SHORT).show();

//        //构建文本消息
//        Message mMessgaeText = getBaseReceiveMessage(MsgType.TEXT);
//        TextMsgBody mTextMsgBody = new TextMsgBody();
//        mTextMsgBody.setMessage("小邋遢不邋遢");
//        mMessgaeText.setBody(mTextMsgBody);
//        mReceiveMsgList.add(mMessgaeText);
//
//        //构建图片消息
//        Message mMessgaeImage = getBaseReceiveMessage(MsgType.IMAGE);
//        ImageMsgBody mImageMsgBody = new ImageMsgBody();
//        //mImageMsgBody.setThumbUrl("https://c-ssl.duitang.com/uploads/item/201208/30/20120830173930_PBfJE.thumb.700_0.jpeg");
//        mImageMsgBody.setThumbUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1605898989485&di=d53e2a90de7ea812528d53363d4e976b&imgtype=0&src=http%3A%2F%2Fi0.hdslb.com%2Fbfs%2Farticle%2F34ca04b442580fe9ace8e9b8febd93d4ccecdb26.jpg");
//        mMessgaeImage.setBody(mImageMsgBody);
//        mReceiveMsgList.add(mMessgaeImage);


        //构建文件消息
//        Message mMessgaeFile = getBaseReceiveMessage(MsgType.FILE);
//        FileMsgBody mFileMsgBody = new FileMsgBody();
//        mFileMsgBody.setDisplayName("收到的文件");
//        mFileMsgBody.setSize(12);
//        mMessgaeFile.setBody(mFileMsgBody);
//        mReceiveMsgList.add(mMessgaeFile);

        mAdapter.addData(0, mReceiveMsgList);
        mSwipeRefresh.setRefreshing(false);
    }


    private void initChatUi() {
        //mBtnAudio
        final ChatUiHelper mUiHelper = ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mLlContent)
                .bindttToSendButton(mBtnSend)
                .bindEditText(mEtContent)
                .bindBottomLayout(mRlBottomLayout)
                .bindEmojiLayout(mLlEmotion)
                .bindAddLayout(mLlAdd)
                .bindToAddButton(mIvAdd)
                .bindToEmojiButton(mIvEmo)
                .bindAudioBtn(mBtnAudio)
                .bindAudioIv(mIvAudio)
                .bindEmojiData();
        //底部布局弹出,聊天列表上滑
        mRvChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mRvChat.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.getItemCount() > 0) {
                                mRvChat.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    });
                }
            }
        });
        //点击空白区域关闭键盘
        mRvChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUiHelper.hideBottomLayout(false);
                mUiHelper.hideSoftInput();
                mEtContent.clearFocus();
                mIvEmo.setImageResource(R.mipmap.ic_emoji);
                return false;
            }
        });
        //
        ((RecordButton) mBtnAudio).setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                LogUtil.d("录音结束回调");
                LogUtil.d("获取到的文件路径:录音" + audioPath);
                //录音/data/user/0/com.x1aolata.youchat/files/voice_1605974952075.mp3
                File file = new File(audioPath);
                if (file.exists()) {

                    // Waiting to send
                    String base64Code = Utils.audio2Base64(audioPath);
                    String ppath = Utils.base642Audio(base64Code);
                    sendAudioMessage(ppath, time);
                    Client.sendAudio(base64Code, time);
                    Log.d("YouChatinfo 获取到的文件路径:转存", ppath);
                    Log.d("YouChatinfo 获取到的文件路径:录音", audioPath);
                    //receiveAudioMessage(ppath, time);
                }
            }
        });

    }

    @OnClick({R.id.btn_send, R.id.rlPhoto, R.id.rlVideo, R.id.rlLocation, R.id.rlFile})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                // 发送文字消息
                String s = mEtContent.getText().toString();
                sendTextMsg(s);
                Log.d("YouChatinfo", "发送文字消息：" + s);
                Client.sendText(s);
                mEtContent.setText("");

                // Waiting to send
                break;
            case R.id.rlPhoto:
                //  PictureFileUtil.openGalleryPic(ChatActivity.this, REQUEST_CODE_IMAGE);
                // Toast.makeText(this, "敬请期待", Toast.LENGTH_SHORT).show();
                PictureSelector
                        .create(ChatActivity.this, PictureSelector.SELECT_REQUEST_CODE)
                        .selectPicture(true, 300, 300, 1, 1);


                break;
            case R.id.rlVideo:
                // PictureFileUtil.openGalleryAudio(ChatActivity.this, REQUEST_CODE_VEDIO);
                Toast.makeText(this, "敬请期待", Toast.LENGTH_SHORT).show();

                break;
            case R.id.rlFile:
                // PictureFileUtil.openFile(ChatActivity.this, REQUEST_CODE_FILE);
                Toast.makeText(this, "敬请期待", Toast.LENGTH_SHORT).show();

                break;
            case R.id.rlLocation:
                Toast.makeText(this, "敬请期待", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    /**
     * 回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    LogUtil.d("获取到的文件路径:" + filePath);
                    sendFileMessage(mSenderId, mTargetId, filePath);
                    break;
//                case REQUEST_CODE_IMAGE:
//                    // 图片选择结果回调
//                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
//                    for (LocalMedia media : selectListPic) {
//                        LogUtil.d("获取图片路径成功:" + media.getPath());
//                        sendImageMessage(media);
//                    }
//                    break;
//                case REQUEST_CODE_VEDIO:
//                    // 视频选择结果回调
//                    List<LocalMedia> selectListVideo = PictureSelector.obtainMultipleResult(data);
//                    for (LocalMedia media : selectListVideo) {
//                        LogUtil.d("获取视频路径成功:" + media.getPath());
//                        sendVedioMessage(media);
//                    }
//                    break;
            }

            /**
             * 回调
             */
            if (requestCode == PictureSelector.SELECT_REQUEST_CODE) {
                if (data != null) {
                    PictureBean pictureBean = data.getParcelableExtra(PictureSelector.PICTURE_RESULT);
                    LogUtil.d("获取到的文件路径:" + pictureBean.getPath());
                    if (pictureBean.isCut()) {
                        sendImageMessage(pictureBean.getPath());

                    } else {
                        sendImageMessage(pictureBean.getPath());
                    }
                    // 获取到bitmap 用于传输
                    // 发送图片
                    Bitmap bitmap = BitmapFactory.decodeFile(pictureBean.getPath());
                    //receiveImage(bitmap);
                    Client.sendImage(bitmap);
                    // Waiting to send
                }
            }
        }
    }


    //文本消息
    private void sendTextMsg(String hello) {
        final Message mMessgae = getBaseSendMessage(MsgType.TEXT);
        TextMsgBody mTextMsgBody = new TextMsgBody();
        mTextMsgBody.setMessage(hello);
        mMessgae.setBody(mTextMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟0.1秒后发送成功
        updateMsg(mMessgae);
    }


    //图片消息
    private void sendImageMessage(final LocalMedia media) {
        final Message mMessgae = getBaseSendMessage(MsgType.IMAGE);
        ImageMsgBody mImageMsgBody = new ImageMsgBody();
        mImageMsgBody.setThumbUrl(media.getCompressPath());
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }

    /**
     * 发送图片消息
     *
     * @param path
     */
    private void sendImageMessage(final String path) {
        final Message mMessgae = getBaseSendMessage(MsgType.IMAGE);
        ImageMsgBody mImageMsgBody = new ImageMsgBody();
        mImageMsgBody.setThumbUrl(path);
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }


    //视频消息
    private void sendVedioMessage(final LocalMedia media) {
        final Message mMessgae = getBaseSendMessage(MsgType.VIDEO);
        //生成缩略图路径
        String vedioPath = media.getPath();
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(vedioPath);
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();
        String imgname = System.currentTimeMillis() + ".jpg";
        String urlpath = Environment.getExternalStorageDirectory() + "/" + imgname;
        File f = new File(urlpath);
        try {
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            LogUtil.d("视频缩略图路径获取失败：" + e.toString());
            e.printStackTrace();
        }
        VideoMsgBody mImageMsgBody = new VideoMsgBody();
        mImageMsgBody.setExtra(urlpath);
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);

    }

    //文件消息
    private void sendFileMessage(String from, String to, final String path) {
        final Message mMessgae = getBaseSendMessage(MsgType.FILE);
        FileMsgBody mFileMsgBody = new FileMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDisplayName(FileUtils.getFileName(path));
        mFileMsgBody.setSize(FileUtils.getFileLength(path));
        mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟0.1秒后发送成功
        updateMsg(mMessgae);

    }

    //语音消息
    private void sendAudioMessage(final String path, int time) {
        final Message mMessgae = getBaseSendMessage(MsgType.AUDIO);
        AudioMsgBody mFileMsgBody = new AudioMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDuration(time);
        mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟0.1秒后发送成功
        updateMsg(mMessgae);
    }


    private Message getBaseSendMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private Message getBaseReceiveMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(mTargetId);
        mMessgae.setTargetId(mSenderId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private void updateMsg(final Message mMessgae) {
        mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
        //模拟0.1秒后发送成功
        new Handler().postDelayed(new Runnable() {
            public void run() {
                int position = 0;
                mMessgae.setSentStatus(MsgSendStatus.SENT);
                //更新单个子条目
                for (int i = 0; i < mAdapter.getData().size(); i++) {
                    Message mAdapterMessage = mAdapter.getData().get(i);
                    if (mMessgae.getUuid().equals(mAdapterMessage.getUuid())) {
                        position = i;
                    }
                }
                mAdapter.notifyItemChanged(position);
            }
        }, 100);
    }
}