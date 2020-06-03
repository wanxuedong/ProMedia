package com.simple.filmfactory;

import android.app.Application;

import com.simple.filmfactory.sql.FileDataBase;
import com.simple.filmfactory.utils.BaseUtil;
import com.simple.filmfactory.utils.CallBack;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class FilmApplication extends Application implements CallBack<Application> {

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {

        //初始化realm数据库
        Realm.init(this);
        //这时候会创建一个叫做 default.realm的Realm文件，一般来说，
        // 这个文件位于/data/data/包名/files/。通过realm.getPath()来获得该Realm的绝对路径
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("myrealm.realm") //文件名
                .schemaVersion(1) //版本号
                .build();
        Realm mRealm = Realm.getInstance(config);
        FileDataBase.getInstance().setRealm(mRealm);

        BaseUtil.setCallBack(this);

    }

    @Override
    public Application call(String... data) {
        return this;
    }
}
