/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.qualcomm.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.qualcomm.vuforia.samples.VuforiaSamples.R;


// This activity starts activities which demonstrate the Vuforia features
public class ActivityLauncher extends Activity
{
    
    private String mActivities[] = { "Start Game!" };
    
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activities_list_vs);
    }
    
    public void button_func(View view)
    {
    	Intent intent = new Intent(getBaseContext(), AboutScreen.class);
		intent.putExtra("ABOUT_TEXT_TITLE", mActivities[0]);
		
		intent.putExtra("ACTIVITY_TO_LAUNCH", "app.ImageTargets.ImageTargets");
		intent.putExtra("ABOUT_TEXT", "ImageTargets/IT_about.html");
		  
		startActivity(intent);
    }
    
}
