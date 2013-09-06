package com.livestream.slideshow;

import static com.livestream.slideshow.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.livestream.slideshow.CommonUtilities.EXTRA_MESSAGE;
import static com.livestream.slideshow.CommonUtilities.SENDER_ID;
import static com.livestream.slideshow.CommonUtilities.SERVER_URL;

import java.io.File;
import java.io.FilenameFilter;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gcm.GCMRegistrar;
import com.livestream.folder.Folder;
import com.livestream.slideshow.SettingsDialog.SettingsUpdatedListener;
import com.livestream.slideshow.util.SystemUiHider;
import com.livestream.slideshow.util.TimedSlideShow;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class SlideshowActivity extends FragmentActivity implements SettingsUpdatedListener{
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	private ImageView imageView;
	private VideoView videoView;
	
	private SettingsUpdatedListener settingsListener;
	View containerView;
	
	PowerManager pm = null;
	PowerManager.WakeLock wl = null;
	Button configureButton, configureTimer;
	
	AsyncTask<Void, Void, Void> mRegisterTask;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settingsListener = this;
		checkNotNull(SERVER_URL, "SERVER_URL");
		checkNotNull(SENDER_ID, "SENDER_ID");

		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set 
		GCMRegistrar.checkManifest(this);

		setContentView(R.layout.activity_slideshow);
		
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));

		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, SENDER_ID);
		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				//mDisplay.append(getString(R.string.already_registered) + "\n");
				Toast.makeText(getApplicationContext(), getString(R.string.already_registered), Toast.LENGTH_SHORT).show();
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						boolean registered = ServerUtilities.register(context,
								regId);
						// At this point all attempts to register with the app
						// server failed, so we need to unregister the device
						// from GCM - the app will try to register again when
						// it is restarted. Note that GCM will send an
						// unregistered callback upon completion, but
						// GCMIntentService.onUnregistered() will ignore it.
						if (!registered) {
							GCMRegistrar.unregister(context);
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}

		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
	    	//wl = pm.newWakeLock(PowerManager., "SlideShowActivity");
	    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    		
	    }else{
	    	wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "SlideShowActivity");
	    	wl.acquire();
	    }
	    
	    
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);
		containerView = findViewById(R.id.containerView);
		imageView = (ImageView) findViewById(R.id.imageView1);
		videoView = (VideoView) findViewById(R.id.videoView1);
		
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		configureButton = (Button)findViewById(R.id.configureButton);
		configureButton.setOnTouchListener(mDelayHideTouchListener);

		configureTimer = (Button) findViewById(R.id.configureTimer);
		configureTimer.setOnTouchListener(mDelayHideTouchListener);
		
	}
	
	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(getString(R.string.error_config,
					name));
		}
	}

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);// To be changed from server
			newMessage = "Settings received from app server";
			//mDisplay.append(newMessage + "\n");
			Toast.makeText(getApplicationContext(), newMessage, Toast.LENGTH_SHORT).show();
			ParseJSON json = new ParseJSON();
			json.setSettingsListener(settingsListener);
			json.execute("http://livestreamslideshow.appspot.com/getSettings");
			
		}
		
	};

	
 
	
	private File[] fileList;
	private String[] filenameList;
	public static final String PACKAGE_NAME = "com.livestream.slideshow"; 
	public static final String KEY_DIRECTORY_SELECTED =  PACKAGE_NAME + ".DIRECTORY_SELECTED";

	private SharedPreferences prefs;
	String oldChosenDirectory;
	public static final String FOLDER_A = "Folder_A";
	public static final String FOLDER_B = "Folder_B";
	
	String folderAPath = null;
	String folderBPath = null;
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
		
		// Do rest of the initialization here.
		prefs = getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
		
		//if no saved directory yet, use SDCard directory as default
		oldChosenDirectory = prefs.getString(KEY_DIRECTORY_SELECTED, null);
		
		// Store the directory path in preferrences, so that the user is prompted only once.
		if(oldChosenDirectory == null){
			
			oldChosenDirectory = Environment.getExternalStorageDirectory().toString();
			showFileListDialog(oldChosenDirectory, SlideshowActivity.this);
			
		}else{
			
            folderAPath = oldChosenDirectory + "/" +FOLDER_A;
            folderBPath = oldChosenDirectory + "/"+FOLDER_B;
            initSlideShow(null);
            
		}
		
		configureButton.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				showFileListDialog(oldChosenDirectory, SlideshowActivity.this);
				
			}
		});

		
		configureTimer.setOnClickListener(new android.view.View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				showEditDialog();
				
			}
			
		});
		
	}
	
	private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SettingsDialog editNameDialog = new SettingsDialog();
        editNameDialog.show(fm, "settings");
    }
	

	
	TimedSlideShow slideShow = null;
	
	void initSlideShow(Settings settings){
		
		Folder primaryFolder = new Folder(folderAPath);
		Folder secondaryFolder = new Folder(folderBPath);
		// Stop current slideshow
		if(slideShow != null)
			slideShow.stopSlideShow();
		
		slideShow = new TimedSlideShow(this, imageView, videoView, containerView);
		slideShow.folderA = primaryFolder;
		slideShow.folderB = secondaryFolder;
		
		
		slideShow.startSlideShow();
		
	}
	
	
	private File[] loadFileList(String directory) {
	    File path = new File(directory);

	    if(path.exists()) {
	        
	        FilenameFilter filter = new FilenameFilter() {

				@Override
	            public boolean accept(File dir, String filename) {
	            	File file = new File(dir, filename);
	                return file.isDirectory();
	            	
	            }
	            
	            
	        };

	        //if null return an empty array instead
	        File[] list = path.listFiles(filter); 
	        return list == null? new File[0] : list;
	    } else {
	        return new File[0];
	    }
	}
	
	public void showFileListDialog(final String directory, final Context context){
	    Dialog dialog = null;
	    AlertDialog.Builder builder = new Builder(context);

	    File[] tempFileList = loadFileList(directory);

	    //if directory is root, no need to up one directory
	    if(directory.equals("/")){
	        fileList = new File[tempFileList.length];
	        filenameList = new String[tempFileList.length];

	        //iterate over tempFileList
	        for(int i = 0; i < tempFileList.length; i++){
	            fileList[i] = tempFileList[i];
	            filenameList[i] = tempFileList[i].getName();
	        }
	    } else {
	        fileList = new File[tempFileList.length+1];
	        filenameList = new String[tempFileList.length+1];

	        //add an "up" option as first item
	        fileList[0] = new File(upOneDirectory(directory));
	        filenameList[0] = "..";

	        //iterate over tempFileList
	        for(int i = 0; i < tempFileList.length; i++){
	            fileList[i+1] = tempFileList[i];
	            filenameList[i+1] = tempFileList[i].getName();
	        }
	    }

	    builder.setTitle("Select folder that contains 'Folder_A' & 'Folder_B': " + directory);

	    builder.setItems(filenameList, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	            File chosenFile = fileList[which];

	            if(chosenFile.isDirectory())
	                showFileListDialog(chosenFile.getAbsolutePath(), context);
	        }
	    });
	    
	    builder.setNegativeButton("Cancel", new OnClickListener() {

	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	        }

	    });
	    builder.setPositiveButton("Save Directory", new OnClickListener() {

	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            prefs.edit().putString(KEY_DIRECTORY_SELECTED, directory).commit();
	            // Set the two folder names (Hardcoded, not a good practice !!)
	            folderAPath = directory + "/" +FOLDER_A;
	            folderBPath = directory + "/"+FOLDER_B;
	            //Toast.makeText(getApplicationContext(), "File location "  + " A-> " + folderAPath , Toast.LENGTH_LONG).show();
	            initSlideShow(null);
	        }
	    });
	    
	    dialog = builder.create();
	    dialog.show();
	}
	
	public String upOneDirectory(String directory){
		String[] dirs = directory.split("/");
		    StringBuilder stringBuilder = new StringBuilder("");

		    for(int i = 0; i < dirs.length-1; i++)
		        stringBuilder.append(dirs[i]).append("/");

		    return stringBuilder.toString();
		}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if(wl != null)
			wl.release();

	}
	
	@Override
	protected void onDestroy() {
		
		if(slideShow != null)
			slideShow.stopSlideShow();
		
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		unregisterReceiver(mHandleMessageReceiver);
		GCMRegistrar.onDestroy(this);
		super.onDestroy();

	}

	@Override
	public void settingsChanged(Settings settings) {
		Toast.makeText(getApplicationContext(), "Settings changed to Duration(millis): "+settings.getDuration() + " Preset: " + settings.getPreset(), Toast.LENGTH_LONG).show();
		
        if(slideShow != null){
        	slideShow.stopSlideShow();
        	slideShow.updateSettings(settings);
        }else{
        	initSlideShow(settings);
        }

		
	}


}
