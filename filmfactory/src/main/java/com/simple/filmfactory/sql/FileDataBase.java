package com.simple.filmfactory.sql;

import com.simple.filmfactory.bean.FileBean;
import com.simple.filmfactory.utils.CallBack;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * 对本地音视频存储的数据进行增删改查
 * 参考：https://www.jianshu.com/p/4e080445af20
 **/
public class FileDataBase {

    private static FileDataBase instance;
    private Realm mRealm;

    private final String TAG = "file_database";

    private FileDataBase() {
    }

    public void setRealm(Realm mRealm) {
        this.mRealm = mRealm;
    }

    public static FileDataBase getInstance() {
        if (instance == null) {
            synchronized (FileDataBase.class) {
                if (instance == null) {
                    instance = new FileDataBase();
                }
            }
        }
        return instance;
    }

    /**
     * 添加新的文件到数据库
     **/
    public void add(final FileBean bean, final CallBack callBack) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FileBean info = realm.createObject(FileBean.class);
                info.setAddTime(bean.getAddTime());
                info.setFilePath(bean.getFilePath());
                info.setFileSize(bean.getFileSize());
                info.setFileType(bean.getFileType());
                info.setName(bean.getName());
                info.setNickName(bean.getNickName());
                info.setPlayTime(bean.getPlayTime());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (callBack != null) {
                    callBack.call(TAG, "add_new_file", "onSuccess");
                }

            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (callBack != null) {
                    callBack.call(TAG, "add_new_file", error.getMessage());
                }
            }
        });
    }

    /**
     * 根据路径判断是否添加过某个文件
     **/
    public boolean hasExist(String filePath) {
        RealmResults<FileBean> fileBeans = mRealm.where(FileBean.class).equalTo("filePath", filePath).findAll();
        if (fileBeans == null || fileBeans.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 查询出全部的音频或者视频文件列表,1视频，2音频
     **/
    public List<FileBean> getFile(String fileType) {
        RealmResults<FileBean> fileBeans = mRealm.where(FileBean.class).equalTo("fileType", fileType).findAll();
        return fileBeans;
    }

    /**
     * 修改文件部分属性
     **/
    public void toUpDate(final FileBean bean, final CallBack callBack) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //先查找后得到User对象
                FileBean info = realm.where(FileBean.class).findFirst();
                info.setAddTime(bean.getAddTime());
                info.setFilePath(bean.getFilePath());
                info.setFileSize(bean.getFileSize());
                info.setFileType(bean.getFileType());
                info.setName(bean.getName());
                info.setNickName(bean.getNickName());
                info.setPlayTime(bean.getPlayTime());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (callBack != null) {
                    callBack.call(TAG, "toUpDate", "onSuccess");
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (callBack != null) {
                    callBack.call(TAG, "toUpDate", "onError" + error.getMessage());
                }
            }
        });
    }

    /**
     * 删除数据库指定的文件
     **/
    public void deleteFile(final String nickName, final CallBack callBack) {
        //先查找到数据
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<FileBean> userList = realm.where(FileBean.class).equalTo("nickName", nickName).findAll();
                userList.get(0).deleteFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (callBack != null) {
                    callBack.call(TAG, "deleteFile", "onSuccess");
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (callBack != null) {
                    callBack.call(TAG, "deleteFile", "onError" + error.getMessage());
                }
            }
        });
    }

    /**
     * 删除数据库全部的音频或者视频文件
     **/
    public void deleteAllFile(final String fileType, final CallBack callBack) {
        //先查找到数据
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<FileBean> userList = realm.where(FileBean.class).equalTo("fileType", fileType).findAll();
                userList.deleteAllFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (callBack != null) {
                    callBack.call(TAG, "deleteAllFile", "onSuccess");
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (callBack != null) {
                    callBack.call(TAG, "deleteAllFile", "OnError" + error.getMessage());
                }
            }
        });
    }

    /**
     * 记得使用完后，在onDestroy中关闭Realm
     **/
    protected void onDestroy() {
        mRealm.close();
    }

}
