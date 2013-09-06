package com.livestream.slideshow.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.VideoView;

import com.livestream.folder.Folder;
import com.livestream.slideshow.Settings;
import com.livestream.slideshow.SlideshowActivity;

public class TimedSlideShow implements RemoteNotifier{
	private static final String TAG = "TimedSlideShow";
	Activity activity;
	
	public Folder folderA;
	public Folder folderB;
	
	private Settings settings = new Settings(5, 10, "seconds", "seconds");
	
	private FileObserver primaryObserver;
	private FileObserver secondaryObserver;
	
	private List<ImgVideoData> primaryImgVideoList = null;
	private List<ImgVideoData> secondaryImgVideoList = null;
	
	private boolean displayPrimaryFolder = false;
	
	public boolean isDisplayPrimaryFolder() {
		return displayPrimaryFolder;
	}

	public void setDisplayPrimaryFolder(boolean displayPrimaryFolder) {
		this.displayPrimaryFolder = displayPrimaryFolder;
	}
	
	public boolean togglePrimaryDisplay(){
		boolean displayPrimary = isDisplayPrimaryFolder();
		boolean isEmpty =isEmpty(folderA.getPath()); 
		Log.e(TAG, "TOGGLE : Primary " + displayPrimary + " Empty " + isEmpty);
		if(displayPrimary == false && isEmpty == false){
			setDisplayPrimaryFolder(true);
			return true;
		}else if(isDisplayPrimaryFolder()){
			setDisplayPrimaryFolder(false);
			return false;
		}
		return false;
	}

	ImageView imageView;
	VideoView videoView;
	
	View container;
	
	public TimedSlideShow(Activity activity, ImageView imageView, VideoView videoView, View containerView) {
		primaryImgVideoList = new ArrayList<ImgVideoData>();
		secondaryImgVideoList = new ArrayList<ImgVideoData>();
		this.container = containerView;
		this.imageView = imageView; // Image View for displaying images
		this.videoView = videoView; // Vide View for display videos
		
		this.activity = activity;
		
	}

	/**
	 * Update the timer values
	 */
	public void updateSettings(Settings newSettings){
		if(newSettings != null){
			
			this.settings = newSettings;
			if(toggleDisplay != null && toggleDirectory != null){
				
				toggleDisplay.updateTime(newSettings.getDuration());
				toggleDirectory.updateInterval(newSettings.getPreset());
				
			}
			
		}

	}

	
	boolean isEmpty(String path){
		
		File[] files = new File(path).listFiles();
		return files.length > 0 ? false : true;
		
	}
	
	
	/**
	 * Update Data when File Observer is invoked and on start of app
	 */
	public void updateData(String path){
		
		File[] files = new File(path).listFiles();
		boolean isWithHeaderA = false;
		
		if(isFileWithHeaderName(path, SlideshowActivity.FOLDER_A)){
			
			primaryImgVideoList.clear();
			isWithHeaderA = true;
			setDisplayPrimaryFolder(true);
		}else {
			
			secondaryImgVideoList.clear();
			isWithHeaderA = false;
		}
		
		// Check all files received and update
		for (File file : files) {
			
			if (file.toString().endsWith(".jpg") || file.toString().endsWith(".JPG") || 
					file.toString().endsWith(".png") || file.toString().endsWith(".PNG")) {
				
				ImgVideoData data = new ImgVideoData();
				
			    Bitmap imgBitmap = getScaledImage(file.getAbsolutePath());
			    
				data.setImgBitMap(imgBitmap);
				if(isWithHeaderA)
					primaryImgVideoList.add(data); 
				else 
					secondaryImgVideoList.add(data);
				
			} else if (file.toString().endsWith(".mp4")) {
				ImgVideoData data = new ImgVideoData();
				data.setVideoUrl(file.getPath());
				secondaryImgVideoList.add(data);
			}
		}
		
	}
	

	/* (non-Javadoc)
	 * @see com.livestream.slideshow.util.RemoteNotifier#configurationReceived(int, int)
	 */
	@Override
	public void configurationReceived(int duration, int cycle) {
		Settings settings = new Settings(cycle, duration, "seconds", "seconds");
		updateSettings(settings);
		
	}

	
	private void reset() {
		toggleDisplay.refresh();
		
	}

	private void initListeners() {
		// Listen to changes in Folder A
		primaryObserver = new FileObserver(folderA.getPath(),FileObserver.CREATE) {
			
			@Override
			public void onEvent(int event, String path) {
				
				Log.i(TAG, "File Received create in path: "+" Event->" + event +" " +folderA.getPath()+"/"+path );
				updateData(folderA.getPath());
				setDisplayPrimaryFolder(true);

				reset();
				
			}
			
		};
		
		
		// Listen to changes in Folder B
		secondaryObserver = new FileObserver(folderB.getPath(),FileObserver.CREATE) {
			
			@Override
			public void onEvent(int event, String path) {
				Log.i(TAG, "File Received in path: " + folderB.getPath()+"/"+path);
				updateData(folderB.getPath());
				
			}
		};
		
		
	}
	
	Handler handler;
	ToggleDisplay toggleDisplay;
	ToggleDirectory toggleDirectory;

	public void startSlideShow(){
		
		initListeners(); // Initialize File Observers for each folder or just a location as per requirement (Not yet clarified)
		updateData(folderA.getPath());
		updateData(folderB.getPath());

		Log.i(TAG, " STARTING SLIDESHOW DURATION: " + settings.getDuration() + " PRESET: " + settings.getPreset());
		handler = new Handler();
		
		toggleDisplay = new ToggleDisplay(settings.getDuration());
		toggleDirectory = new ToggleDirectory(settings.getPreset());
		
		primaryObserver.startWatching();
		secondaryObserver.startWatching();
		
		handler.post(toggleDisplay);
		handler.postDelayed(toggleDirectory, settings.getPreset());
	}
	
	
	class ToggleDisplay implements Runnable {

		long startTime;
		long interval;

		private int currentIndex = 0;
		int length = 0;

		public ToggleDisplay(long toggleDuration) {
			this.interval = toggleDuration;
		}

		@Override
		public void run() {
			Log.i(TAG, " TOGGLE RUN");

			startTime = System.currentTimeMillis();

			ImgVideoData data = getData();

			if (data != null) {
				currentIndex++;

				if (data.isImage()) {

					disable(videoView);

					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
					imageView.setVisibility(View.VISIBLE);
					imageView.setImageBitmap(data.getImgBitMap());

					animate(container, 500, 1000, (int) (interval - 1500));

				} else {
					disable(imageView);

					videoView.setVideoPath(data.getVideoUrl());
					videoView.setVisibility(View.VISIBLE);

					videoView.start();
					animate(container, 500, 1000, (int) (interval - 1500));
				}

			}

			handler.postDelayed(toggleDisplay, interval);
		}

		private ImgVideoData getData() {
			ImgVideoData data = null;
			if (primaryImgVideoList.size() > 0 || secondaryImgVideoList.size() > 0) {
				if (isDisplayPrimaryFolder()) {
					length = primaryImgVideoList.size();
					if (currentIndex >= length)
						currentIndex = 0;

					data = primaryImgVideoList.get(currentIndex);
				} else {
					length = secondaryImgVideoList.size();
					if (currentIndex >= length)
						currentIndex = 0;

					data = secondaryImgVideoList.get(currentIndex);
				}
				return data;
			}
			return null;
		}

		private void animate(View viewContainer, int fadeInDuration, int fadeOutDuration, int timeBetween) {

			//viewContainer.setVisibility(View.INVISIBLE);

			Animation fadeIn = new AlphaAnimation(0, 1);
			fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
			fadeIn.setDuration(fadeInDuration);

			Animation fadeOut = new AlphaAnimation(1, 0);
			fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
			fadeOut.setStartOffset(fadeInDuration + timeBetween);
			fadeOut.setDuration(fadeOutDuration);

			AnimationSet animation = new AnimationSet(false); // change to false
			animation.addAnimation(fadeIn);
			animation.addAnimation(fadeOut);
			animation.setRepeatCount(1);
			viewContainer.setAnimation(animation);

		}

		private void disable(ImageView imageView) {

			imageView.setVisibility(View.GONE);

		}

		private void disable(VideoView videoView) {

			videoView.setVisibility(View.GONE);

			if (videoView.isPlaying()) {
				videoView.stopPlayback();
				videoView.suspend();
			}

		}

		public void updateTime(long newTime) {
			this.interval = newTime;

			long currentTime = System.currentTimeMillis();
			long elapsedTime = startTime - currentTime;
			handler.removeCallbacks(toggleDisplay);

			handler.postDelayed(toggleDisplay, newTime - elapsedTime);

		}

		public void setAnimation(View view, int fadeInDuration,
				int fadeOutDuration, int interval) {
			view.setVisibility(View.INVISIBLE); // Visible or invisible by
												// default - this will apply
												// when the animation ends

		}

		public void refresh() {

			handler.removeCallbacks(toggleDisplay);
			handler.post(toggleDisplay);

		}

	}
	
	class ToggleDirectory implements Runnable{
		
		long interval;
		
		public ToggleDirectory(long duration) {
			this.interval = duration;
		}

		@Override
		public void run() {
			
			Log.i(TAG, " TOGGLE DIRECTORY RUN");

			togglePrimaryDisplay();
			toggleDisplay.refresh();
			handler.postDelayed(this, interval);
		}
		
		public void updateInterval(long newInterval){
			this.interval = newInterval;
			handler.removeCallbacks(toggleDirectory);
			handler.postDelayed(this, newInterval);
		}
		
	}
	 
	
	boolean isFileWithHeaderName(String path, String header){
		if(path.contains(header)){
			return true;
		}
		return false;
	}
	
	public void stopSlideShow(){
		
		handler.removeCallbacks(toggleDisplay);
		handler.removeCallbacks(toggleDirectory);
		
	}
	
	private Bitmap getScaledImage(String path) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
		BitmapFactory.decodeFile(path, options);
		
		// Calculate inSampleSize

		Point size = getScreenSize(activity);
		int requiredWidth = size.x;
		int requiredHeight = size.y;		

		
		Log.i(TAG, " Setting width and height for image bitmap: " + requiredWidth + " ~ " + requiredHeight);
		options.inSampleSize = calculateInSampleSize(options,requiredWidth ,requiredHeight );
		
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    
	    return BitmapFactory.decodeFile(path, options);
	    
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static Point getScreenSize(Activity a) {
	    Point size = new Point();
	    Display d = a.getWindowManager().getDefaultDisplay();
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	        d.getSize(size);
	    } else {
	        size.x = d.getWidth();
	        size.y = d.getHeight();
	    }
	    return size;
	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
	
}
