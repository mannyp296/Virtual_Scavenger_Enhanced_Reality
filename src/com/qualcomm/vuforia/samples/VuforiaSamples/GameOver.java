package com.qualcomm.vuforia.samples.VuforiaSamples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets.ImageTargets;

public class GameOver extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_over);
		TextView t1 = (TextView)findViewById(R.id.gameEnder);
		TextView t2 = (TextView)findViewById(R.id.level);

		t1.setText("You have failed us. \nHowever, you made it to level: ");
		t2.setText(String.valueOf(ImageTargets.LEVEL));
		
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
	public void onBackPressed() {
		Toast.makeText(getBaseContext(), "You cannot turn back time. You are dead!",Toast.LENGTH_LONG).show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_over, menu);
		return true;
	}

}
