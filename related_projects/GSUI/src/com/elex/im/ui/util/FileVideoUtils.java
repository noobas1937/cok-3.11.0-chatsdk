package com.elex.im.ui.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class FileVideoUtils {
    public static String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection,null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }

        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    
	public static String saveJpegToSDCard(Bitmap photoBitmap,String path,String photoName){
		String absolutePath = "";
		if (checkSDCardAvailable()) {
			File dir = new File(path);
			if (!dir.exists()){
				dir.mkdirs();
			}
			
			File photoFile = new File(path , photoName + ".jpeg");
			absolutePath = photoFile.getAbsolutePath();
			
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(photoFile);
				if (photoBitmap != null) {
					if (photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)) {
						fileOutputStream.flush();
					}
				}
			} catch (FileNotFoundException e) {
				photoFile.delete();
				e.printStackTrace();
			} catch (IOException e) {
				photoFile.delete();
				e.printStackTrace();
			} finally{
				try {
					fileOutputStream.close();
				} catch (Throwable e) {
					//e.printStackTrace();
				}
			}
		} 
		
		return absolutePath;
	}
	
	public static boolean checkSDCardAvailable(){
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}
	
	public static void saveImageToAlbum(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "cokPicture");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        // 其次把文件插入到系统图库
        try {
            String saveImg=  MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
            if(saveImg != null)
            {
//                Toast toast=Toast.makeText(context,    "保存成功", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
            	Log.d("cok", "savePicture success");
            }
            else {
//                Toast toast=Toast.makeText(context,    "保存失败", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
            	Log.d("cok", "savePicture fail");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
       
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,    Uri.fromFile(new File(file.getPath()))));
    }
}
