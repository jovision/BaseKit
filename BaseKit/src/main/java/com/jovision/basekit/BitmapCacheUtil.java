package com.jovision.basekit;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video;


/**
 * @author 该类用于图片缓存，防止内存溢出
 */
public class BitmapCacheUtil {

    static BitmapCacheUtil bitmapCache;

    /**
     * 取得缓存器实例
     */
    public static BitmapCacheUtil getInstance() {
        if (bitmapCache == null) {
            bitmapCache = new BitmapCacheUtil();
        }
        return bitmapCache;

    }

    /**
     * 按照路径加载图片
     *
     * @param path     图片资源的存放路径
     * @param scalSize 缩小的倍数
     * @return
     */
    public static Bitmap loadImageBitmap(String path, int scalSize) {
        LogUtil.e("loadImageBitmap--from-local", path);
        if (-1 == scalSize) {
            return BitmapFactory.decodeFile(path);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = scalSize;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        return bmp;
    }

    /**
     * 获取视频缩略图
     *
     * @param filePath
     * @return
     */
    public static Bitmap loadVideoBitmap(String filePath) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath,
                Video.Thumbnails.MINI_KIND);
        return bitmap;
    }

    /**
     * 获取视频缩略图
     *
     * @param path
     * @return
     */
    public static Bitmap decodeVideoThumbnail(String path, int width,
                                              int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (bitmap != null) {
            Bitmap scale = Bitmap.createScaledBitmap(bitmap, width, height, true);
            return scale;
        }
        return bitmap;
    }

    // 垂直拼接图片
    public static Bitmap montageBitmapVertical(String firstPath, String secondPath) {
        return montageBitmapVertical(loadImageBitmap(firstPath, -1), loadImageBitmap(secondPath, -1));
    }

    public static Bitmap montageBitmapVertical(Bitmap first, Bitmap second) {
        if (first == null && second == null) {
            return null;
        }
        if (first != null && second == null) {
            return first;
        }
        if (first == null && second != null) {
            return second;
        }
        int width = Math.max(first.getWidth(), second.getWidth());
        int height = first.getHeight() + second.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(first, 0, 0, null);
        canvas.drawBitmap(second, 0, first.getHeight(), null);
        return result;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources resources, int id,
                                                         int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(resources, id, options);

    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

}
