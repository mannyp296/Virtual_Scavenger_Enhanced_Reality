/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.Rectangle;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.VirtualButton;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationException;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleApplicationGLView;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;
import com.qualcomm.vuforia.samples.VuforiaSamples.GameOver;
import com.qualcomm.vuforia.samples.VuforiaSamples.PuzzleActivityMulti;
import com.qualcomm.vuforia.samples.VuforiaSamples.R;
import com.qualcomm.vuforia.samples.VuforiaSamples.Success;
import com.qualcomm.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenu;
import com.qualcomm.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuGroup;
import com.qualcomm.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuInterface;


public class ImageTargets extends Activity implements SampleApplicationControl,
    SampleAppMenuInterface
{
    private static final String LOGTAG = "ImageTargets";
    
    SampleApplicationSession vuforiaAppSession;
    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();
	static public long RUNTIME=0;


    // Our OpenGL view:
    private SampleApplicationGLView mGlView;
    
    // Our renderer:
    private ImageTargetRenderer mRenderer;
    
    private GestureDetector mGestureDetector;
    
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;
    
    private boolean mSwitchDatasetAsap = false;
    private boolean mFlash = false;
    private boolean mContAutofocus = true;
    private boolean mExtendedTracking = false;
    
    private View mFlashOptionView;
    
    private RelativeLayout mUILayout;
    
    private SampleAppMenu mSampleAppMenu;
    
    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    
    private DataSet dataSet = null;
    public static int LEVEL=1;
    public static boolean NEW_ROUND=true;
    public static int HEALTH=100;
    public static int PREVIOUS_HEALTH=100;
    public static int POISON_DAMAGE=15;
    public static int FIRST_AID=40;
    public static int AID_COUNT=0;
   public static final String MULTI[]={"According to the proverb, what is the pot calling the kettle?; Hot; Noisy; Black; Admirable;3",
	   "On June 9, 1790, the first ever U.S. copyright was issued for a book about what?; Spelling; Astronomy; History; Religion;1",
	   "The inscription 'Pass and Stow,' appears on which of these US landmarks?; Statue of Liberty; Liberty Bell; Mount Rushmore; Washington Monument;2",
	   "The 'Pua alohalo' is Hawaii’s official state what?; Bird; Flag; Motto; Flower;4"};
   public static final int NUM_MULTI=4;
   public static int MULTI_COUNT[]={0,0,0,0};
   public static int PASSED=1;
   public static int QUESTION;
   // Virtual Button runtime creation:
    private boolean updateBtns = false;
    public String virtualButtonColors[] = { "a", "b", "c", "d", "e" };
    
    // Enumeration for masking button indices into single integer:
    private static final int BUTTON_1 = 1;
    private static final int BUTTON_2 = 2;
    private static final int BUTTON_3 = 4;
    private static final int BUTTON_4 = 8;
    private static final int BUTTON_5 = 16;
        
    private byte buttonMask = 0;
    static final int NUM_BUTTONS = 5;
    
    boolean mIsDroidDevice = false;
    
    
    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        vuforiaAppSession = new SampleApplicationSession(this);
        startLoadingAnimation();
        mDatasetStrings.add("Virtual_Scavenger.xml");
        mDatasetStrings.add("Tarmac.xml");


        vuforiaAppSession
            .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        mGestureDetector = new GestureDetector(this, new GestureListener());
        
        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();
        
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
            "droid");
        
    }
    
    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
        GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();
        
        
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }
        
        
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                    
                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);
            
            return true;
        }
    }
    
    
    // We want to load specific textures from the APK, which we will later use
    // for rendering.
    
    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk("mario-box-old.gif",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("mario-box-question-mark.gif",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("mario-box-question-mark.gif",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("mario-box-question-mark.gif",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("bomb.png",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("tnt.jpg",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("poison.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("redCross.jpg",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("hole.png",
                getAssets()));
    }
    
    
    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();
        
        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
        
    }
    
    
    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        
        vuforiaAppSession.onConfigurationChanged();
    }
    
    
    // Called when the system is about to start resuming a previous activity.
    @SuppressLint("NewApi")
	@Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();
        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
        
        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }
        
        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }
    
    
    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        
        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Unload texture:
        mTextures.clear();
        mTextures = null;
        
        System.gc();
    }
    
    
    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        mRenderer = new ImageTargetRenderer(this, vuforiaAppSession);
        
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
        
    }
    
    
    private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
            null, false);
        
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
        
    }
    
    
    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager
            .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
            return false;
        
        if (mCurrentDataset == null)
            mCurrentDataset = imageTracker.createDataSet();
        
        if (mCurrentDataset == null)
            return false;
        
        if (!mCurrentDataset.load(
            mDatasetStrings.get(mCurrentDatasetSelectionIndex),
            DataSet.STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;
        
        if (!imageTracker.activateDataSet(mCurrentDataset))
            return false;
        
        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if(mExtendedTracking)
            	trackable.startExtendedTracking();
            
            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                + (String) trackable.getUserData());
        }
        
     // Create the data set:
        dataSet = imageTracker.createDataSet();
        if (dataSet == null)
        {
            Log.d(LOGTAG, "Failed to create a new tracking data.");
            return false;
        }
        
        // Load the data set:
        if (!dataSet.load("VirtualButtons/Wood.xml",
            DataSet.STORAGE_TYPE.STORAGE_APPRESOURCE))
        {
            Log.d(LOGTAG, "Failed to load data set.");
            return false;
        }
        
        // Activate the data set:
        if (!imageTracker.activateDataSet(dataSet))
        {
            Log.d(LOGTAG, "Failed to activate data set.");
            return false;
        }
        
        Log.d(LOGTAG, "Successfully loaded and activated data set.");
        
        return true;
    }
    
    
    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager
            .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
            return false;
        
        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (imageTracker.getActiveDataSet().equals(mCurrentDataset)
                && !imageTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!imageTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }
            
            mCurrentDataset = null;
        }
        
        return result;
    }
    
    
    @Override
    public void onInitARDone(SampleApplicationException exception)
    {
        
        if (exception == null)
        {
            initApplicationAR();
            
            mRenderer.mIsActive = true;
            
            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
            
            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();
            TextView t1 = new TextView(this);
            t1.setId(R.id.health_overlay);
            t1.setText(String.valueOf(HEALTH)); //getString(R.string.menu_health) + 
            t1.setTextColor(Color.WHITE);
            t1.setTextSize(50);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            params.leftMargin = 30;
            params.topMargin = 30;
            mUILayout.addView(t1, params);
            
           /* TextView t2 = new TextView(this);
            t2.setId(R.id.health_packs_overlay);
            t2.setText("");
            t2.setTextColor(Color.BLACK);
            t2.setTextSize(20);
            t2.setClickable(true);
            t2.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(AID_COUNT > 0) {
						HEALTH += 40;
						if(HEALTH > 100) {
							HEALTH = 100;
						}
						--AID_COUNT;
						updateHealth();
						updateAid();
					}
				}
			});
            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params2.addRule(RelativeLayout.BELOW, R.id.health_overlay);
            params2.leftMargin = 30;
            params2.topMargin = 30;
            mUILayout.addView(t2, params2);*/
            
            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);
            
            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }
            
            boolean result = CameraDevice.getInstance().setFocusMode(
                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
            
            if (result)
                mContAutofocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");
            
            mSampleAppMenu = new SampleAppMenu(this, this, "Menu",
                mGlView, mUILayout, null);
            setSampleAppMenuSettings();
            
        } else
        {
            Log.e(LOGTAG, exception.getString());
            finish();
        }
    }
    
    
    @Override
    public void onQCARUpdate(State state)
    {
    	 if (updateBtns)
         {
             // Update runs in the tracking thread therefore it is guaranteed
             // that the tracker is
             // not doing anything at this point. => Reconfiguration is possible.
             
             ImageTracker it = (ImageTracker) (TrackerManager.getInstance()
                 .getTracker(ImageTracker.getClassType()));
             assert (dataSet != null);
             
             // Deactivate the data set prior to reconfiguration:
             it.deactivateDataSet(dataSet);
             
             assert (dataSet.getNumTrackables() > 0);
             Trackable trackable = dataSet.getTrackable(0);
             
             assert (trackable != null);
             assert (trackable.getType() == ImageTracker.getClassType());
             ImageTarget imageTarget = (ImageTarget) (trackable);
             
             if ((buttonMask & BUTTON_1) != 0)
             {
                 Log.d(LOGTAG, "Toggle Button 1");
                 
                 toggleVirtualButton(imageTarget, virtualButtonColors[0],
                		 -108.68f, 65f, 109.50f, -65.87f);
                 
             }
             if ((buttonMask & BUTTON_2) != 0)
             {
                 Log.d(LOGTAG, "Toggle Button 2");
                 
                 toggleVirtualButton(imageTarget, virtualButtonColors[1],
                		 -108.68f, 65f, 109.50f, -65.87f);
             }
             if ((buttonMask & BUTTON_3) != 0)
             {
                 Log.d(LOGTAG, "Toggle Button 3");
                 
                 toggleVirtualButton(imageTarget, virtualButtonColors[2],
                		 -108.68f, 65f, 109.50f, -65.87f);
             }
             if ((buttonMask & BUTTON_4) != 0)
             {
                 Log.d(LOGTAG, "Toggle Button 4");
                 
                 toggleVirtualButton(imageTarget, virtualButtonColors[3],
                		 -108.68f, 65f, 109.50f, -65.87f);
             }
             if ((buttonMask & BUTTON_5) != 0)
             {
                 Log.d(LOGTAG, "Toggle Button 5");
                 
                 toggleVirtualButton(imageTarget, virtualButtonColors[4],
                		 -108.68f, 65f, 109.50f, -65.87f);
             }
             
             // Reactivate the data set:
             it.activateDataSet(dataSet);
             
             buttonMask = 0;
             updateBtns = false;
         }
        if (mSwitchDatasetAsap)
        {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ImageTracker it = (ImageTracker) tm.getTracker(ImageTracker
                .getClassType());
            if (it == null || mCurrentDataset == null
                || it.getActiveDataSet() == null)
            {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }
            
            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }
    
 // Create/destroy a Virtual Button at runtime
    //
    // Note: This will NOT work if the tracker is active!
    boolean toggleVirtualButton(ImageTarget imageTarget, String name,
        float left, float top, float right, float bottom)
    {
        Log.d(LOGTAG, "toggleVirtualButton");
        
        boolean buttonToggleSuccess = false;
        
        VirtualButton virtualButton = imageTarget.getVirtualButton(name);
        if (virtualButton != null)
        {
            Log.d(LOGTAG, "Destroying Virtual Button> " + name);
            buttonToggleSuccess = imageTarget
                .destroyVirtualButton(virtualButton);
        } else
        {
            Log.d(LOGTAG, "Creating Virtual Button> " + name);
            Rectangle vbRectangle = new Rectangle(left, top, right, bottom);
            VirtualButton virtualButton2 = imageTarget.createVirtualButton(
                name, vbRectangle);
            
            if (virtualButton2 != null)
            {
                // This is just a showcase. The values used here a set by
                // default on Virtual Button creation
                virtualButton2.setEnabled(true);
                virtualButton2.setSensitivity(VirtualButton.SENSITIVITY.MEDIUM);
                buttonToggleSuccess = true;
            }
        }
        
        return buttonToggleSuccess;
    }
    
    
    @Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;
        
        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ImageTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                LOGTAG,
                "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }
    
    
    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;
        
        Tracker imageTracker = TrackerManager.getInstance().getTracker(
            ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.start();
        
        return result;
    }
    
    
    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        Tracker imageTracker = TrackerManager.getInstance().getTracker(
            ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.stop();
        
        return result;
    }
    
    
    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ImageTracker.getClassType());
        
        return result;
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        if (mSampleAppMenu != null && mSampleAppMenu.processEvent(event))
            return true;
        
        return mGestureDetector.onTouchEvent(event);
    }
    
    
    boolean isExtendedTrackingActive()
    {
        return mExtendedTracking;
    }
    
    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    final public static int CMD_AUTOFOCUS = 2;
    final public static int CMD_FLASH = 3;
    final public static int CMD_CAMERA_FRONT = 4;
    final public static int CMD_CAMERA_REAR = 5;
    final public static int CMD_USE_HEALTH_PACK = 6;
    final public static int CMD_DATASET_START_INDEX = 7;
    
    
    // This method sets the menu's settings
    public void setSampleAppMenuSettings()
    {
        SampleAppMenuGroup group;
        
        group = mSampleAppMenu.addGroup("", false);
        group.addTextItem(getString(R.string.menu_back), -1);

        	group.addTextItem(getString(R.string.menu_health_pack), CMD_USE_HEALTH_PACK);
        
        
        group = mSampleAppMenu.addGroup("", true);
        group.addSelectionItem(getString(R.string.menu_contAutofocus),
            CMD_AUTOFOCUS, mContAutofocus);
        mFlashOptionView = group.addSelectionItem(
            getString(R.string.menu_flash), CMD_FLASH, false);
        
        CameraInfo ci = new CameraInfo();
        boolean deviceHasFrontCamera = false;
        boolean deviceHasBackCamera = false;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++)
        {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT)
                deviceHasFrontCamera = true;
            else if (ci.facing == CameraInfo.CAMERA_FACING_BACK)
                deviceHasBackCamera = true;
        }
        
        if (deviceHasBackCamera && deviceHasFrontCamera)
        {
            group = mSampleAppMenu.addGroup(getString(R.string.menu_camera),
                true);
            group.addRadioItem(getString(R.string.menu_camera_front),
                CMD_CAMERA_FRONT, false);
            group.addRadioItem(getString(R.string.menu_camera_back),
                CMD_CAMERA_REAR, true);
        }
        
//        group = mSampleAppMenu
//            .addGroup(getString(R.string.menu_datasets), true);
//        mStartDatasetsIndex = CMD_DATASET_START_INDEX;
//        mDatasetsNumber = mDatasetStrings.size();
//        
//        group.addRadioItem("Virtual Scavenger", mStartDatasetsIndex, true);
//        group.addRadioItem("Tarmac", mStartDatasetsIndex + 1, false);
        
        mSampleAppMenu.attachMenu();
    }
    
    
    @SuppressLint("NewApi")
	@Override
    public boolean menuProcess(int command)
    {
        
        boolean result = true;
        
        switch (command)
        {
            case CMD_BACK:
                finish();
                break;
            
            case CMD_FLASH:
                result = CameraDevice.getInstance().setFlashTorchMode(!mFlash);
                
                if (result)
                {
                    mFlash = !mFlash;
                } else
                {
                    showToast(getString(mFlash ? R.string.menu_flash_error_off
                        : R.string.menu_flash_error_on));
                    Log.e(LOGTAG,
                        getString(mFlash ? R.string.menu_flash_error_off
                            : R.string.menu_flash_error_on));
                }
                break;
            
            case CMD_AUTOFOCUS:
                
                if (mContAutofocus)
                {
                    result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
                    
                    if (result)
                    {
                        mContAutofocus = false;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_off));
                        Log.e(LOGTAG,
                            getString(R.string.menu_contAutofocus_error_off));
                    }
                } else
                {
                    result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
                    
                    if (result)
                    {
                        mContAutofocus = true;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_on));
                        Log.e(LOGTAG,
                            getString(R.string.menu_contAutofocus_error_on));
                    }
                }
                
                break;
            
            case CMD_CAMERA_FRONT:
            case CMD_CAMERA_REAR:
                
                // Turn off the flash
                if (mFlashOptionView != null && mFlash)
                {
                    // OnCheckedChangeListener is called upon changing the checked state
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    {
                        ((Switch) mFlashOptionView).setChecked(false);
                    } else
                    {
                        ((CheckBox) mFlashOptionView).setChecked(false);
                    }
                }
                
                doStopTrackers();
                CameraDevice.getInstance().stop();
                CameraDevice.getInstance().deinit();
                try
                {
                    vuforiaAppSession
                        .startAR(command == CMD_CAMERA_FRONT ? CameraDevice.CAMERA.CAMERA_FRONT
                            : CameraDevice.CAMERA.CAMERA_BACK);
                } catch (SampleApplicationException e)
                {
                    showToast(e.getString());
                    Log.e(LOGTAG, e.getString());
                    result = false;
                }
                doStartTrackers();
                break;
            
            case CMD_EXTENDED_TRACKING:
                for (int tIdx = 0; tIdx < mCurrentDataset.getNumTrackables(); tIdx++)
                {
                    Trackable trackable = mCurrentDataset.getTrackable(tIdx);
                    
                    if (!mExtendedTracking)
                    {
                        if (!trackable.startExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                "Failed to start extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                "Successfully started extended tracking target");
                        }
                    } else
                    {
                        if (!trackable.stopExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                "Failed to stop extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                "Successfully started extended tracking target");
                        }
                    }
                }
                
                if (result)
                    mExtendedTracking = !mExtendedTracking;
                
                break;
            case CMD_USE_HEALTH_PACK:
            	if(AID_COUNT == 0)
            	{
            		Toast.makeText(getBaseContext(), "You do not have any health packs", Toast.LENGTH_SHORT).show();
            		break;
            	} else
            	{
            		HEALTH += 40;
            		if(HEALTH > 100)
            			HEALTH = 100;
            		AID_COUNT--;
            		updateHealth();
            		//updateMenu();
            	}
            default:
                break;
        }
        
        return result;
    }
    
    
    public void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    
    public void showQuestion()
    {
    	if (LEVEL<=NUM_MULTI){ //questions in general
    		Intent intent = new Intent(this, PuzzleActivityMulti.class);
    		startActivityForResult(intent, 1);
    	}
    	else{
    		Intent intent = new Intent(this, Success.class);
    		startActivity(intent);
    	}
    	
    }
    
    public void gameOver()
    {
    	Intent intent = new Intent(this, GameOver.class);
    	startActivity(intent);
    }
    
    public void updateHealth() {
    	runOnUiThread(new Runnable() {
    	     @Override
    	     public void run() {
    	    	 long tmp;
    	    	 	int sign;
    	    	 	
    	    	 if(HEALTH<0)
    	    		 HEALTH=0;
    	    	TextView t = (TextView) findViewById(R.id.health_overlay);
    	 		tmp=PREVIOUS_HEALTH-HEALTH;
    	 		if (tmp<0)
    	 			sign=1;
    	 		else
    	 			sign=0;
    	 		ReduceHealth counter = new ReduceHealth(tmp*50,50,t,sign);
    	 		counter.start();
    	 		PREVIOUS_HEALTH=HEALTH;
    	    }
    	});
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	  if (requestCode == 1) {

	     if(resultCode == RESULT_OK){      
	         updateHealth();       
	     }
	     if (resultCode == RESULT_CANCELED) {    
	         //Write your code if there's no result
	     }
	  }
	}//onActivityResult
    
    
    public void updateAid() {
    	runOnUiThread(new Runnable() {
   	     @Override
   	     public void run() {

   	    	TextView t = (TextView) findViewById(R.id.health_packs_overlay);
   	    	if(AID_COUNT > 0) {
   	    		t.setText(getString(R.string.menu_health_pack));
   	    	} else {
   	    		t.setText("");
   	    	}
   	    }
   	});
    }
    
    public class ReduceHealth extends CountDownTimer{
		  
		  private TextView textView1;
		  private int mySign;
		  
	        public ReduceHealth(long millisInFuture, long countDownInterval, TextView tv1, int sign ) {
	            super(millisInFuture, countDownInterval);
	            textView1 = tv1;
	            mySign = sign;
	            }

	        @Override
	        public void onFinish() {
	            // TODO Auto-generated method stub

	            textView1.setText(String.valueOf(HEALTH));
	        }

	        @Override
	        public void onTick(long millisUntilFinished) {
	            // TODO Auto-generated method stub
	        	if(mySign==1){
	        		textView1.setText("" + (HEALTH-((int)(millisUntilFinished/50))));
	        	}
	        	else{
	        		textView1.setText("" + (HEALTH+((int)(millisUntilFinished/50))));
	        	}


	        }
	    }
    
	  
	  
}
