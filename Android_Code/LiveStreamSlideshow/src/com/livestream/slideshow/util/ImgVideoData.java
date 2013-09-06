/**
 * 
 */
package com.livestream.slideshow.util;

import android.graphics.Bitmap;

/**
 * @author ArunAbraham
 *
 */
public class ImgVideoData {

	public String header;
	
	private Bitmap imgBitMap = null;
	private String videoUrl = null;

	public ImgVideoData() {
		// TODO Auto-generated constructor stub
	}
	
	public ImgVideoData(Bitmap imgBitMap, String videoUrl) {
		this.imgBitMap = imgBitMap;
		this.videoUrl = videoUrl;
	}
	
	public boolean isImage(){
		return this.imgBitMap != null ? true : false;
	}
	
	public boolean isVideo(){
		return this.videoUrl != null ? true : false;
	}

	public Bitmap getImgBitMap() {
		return imgBitMap;
	}

	public void setImgBitMap(Bitmap imgBitMap) {
		this.imgBitMap = imgBitMap;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}
	
	

}
