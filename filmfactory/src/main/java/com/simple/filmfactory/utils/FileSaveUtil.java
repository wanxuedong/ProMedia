package com.simple.filmfactory.utils;

import com.simple.filmfactory.utils.logutils.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static com.simple.filmfactory.utils.FileCataLog.FILE_SAVE;

/***
 * 文件存储序列化对象的工具类
 * **/

public class FileSaveUtil {

    private static String present = FILE_SAVE;

    /**
     * 保存对象
     *
     * @param ser      要保存的序列化对象
     * @param fileName 保存在本地的文件名
     */
    public static boolean saveSerializable(String fileName, Serializable ser) {
        File dirFirstFile = new File(present);
        if (!dirFirstFile.exists()) {
            //创建文件夹
            boolean isCreate = dirFirstFile.mkdir();
            if (!isCreate) {
                LogUtil.e("FileUtil", "Method saveSerializable create FileFolder fail!");
            }
        }
        File file = new File(present, fileName);
        if (file.exists()) {
            boolean isDelete = file.delete();
            if (!isDelete) {
                LogUtil.e("FileUtil", "Method saveSerializable delete File fail!");
            }
        }
        try {
            //创建文件
            boolean isCreate = file.createNewFile();
            if (!isCreate) {
                LogUtil.e("FileUtil", "Method saveSerializable create File fail!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取对象
     *
     * @return fileName 保存在本地的文件名
     */
    public static Serializable readSerializable(String fileName) {
        if (!checkFile(fileName))
            return null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        File file = new File(present, fileName);
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            return (Serializable) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtil.e("FileUtil", e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if (e instanceof InvalidClassException) {
                boolean isDelete = file.delete();
                if (!isDelete) {
                    LogUtil.e("FileUtil", "Method readSerializable isDelete File fail!");
                }
            }
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //检查本地文件是否存在
    private static boolean checkFile(String filePath) {
        File dirFirstFile = new File(present);
        if (!dirFirstFile.exists()) {
            return false;
        }
        File file = new File(present, filePath);
        if (!file.exists()) {
            return false;
        }
        return true;
    }


    //删除文件夹
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            File myFilePath = new File(filePath);
            boolean isDelete = myFilePath.delete(); //删除空文件夹
            if (!isDelete) {
                LogUtil.e("FileUtil", "Method delFolder isDelete FileFolder fail!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 删除指定文件夹下的所有文件
    private static boolean delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] tempList = file.list();
        File temp;
        if (tempList == null || tempList.length == 0) {
            return false;
        }
        for (String str : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + str);
            } else {
                temp = new File(path + File.separator + str);
            }
            if (temp.isFile()) {
                boolean isDelete = temp.delete();
                if (!isDelete) {
                    LogUtil.e("FileUtil", "Method delAllFile isDelete File fail!");
                }
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + str);//先删除文件夹里面的文件
                delFolder(path + "/" + str);//再删除空文件夹
            }
        }
        return true;
    }
}
