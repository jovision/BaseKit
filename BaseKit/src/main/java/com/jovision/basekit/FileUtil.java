package com.jovision.basekit;

import static android.os.Environment.DIRECTORY_DCIM;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * 文件操作工具类
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileUtil {

    private static final String TAG = "FileUtil";


    /**
     * 根据URI获取文件真实路径（兼容多张机型）
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getFilePathByUri(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {

            int sdkVersion = Build.VERSION.SDK_INT;
            if (sdkVersion >= 19) { // api >= 19
                return getRealPathFromUriAboveApi19(context, uri);
            } else { // api < 19
                return getRealPathFromUriBelowAPI19(context, uri);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }


    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String type = documentId.split(":")[0];
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};

                //
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            } else if (isExternalStorageDocument(uri)) {
                // ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }

        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }


    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * 递归创建文件目录
     *
     * @param filePath 要创建的目录路径
     */
    @SuppressWarnings("ConstantConditions")
    public static void createDirectory(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;

        File file = new File(filePath);
        if (file == null || file.exists()) {
            return;
        }
        File parentFile = file.getParentFile();

        if (parentFile != null && parentFile.exists()) {
            if (!parentFile.isDirectory()) {
                parentFile.delete();
                boolean res = parentFile.mkdir();
                if (!res) {
                    parentFile.delete();
                }
            }

            boolean res = file.mkdir();
            if (!res) {
                file.delete();
            }

        } else {
            createDirectory(file.getParentFile().getAbsolutePath());
            boolean res = file.mkdir();
            if (!res) {
                file.delete();
            }
        }
    }

    /**
     * 递归创建文件目录
     *
     * @param file 要创建的文件
     */
    public static void createDirectory(File file) {
        if (file == null) return;
        createDirectory(file.getAbsolutePath());
    }

    /**
     * 递归删除文件或者文件夹（清空文件夹包括根目录）
     * <p>
     * 删除文件夹和删除文件以后可以拆分成两个方法
     *
     * @param file 要删除的根目录
     */
    public static void deleteDirOrFile(File file) {
        try{
            if (file == null || !file.exists()) return;
            if (file.isFile()) {
                file.delete();
                return;
            }
            LogUtil.v(TAG,"deleteDirOrFile-file="+file.getAbsolutePath());

            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return;
                }
                for (File f : childFile) {
                    deleteDirOrFile(f);
                }
                file.delete();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 删除文件或者文件夹（清空文件夹包括根目录）
     *
     * @param filePath 要删除的根目录路径
     */
    public static void deleteDirOrFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;
        deleteDirOrFile(new File(filePath));
    }

    /**
     * 删除文件
     *
     * @param file 要删除的文件
     * @return 是否删除成功
     */
    public static boolean deleteFile(File file) {
        if (file == null) return false;
        if (!file.exists()) return false;
        if (!file.isFile()) return false;
        return file.delete();
    }

    /**
     * 删除文件
     *
     * @param filePath 要删除的文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) return false;
        return deleteFile(new File(filePath));
    }

    /**
     * 删除目录里面的文件和子目录，保留原目录
     *
     * @param file 要删除的根目录
     */
    public static void deleteFilesInDir(File file) {
        if (file == null || !file.exists()) return;
        if (!file.isDirectory()) return;
        File[] childFile = file.listFiles();
        if (childFile == null || childFile.length == 0) return;
        for (File f : childFile) {
            deleteDirOrFile(f);
        }
    }

    /**
     * 删除目录里面的文件和子目录，保留原目录
     * @param filePath 要删除的根目录路径
     */
    public static void deleteFilesInDir(String filePath){
        if (TextUtils.isEmpty(filePath)) return;
        deleteFilesInDir(new File(filePath));
    }

    /**
     * 删除目录下指定文件名的文件
     * 如果目录下没有文件则删除目录
     *
     * @param file        目录/文件地址
     * @param targetFiles 要删除的文件数组
     */
    public static void deleteTargetFiles(File file, String[] targetFiles) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length <= 0) {
                file.delete();
                return;
            }
            for (File f : files) {
                deleteTargetFiles(f, targetFiles);
            }
            file.delete(); // 空文件夹删除，有文件时会删除失败
        } else {
            if (ArrayUtil.contains(targetFiles, file.getName())) {
                file.delete();
            }
        }
    }

    /**
     * 删除目录下除了指定文件以外的其它文件
     * 如果目录下没有文件则删除目录
     *
     * @param file         目录/文件地址
     * @param excludeFiles 不需要删除的文件地址
     */
    public static void deleteFilesExceptTargetFiles(File file, String[] excludeFiles) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length <= 0) {
                file.delete();
                return;
            }
            for (File f : files) {
                deleteFilesExceptTargetFiles(f, excludeFiles);
            }
            file.delete(); // 空文件夹删除，有文件时会删除失败
        } else {
            if (!ArrayUtil.contains(excludeFiles, file.getName())) {
                file.delete();
            }
        }
    }

    /**
     * 重命名文件，删除旧文件
     */
    public static boolean renameFile(String oldFileName, String newFileName) {

        boolean renameRes = false;
        try {
            File oldFile = new File(oldFileName); //要重命名的文件或文件夹
            File newFile = new File(newFileName);  //重命名为
            LogUtil.v(TAG, "reNameFile:文件存在状态：oldFile.exists=" + oldFile.exists() + ";newFile.exists=" + newFile.exists());

            if (oldFile.exists() && newFile.exists()) {
                deleteDirOrFile(newFile);
            }
            if (oldFile.exists()) {
                renameRes = oldFile.renameTo(newFile);  //执行重命名
                LogUtil.v(TAG,"reNameFile:rename Res =" + renameRes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return renameRes;
    }

    /**
     * 重命名文件，不删除旧文件
     */
    public static boolean renameFileNotDeleteOldFile(String oldFileName, String newFileName) {

        boolean renameRes = false;
        try {
            File oldFile = new File(oldFileName); //要重命名的文件或文件夹
            File newFile = new File(newFileName);  //重命名为
            LogUtil.v(TAG, "reNameFileNotDeleteOldFile:文件存在状态：oldFile.exists=" + oldFile.exists() + ";newFile.exists=" + newFile.exists());

            if (oldFile.exists()) {
                renameRes = oldFile.renameTo(newFile);  //执行重命名
                LogUtil.v(TAG, "reNameFileNotDeleteOldFile:rename Res =" + renameRes);
            } else {
                LogUtil.v(TAG, "reNameFileNotDeleteOldFile:oleFile=" + oldFile + ";old file not exist do nothing");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return renameRes;
    }


    /**
     * 获取文件夹下的所有文件
     *
     * @param filePath 根目录
     */
    @SuppressWarnings("ConstantConditions")
    public static File[] getListFilesInDir(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        File file = new File(filePath);
        if (file != null && file.exists() && file.isDirectory()) {
            return file.listFiles();
        }
        return null;
    }


    /**
     * 获取文件夹下的文件
     *
     * @param filePath 根目录
     */
    public static File[] getFilesByFolder(String filePath) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            return file.listFiles();
        }
        return null;
    }


    /**
     * 重命名文件
     *
     * @param oldFileName
     * @param newFileName
     * @return
     */
    public static boolean reNameFile(String oldFileName, String newFileName) {

        boolean renameRes = false;
        try {
            File oldFile = new File(oldFileName); //要重命名的文件或文件夹
            File newFile = new File(newFileName);  //重命名为
            LogUtil.v(TAG, "reNameFile:文件存在状态：oldFile=" + oldFile.exists() + ";newFile=" + newFile.exists());

            if (oldFile.exists() && newFile.exists()) {
                deleteFile(newFile);
                LogUtil.v(TAG, "reNameFile:新老文件都存在，删除新文件夹");
            }
            if (oldFile.exists()) {
                LogUtil.v(TAG, "reNameFile:oleFile=" + oldFile + ";renameTo newFile=" + newFile);
                renameRes = oldFile.renameTo(newFile);  //执行重命名
                LogUtil.v(TAG, "reNameFile:rename Res =" + renameRes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return renameRes;
    }


    /**
     * 获取文件夹下面的最后修改的文件地址
     *
     * @param folderPath 文件夹路径
     * @return
     */
    public static String getLastModifiedImagePath(String folderPath) {
        String imageFilePath = "";
        // 文件夹目录不存在,直接返回
        File folder = new File(folderPath);
        if (!folder.exists()) {
            return imageFilePath;
        }

        // 遍历文件夹下的文件
        File[] files = folder.listFiles();
        if (null != files && 0 != files.length) {
            int length = files.length;
            if (length == 1) {
                File file = files[0];
                imageFilePath = file.getAbsolutePath();
            } else {
                List<File> filesList = getListFilesByOrder(folderPath);
                imageFilePath = filesList.get(0).getAbsolutePath();
            }
        }

        return imageFilePath;
    }

    /**
     * 获取文件夹下按最后修改时间的降序排列后的文件列表
     *
     * @param path
     * @return
     */
    public static List<File> getListFilesByOrder(String path) {
        File file_path = new File(path);
        //列出该目录下所有文件和文件夹
        File[] files = file_path.listFiles();
        //按照文件最后修改日期倒序排序
        Map<String, File> map = new LinkedHashMap<>();
        //取出第一个(即最新修改的)文件，打印文件名
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isHidden() && files[i].isFile()) {
                    File value = files[i];
                    String k = String.valueOf(files[i].lastModified());
                    k = getMilliseconds(k, i);
                    map.put(k, value);
                }
            }
        }
        return orderFile(map.size(), map);
    }

    /**
     * 解决linux读取毫秒数为000的情况
     *
     * @param val
     * @param i
     * @return
     */
    public static String getMilliseconds(String val, int i) {
        //截取后三位是否为000
        String returnStr;
        String lastStr = val.substring(val.length() - 3);
        if (lastStr.equals("000")) {
            String fisrtStr = val.substring(0, val.length() - 3);
            String lastNum = "001";
            if (i < 10) {
                lastNum = "00" + i;
            } else if (i < 100) {
                lastNum = "0" + i;
            } else {
                lastNum = i < 1000 ? String.valueOf(i) : lastNum;
            }
            returnStr = fisrtStr + lastNum;
        } else {
            returnStr = val;
        }
        return returnStr;
    }

    /**
     * 文件排序, 按降序排列
     *
     * @param x
     * @param map
     * @return
     */
    @SuppressWarnings("WhileLoopReplaceableByForEach")
    public static List<File> orderFile(int x, Map<String, File> map) {
        List<File> list = new ArrayList<>();
        Long[] arr = new Long[x];
        int i = 0;
        Iterator<String> ite = map.keySet().iterator();
        while (ite.hasNext()) {
            String k = ite.next();
            arr[i] = Long.parseLong(k);
            i++;
        }
        Arrays.sort(arr);
        for (int k = arr.length - 1; k >= 0; k--) {
            list.add(map.get("" + arr[k]));
        }
        return list;
    }


    /**
     * 计算目录下所有文件（包括子文件夹内文件）文件大小
     * 目录下所有文件的个数
     */
    public static long[] scanSizeAndCount(File dir) {
        long[] ret = new long[2];

        if (dir == null || !dir.exists()) {
            return ret;
        }

        if (!dir.isDirectory()) {
            ret[0] += 1;
            ret[1] += dir.length();
            return ret;
        }

        File[] child = dir.listFiles();
        if (child == null || child.length == 0) {
            return ret;
        }
        for (File file : child) {
            long[] tmp = scanSizeAndCount(file);
            ret[0] += tmp[0];
            ret[1] += tmp[1];
        }
        return ret;
    }

    /**
     * 计算目录下所有文件所占大小
     */
    public static long calcSize(File dir) {
        if (dir == null) {
            return 0;
        }
        if (!dir.exists()) {
            return 0;
        }
        if (!dir.isDirectory()) {
            return dir.length();
        }
        long sum = 0;
        File[] files = dir.listFiles();
        for (File file : files) {
            sum += calcSize(file);
        }
        return sum;
    }

    /**
     * 获取文件大小
     *
     * @param filePath 文件路径
     * @return 文件大小
     */
    @SuppressWarnings("ConstantConditions")
    public static long getFileSize(String filePath) {
        File file = new File(filePath);

        long size = 0;
        try {
            if (file != null && file.exists() && file.isFile()) {
                FileInputStream fis;
                fis = new FileInputStream(file);
                size = fis.available();
            } else {
                LogUtil.v(TAG, "获取文件大小-文件不存在!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return size;

    }


    /**
     * 获取剩余sd卡空间
     *
     * @return
     */
    public static long getSDFreeSize() {
        // 取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        // 获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        // 空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        // 返回SD卡空闲大小
        // return freeBlocks * blockSize; //单位Byte
        // return (freeBlocks * blockSize)/1024; //单位KB
        return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
    }


    /**
     * 根据文件路径获取文件名，包括文件扩展名
     *
     * @param filePath 文件路径
     * @return 例:test.jpg
     */
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) return "";

        int start = filePath.lastIndexOf("/");
        if (start != -1) {
            return filePath.substring(start + 1);
        }
        return filePath;
    }

    /**
     * 根据文件路径获取文件名，包括文件扩展名
     *
     * @param file 文件
     * @return 例:test.jpg
     */
    public static String getFileName(File file) {
        if (file == null) return "";

        return getFileName(file.getAbsolutePath());
    }

    /**
     * 获取文件名称(不包括扩展名)
     *
     * @param filePath 文件名称
     * @return 文件名:test.jpg 最后结果:test
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) return "";
        String fileName = getFileName(filePath);
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return "";
        }
    }

    /**
     * 获取文件名称(不包括扩展名)
     *
     * @param file 文件
     * @return 文件名:test.jpg 最后结果:test
     */
    public static String getFileNameWithoutExtension(File file) {
        if (file == null) return "";
        return getFileNameWithoutExtension(file.getPath());
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名称
     * @return
     */
    public static String getFileExtension(String fileName) {
        if (TextUtils.isEmpty(fileName)) return "";
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param file 文件
     * @return
     */
    public static String getFileExtension(File file) {
        if (file == null) return "";
        return getFileExtension(file.getPath());
    }


    /**
     * 复制单个文件
     *
     * @param oldPath$Name String 原文件路径+文件名 如：data/user/0/com.test/files/abc.txt
     * @param newPath$Name String 复制后路径+文件名 如：data/user/0/com.test/cache/abc.txt
     * @return <code>true</code> if and only if the file was copied;
     * <code>false</code> otherwise
     */
    public static boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                LogUtil.v(TAG, "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                LogUtil.v(TAG, "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                LogUtil.v(TAG, "copyFile:  oldFile cannot read.");
                return false;
            }

            /* 如果不需要打log，可以使用下面的语句
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //file文件读取成byte[]
    public static byte[] readFile(File file) {
        RandomAccessFile rf = null;
        byte[] data = null;
        try {
            rf = new RandomAccessFile(file, "r");
            data = new byte[(int) rf.length()];
            rf.readFully(data);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            closeQuietly(rf);
        }
        return data;
    }

    //关闭读取file
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     * */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static void copyAssetsFileIntoSdCard(Context context, String assetsFilePath, String sdCardFilePath) throws IOException {

        if (new File(sdCardFilePath).exists()) return;
        InputStream inputStream;

        inputStream = context.getResources().getAssets().open(assetsFilePath);// assets文件夹下的文件
        File file = new File(sdCardFilePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(sdCardFilePath);// 保存到本地的文件夹下的文件
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = inputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, count);
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        inputStream.close();

    }


    /**
     * 复制文件夹及其中的文件
     *
     * @param oldPath String 原文件夹路径 如：data/user/0/com.test/files
     * @param newPath String 复制后的路径 如：data/user/0/com.test/cache
     * @return <code>true</code> if and only if the directory and files were copied;
     *         <code>false</code> otherwise
     */
    public static boolean copyFolder(String oldPath, String newPath,boolean isImage) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.");
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String olderFolderName = oldFile.getName();
            String[] files = oldFile.list();
            File temp;

            if(null == files || files.length == 0){
                return true;
            }
            for (String file : files) {

                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file, isImage);
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.");
                    return false;
                } else if (!temp.isFile()) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.");
                    return false;
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.");
                    return false;
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + File.separator + (isImage?"":"video_")+olderFolderName+"-"+temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }

                /* 如果不需要打log，可以使用下面的语句
                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (temp.exists() && temp.isFile() && temp.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                 */
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
     * 复制文件夹及其中的文件
     *
     * @param oldPath String 原文件夹路径 如：data/user/0/com.test/files
     * @param newPath String 复制后的路径 如：data/user/0/com.test/cache
     * @return <code>true</code> if and only if the directory and files were copied;
     *         <code>false</code> otherwise
     */
    public static boolean copyFolderAndRenameFileName(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.");
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String olderFolderName = oldFile.getName();
            String[] files = oldFile.list();
            File temp;

            if(null == files || files.length == 0){
                return true;
            }
            for (String file : files) {

                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolderAndRenameFileName(oldPath + "/" + file, newPath + "/" + file);
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.");
                    return false;
                } else if (!temp.isFile()) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.");
                    return false;
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.");
                    return false;
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);

                    String newName = "";
                    String oldName = temp.getName();
                    String[] oldNameArray = oldName.split("_");
                    String devName= oldNameArray[0];
                    String date = oldNameArray[1];
                    String time = oldNameArray[2].substring(3);

                    String newTime = time.substring(0,2)+"-"+time.substring(2,4)+"-"+time.substring(4);

                    newName = devName+"-"+time.substring(0,3)+"_"+date+"-"+newTime;
                    LogUtil.v(TAG,"devName="+devName+";\ndate="+date+";\ntime="+time+";\nnewTime="+newTime+";\nnewName="+newName);


                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + File.separator +newName);
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }

                /* 如果不需要打log，可以使用下面的语句
                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (temp.exists() && temp.isFile() && temp.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                 */
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 方法名称： createFolder
     * 方法作用：创建文件夹
     * @param path ：路径
     * @return
     * 返回值类型：int
     * 返回值含义：0代表 文件夹已存在 1代表文件夹创建成功
     * 作者：livingwater
     * 时间：2018年8月16日  下午7:25:21
     */
    public static int createFolder(String path) {
        int result=0;
        // 创建文件夹
        File folder = new File(path);
        // 判断文件夹是否存在
        if (!folder.exists() && !folder.isDirectory()) {
            System.out.println("文件夹创建成功，路径是:" + folder.getPath());
            folder.mkdirs();
            result = 1;
        } else {
            System.out.println("文件夹已存在:" + folder.getPath());
        }
        return result;
    }
}
