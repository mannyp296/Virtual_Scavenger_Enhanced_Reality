package com.qualcomm.vuforia.samples.VuforiaSamples;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets.ImageTargetRenderer;
import com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets.ImageTargets;

public class PuzzleActivityMulti extends Activity {
	private MediaPlayer mpSuccess;
	private MediaPlayer mpPoison;
	private MediaPlayer mpBeep;
	private MyCount counter;
	@Override
	public void onBackPressed() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_puzzle_activity_multi);
		/*check to see if the user must repeat the level*/
    	mpSuccess = MediaPlayer.create(getApplicationContext(), R.raw.success);
    	mpPoison = MediaPlayer.create(getApplicationContext(), R.raw.mario3); 
    	mpBeep = MediaPlayer.create(getApplicationContext(), R.raw.beep);
		if (ImageTargets.PASSED==1)
			ImageTargets.QUESTION = getRandomNum(); 
		ImageTargetRenderer.resetNums();
		ImageTargets.NEW_ROUND=true;
		final String[] tokens = ImageTargets.MULTI[ImageTargets.QUESTION].split(";");
		TextView tv = (TextView)findViewById(R.id.mult_question_text);
		tv.setText(tokens[0]);
		((Button)findViewById(R.id.Button1)).setText(tokens[1]);
		((Button)findViewById(R.id.Button2)).setText(tokens[2]);
		((Button)findViewById(R.id.Button3)).setText(tokens[3]);
		((Button)findViewById(R.id.Button4)).setText(tokens[4]);
		
		/* Set the timer*/
		TextView mTextField = (TextView)findViewById(R.id.countdown_text);
		counter = new MyCount(20000, 1000, mTextField);
        counter.start();
		
		/*check if answer is correct*/
		((Button)findViewById(R.id.Button1)).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if(tokens[5].equals("1")){
                	 passed();
                 }else{
                	 failed();
                 }
             }
         });
		((Button)findViewById(R.id.Button2)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(tokens[5].equals("2")){
                	passed();
                }else{
                	failed();
                 }
            }
        });
		((Button)findViewById(R.id.Button3)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(tokens[5].equals("3")){
                	passed();
                }else{
                	failed();
                }
            }
        });
		((Button)findViewById(R.id.Button4)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(tokens[5].equals("4")){
                	passed();
                }else{
                	failed();
                 }
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.puzzle_activity_multi, menu);
		return true;
	}
	
	private void passed(){
		mpSuccess.start();
		counter.cancel();
		 ImageTargets.MULTI_COUNT[ImageTargets.QUESTION]=1;
        	ImageTargets.LEVEL++;
        	Toast.makeText(getBaseContext(), "That is correct! You are now on level "+ImageTargets.LEVEL, Toast.LENGTH_LONG).show();
     	ImageTargets.PASSED=1;
        	Intent intent = new Intent();
        	setResult(RESULT_OK, intent);
        	finish();
	}

	private void failed(){
 	   mpPoison.start();
		counter.cancel();
     	Toast.makeText(getBaseContext(), "That is incorrect. You are on level "+ImageTargets.LEVEL+" still", Toast.LENGTH_LONG).show();
     	ImageTargets.HEALTH=(int)ImageTargets.HEALTH/2;
     	ImageTargets.PASSED=0;
     	Intent intent = new Intent();
     	setResult(RESULT_OK, intent);
     	finish();
	}
	  private int getRandomNum(){
		  int rn=(int)(Math.random() * ImageTargets.NUM_MULTI);
		  while (ImageTargets.MULTI_COUNT[rn]!=0)
			  rn=(int)(Math.random() * ImageTargets.NUM_MULTI);
		  return rn;
	    }
	  
	  public class MyCount extends CountDownTimer{
		  
		  private TextView textView1;
		  
	        public MyCount(long millisInFuture, long countDownInterval, TextView tv1 ) {
	            super(millisInFuture, countDownInterval);
	            textView1 = tv1;
	            }

	        @Override
	        public void onFinish() {
	            // TODO Auto-generated method stub

	            textView1.setText("Time's Up");
	            failed();
	        }

	        @Override
	        public void onTick(long millisUntilFinished) {
	            // TODO Auto-generated method stub

	            textView1.setText("" + ((int)(millisUntilFinished/1000)));
	            if(((int)(millisUntilFinished/1000))<=10){
	            	mpBeep.start(); //set text to red
	            	textView1.setTextColor(Color.RED);
	            }
	        }
	    }
	  
	  
}
