package com.x1aolata.youchat.emoji;

import android.app.Application;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.x1aolata.youchat.MainActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * describe: 表情数据库操作
 * author: Went_Gone
 * create on: 2016/10/27
 */


public class EmojiDao {
    private static final String TAG = "EmojiDao";
    private String path;
    private static EmojiDao dao;

    public static EmojiDao getInstance() {
        if (dao == null) {
            synchronized (EmojiDao.class) {
                if (dao == null) {
                    dao = new EmojiDao();
                }
            }
        }
        return dao;
    }

    private EmojiDao() {
        try {
            path = CopySqliteFileFromRawToDatabases("emoji.db");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<EmojiBean> getEmojiBean() {
        List<EmojiBean> emojiBeanList = new ArrayList<EmojiBean>();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.query("emoji", new String[]{"unicodeInt", "_id"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            EmojiBean bean = new EmojiBean();
            int unicodeInt = cursor.getInt(0);
            int id = cursor.getInt(1);
            bean.setUnicodeInt(unicodeInt);
            bean.setId(id);
            emojiBeanList.add(bean);
        }
        return emojiBeanList;
    }


    /**
     * 将assets目录下的文件拷贝到database中
     *
     * @return 存储数据库的地址
     */
    public static String CopySqliteFileFromRawToDatabases(String SqliteFileName) throws IOException {
        // 第一次运行应用程序时，加载数据库到data/data/当前包的名称/database/<db_name>
        //复制的话这里需要换成自己项目的包名
        File dir = new File("data/data/" + "com.x1aolata.youchat" + "/databases");

        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        File file = new File(dir, SqliteFileName);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        copyDB();



        //通过IO流的方式，将assets目录下的数据库文件，写入到SD卡中。
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//                inputStream = MyApplication.mApplication.getClass().getClassLoader().getResourceAsStream("assets/" + SqliteFileName);
//                outputStream = new FileOutputStream(file);
//                byte[] buffer = new byte[1024];
//                int len;
//                while ((len = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, len);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (outputStream != null) {
//                    outputStream.flush();
//                    outputStream.close();
//                }
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            }
//        }
        return file.getPath();
    }


    public static void copyDB() {
        // 获取输出流,文件存储目录:data/data/包名/files目录下，文件名相同
        File file = new File(MainActivity.appCompatActivity.getFilesDir().toString());

        file = new File(file.getParent() + "/databases/emoji.db");
        File parent = new File(file.getParent());
        if (!parent.exists()) {//创建父类目录文件夹。
            try {
                parent.mkdirs();//监测父类文件不存在则创建
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 当文件不存在的时候：才去拷贝，已经存在的不再去拷贝了。
        if (!file.exists()) {
            AssetManager assetManager = MainActivity.appCompatActivity.getAssets();
            try {
                // 获取输入流
                InputStream is = assetManager.open("emoji.db");
                FileOutputStream fos = new FileOutputStream(file);
                // 开始读和写
                byte[] bys = new byte[1024];
                int len;
                while ((len = is.read(bys)) != -1) {
                    fos.write(bys, 0, len);
                }
                is.close();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
