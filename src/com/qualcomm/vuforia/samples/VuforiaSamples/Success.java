package com.qualcomm.vuforia.samples.VuforiaSamples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets.ImageTargets;

public class Success extends Activity {

	@Override
	public void onBackPressed() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_success);
		TextView tv = (TextView)findViewById(R.id.time);
		tv.setText("Level: "+String.valueOf(ImageTargets.LEVEL));
		((Button)findViewById(R.id.reset)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(getBaseContext(), ImageTargets.class);
				ImageTargets.LEVEL=1;
				ImageTargets.HEALTH=100;
				ImageTargets.PREVIOUS_HEALTH=100;
				//TextView t = (TextView) findViewById(R.id.health_overlay);
				//t.setText(String.valueOf(ImageTargets.HEALTH));
				ImageTargets.AID_COUNT=0;
				ImageTargets.NEW_ROUND=true;
				for (int i=0; i<ImageTargets.NUM_MULTI;i++)
					ImageTargets.MULTI_COUNT[i]=0;
				ImageTargets.PASSED=1;
				startActivity(intent);
            }
        });
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.success, menu);
		return true;
	}


	
}
