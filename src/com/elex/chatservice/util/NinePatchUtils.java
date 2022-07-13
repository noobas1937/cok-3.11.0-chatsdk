package com.elex.chatservice.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
 /***
  * 
  * @author Administrator
  *
  */
public class NinePatchUtils {
 
	private final static int NO_COLOR = 0x00000001;
	private NinePatchUtils() { }
	/***
	 * 读取assets下图片,此处修改了NinePatchDrawable不能正常读取问题，
	 * e.g
	 * 		Drawable bg = NinePatchTool.decodeDrawableFromAsset(this, path);
	 * 
	 * assetPath:如果存在子目录，则需要写子目录名称，如下："aaa/log.png"
	 * @param context
	 * @param assetPath
	 * @return
	 * @throws Exception
	 */
	public static Drawable decodeDrawableFromAsset(Context context,
			String assetPath) throws Exception {
		Bitmap bitmap = readFromAsset(context, assetPath);
		if (bitmap.getNinePatchChunk() == null) {
			BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
			return bitmapDrawable;
		} else {
			Rect paddingRect = new Rect();
			readPaddingFromChunk(bitmap.getNinePatchChunk(), paddingRect);
			return new NinePatchDrawable(context.getResources(),
					bitmap, bitmap.getNinePatchChunk(), paddingRect, null);
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param assetPath
	 * @return
	 * @throws Exception
	 */
	public static Drawable decodeDrawableFromOtherResour(Context context,
			String assetPath) throws Exception {
		Bitmap bitmap = decodeFromFile(assetPath);
		if (bitmap.getNinePatchChunk() == null)
		{
			BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
			return bitmapDrawable;
		} else 
		{
			Rect paddingRect = new Rect();
			readPaddingFromChunk(bitmap.getNinePatchChunk(), paddingRect);
			return new NinePatchDrawable(context.getResources(),
					bitmap, bitmap.getNinePatchChunk(), paddingRect, null);
		}
	}
	
	public static Bitmap decodeNineFromStream(InputStream in) throws Exception {
		Rect pad = new Rect();
		BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScreenDensity = ScaleUtil.getScreenDensity();
        opts.inDensity = 240;
        opts.inTargetDensity = ScaleUtil.getScreenDensity();
        
        Bitmap srcBm = BitmapFactory.decodeStream(in, pad, opts);
		byte[] chunk = readChunk(srcBm);
		boolean isNinePatchChunk = NinePatch.isNinePatchChunk(chunk);
		if (isNinePatchChunk) {
			Bitmap tgtBm = Bitmap.createBitmap(srcBm, 1, 1,
					srcBm.getWidth() - 2, srcBm.getHeight() - 2);
			srcBm.recycle();
			Field f = tgtBm.getClass().getDeclaredField("mNinePatchChunk");
			f.setAccessible(true);
			f.set(tgtBm, chunk);
			return tgtBm;
		} else {
			return srcBm;
		}
	}
	
	/***
	 * 通过流读取图片
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static Bitmap decodeFromStream(InputStream in) throws Exception {
		Rect pad = new Rect();
		// 从web加载的图片没有dpi信息，默认为屏幕的dpi，不会缩放；与drawable下加载资源的dpi可能不一样。需要手动缩放
		BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScreenDensity = ScaleUtil.getScreenDensity();
        opts.inDensity = 240; // hdpi（默认资源文件夹）对应的密度
        opts.inTargetDensity = ScaleUtil.getScreenDensity();

        int ratio = 1;
		Bitmap srcBm = null;
		// TODO 当屏幕小于240时，drawable下面的资源可以缩小，但加载的资源不会缩小（缩小的话色值会改变，readChunk读不到九宫格信息）
        if(ScaleUtil.getScreenDensity() > (double) opts.inDensity)
        {
        	ratio = (int) Math.ceil((double) ScaleUtil.getScreenDensity() / (double) opts.inDensity + 0.5f);
    		// 根据dpi，对原图进行放大（模仿drawable的机制）
    		srcBm = BitmapFactory.decodeStream(in, pad, opts);
        }else{
        	srcBm = BitmapFactory.decodeStream(in);
        }
		
		byte[] chunk = readChunk(srcBm);
		boolean isNinePatchChunk = NinePatch.isNinePatchChunk(chunk);
		if (isNinePatchChunk) {
			// 裁去放大后多余的黑边
			Bitmap tgtBm = Bitmap.createBitmap(srcBm, 1 * ratio, 1 * ratio,
					srcBm.getWidth() - 2 * ratio, srcBm.getHeight() - 2 * ratio);
			srcBm.recycle();
			Field f = tgtBm.getClass().getDeclaredField("mNinePatchChunk");
			f.setAccessible(true);
			f.set(tgtBm, chunk);
			return tgtBm;
		} else {
			return srcBm;
		}
	}
	/***
	 * 通过制定目录读取文件
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static Bitmap decodeFromFile(String path) throws Exception {
		InputStream in = new FileInputStream(path);
		Bitmap bm = decodeFromStream(in);
		in.close();
		return bm;
	}
	/***
	 * 读取Assets下图片包括.9.png
	 * @param context
	 * @param ninePatchPngPath
	 * @return
	 * @throws Exception
	 */
	public static Bitmap readFromAsset(Context context,
			String ninePatchPngPath) throws Exception 
	{
		InputStream is = context.getAssets().open(ninePatchPngPath);
		Bitmap bm = decodeFromStream(is);
		is.close();
		return bm;
	}
 
	public static void readPaddingFromChunk(byte[] chunk, Rect paddingRect) {
		paddingRect.left = getInt(chunk, 12);
		paddingRect.right = getInt(chunk, 16);
		paddingRect.top = getInt(chunk, 20);
		paddingRect.bottom = getInt(chunk, 24);
	}
 
	public static byte[] readChunk(Bitmap resourceBmp) throws IOException {
		final int BM_W = resourceBmp.getWidth() ;
		final int BM_H = resourceBmp.getHeight() ;
 
		int xPointCount = 0;
		int yPointCount = 0;
		int xBlockCount = 0;
		int yBlockCount = 0;
 
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		for (int i = 0; i < 32; i++) {
			bao.write(0);
		}
 
		{
			int[] pixelsTop = new int[BM_W - 2];
			resourceBmp.getPixels(pixelsTop, 0, BM_W, 1, 0, BM_W - 2, 1);
			boolean topFirstPixelIsBlack = pixelsTop[0] == Color.BLACK;
			boolean topLastPixelIsBlack = pixelsTop[pixelsTop.length - 1] == Color.BLACK;
			int tmpLastColor = Color.TRANSPARENT;
			for (int i = 0, len = pixelsTop.length; i < len; i++) {
				if (tmpLastColor != pixelsTop[i]) {
					xPointCount++;
					writeInt(bao, i);
					tmpLastColor = pixelsTop[i];
				}
			}
			if (topLastPixelIsBlack) {
				xPointCount++;
				writeInt(bao, pixelsTop.length);
			}
			xBlockCount = xPointCount + 1;
			if (topFirstPixelIsBlack) {
				xBlockCount--;
			}
			if (topLastPixelIsBlack) {
				xBlockCount--;
			}
		}
 
		{
			int[] pixelsLeft = new int[BM_H - 2];
			resourceBmp.getPixels(pixelsLeft, 0, 1, 0, 1, 1, BM_H - 2);
			boolean firstPixelIsBlack = pixelsLeft[0] == Color.BLACK;
			boolean lastPixelIsBlack = pixelsLeft[pixelsLeft.length - 1] == Color.BLACK;
			int tmpLastColor = Color.TRANSPARENT;
			for (int i = 0, len = pixelsLeft.length; i < len; i++) {
				if (tmpLastColor != pixelsLeft[i]) {
					yPointCount++;
					writeInt(bao, i);
					tmpLastColor = pixelsLeft[i];
				}
			}
			if (lastPixelIsBlack) {
				yPointCount++;
				writeInt(bao, pixelsLeft.length);
			}
			yBlockCount = yPointCount + 1;
			if (firstPixelIsBlack) {
				yBlockCount--;
			}
			if (lastPixelIsBlack) {
				yBlockCount--;
			}
		}
 
		{
			for (int i = 0; i < xBlockCount * yBlockCount; i++) {
				writeInt(bao, NO_COLOR);
			}
		}
 
		byte[] data = bao.toByteArray();
		data[0] = 1;
		data[1] = (byte) xPointCount;
		data[2] = (byte) yPointCount;
		data[3] = (byte) (xBlockCount * yBlockCount);
		dealPaddingInfo(resourceBmp, data);
		return data;
	}
 
	private static void dealPaddingInfo(Bitmap bm, byte[] data) {
		{ // padding left & padding right
			final int BM_W = bm.getWidth();
			final int BM_H = bm.getHeight();
			int[] bottomPixels = new int[BM_W - 2];
			bm.getPixels(bottomPixels, 0, bottomPixels.length, 1,
					BM_H - 1, bottomPixels.length, 1);
			for (int i = 0; i < bottomPixels.length; i++) {
				if (Color.BLACK == bottomPixels[i]) { // padding left
					writeInt(data, 12, i);
					break;
				}
			}
			for (int i = bottomPixels.length - 1; i >= 0; i--) {
				if (Color.BLACK == bottomPixels[i]) { // padding right
					writeInt(data, 16, bottomPixels.length - i - 2);
					break;
				}
			}
		}
		{ // padding top & padding bottom
			final int BM_W = bm.getWidth() ;
			final int BM_H = bm.getHeight();
			int[] rightPixels = new int[BM_H - 2];
			bm.getPixels(rightPixels, 0, 1, BM_W - 1, 0, 1,
					rightPixels.length);
			for (int i = 0; i < rightPixels.length; i++) {
				if (Color.BLACK == rightPixels[i]) { // padding top
					writeInt(data, 20, i);
					break;
				}
			}
			for (int i = rightPixels.length - 1; i >= 0; i--) {
				if (Color.BLACK == rightPixels[i]) { // padding bottom
					writeInt(data, 24, rightPixels.length - i - 2);
					break;
				}
			}
		}
	}
 
	private static void writeInt(OutputStream out, int v) throws IOException {
		out.write((v >> 0) & 0xFF);
		out.write((v >> 8) & 0xFF);
		out.write((v >> 16) & 0xFF);
		out.write((v >> 24) & 0xFF);
	}
 
	private static void writeInt(byte[] b, int offset, int v) {
		b[offset + 0] = (byte) (v >> 0);
		b[offset + 1] = (byte) (v >> 8);
		b[offset + 2] = (byte) (v >> 16);
		b[offset + 3] = (byte) (v >> 24);
	}
 
	private static int getInt(byte[] bs, int from) {
		int b1 = bs[from + 0];
		int b2 = bs[from + 1];
		int b3 = bs[from + 2];
		int b4 = bs[from + 3];
		int i = b1 | (b2 << 8)  | (b3 << 16) | b4 << 24;
		return i;
	}
 
	/** print chunk info from bitmap */
	public static void printChunkInfo(Bitmap bm) {
		byte[] chunk = bm.getNinePatchChunk();
		if (null == chunk) {
			System.out.println("can't find chunk info from this bitmap(" + bm
					+ ")");
			return;
		}
		int xLen = chunk[1];
		int yLen = chunk[2];
		int cLen = chunk[3];
 
		StringBuilder sb = new StringBuilder();
		int peddingLeft = getInt(chunk, 12);
		int paddingRight = getInt(chunk, 16);
		int paddingTop = getInt(chunk, 20);
		int paddingBottom = getInt(chunk, 24);
		sb.append("peddingLeft=" + peddingLeft);
		sb.append("\r\n");
		sb.append("paddingRight=" + paddingRight);
		sb.append("\r\n");
		sb.append("paddingTop=" + paddingTop);
		sb.append("\r\n");
		sb.append("paddingBottom=" + paddingBottom);
		sb.append("\r\n");
 
		sb.append("x info=");
		for (int i = 0; i < xLen; i++) {
			int vv = getInt(chunk, 32 + i * 4);
			sb.append("," + vv);
		}
		sb.append("\r\n");
		sb.append("y info=");
		for (int i = 0; i < yLen; i++) {
			int vv = getInt(chunk, xLen * 4 + 32 + i * 4);
			sb.append("," + vv);
		}
		sb.append("\r\n");
		sb.append("color info=");
		for (int i = 0; i < cLen; i++) {
			int vv = getInt(chunk, xLen * 4 + yLen * 4 + 32 + i * 4);
			sb.append("," + vv);
		}
		System.err.println("" + sb);
	}
}
