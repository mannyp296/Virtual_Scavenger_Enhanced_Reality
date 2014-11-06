/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.ImageTargetResult;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.VirtualButton;
import com.qualcomm.vuforia.VirtualButtonResult;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeObject;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LineShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleApplication3DModel;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;
import com.qualcomm.vuforia.samples.SampleApplication.utils.flatObject;
import com.qualcomm.vuforia.samples.VuforiaSamples.R;


// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer
{
	
    private static final String LOGTAG = "ImageTargetRenderer";
    
    private SampleApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;
    
    private static int nums[]={0,0,0,0,0};
    private static int rn1 = getRandomNum();
    private static int rn2 = getRandomNum();
    private static int rn3 = getRandomNum();
    private static int rn4 = getRandomNum();
    private static int rn5 = getRandomNum();
    private Vector<Texture> mTextures;

    private int shaderProgramID;
    
    private int vertexHandle;
    
    private int normalHandle;
    
    private int textureCoordHandle;
    
    private int mvpMatrixHandle;
    
    private int texSampler2DHandle;
    
    private CubeObject mTeapot;
    private flatObject exploded;
    
    private int lineOpacityHandle = 0;
    private int lineColorHandle = 0;
    private int mvpMatrixButtonsHandle = 0;
    
    // OpenGL ES 2.0 specific (Virtual Buttons):
    private int vbShaderProgramID = 0;
    private int vbVertexHandle = 0;
    
    private float kBuildingScale = 12.0f;
    private SampleApplication3DModel mBuildingsModel;
    
    private Renderer mRenderer;
    private MediaPlayer mpBox;
    private MediaPlayer mpPuzzle;
    private MediaPlayer mpPoison;
    private MediaPlayer mpGameover;
    private MediaPlayer mpExplosion;
    private MediaPlayer mpBomb;

    boolean mIsActive = false;
    
    private static final float OBJECT_SCALE_FLOAT = 3.0f;
    
    public int cubeState[]= {0,0,0,0,0};
    public int clicked[]= {0,0,0,0,0};
    
    
    
    public ImageTargetRenderer(ImageTargets activity,
        SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content
        renderFrame();
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initRendering();
    	mpBox = MediaPlayer.create(mActivity.getApplicationContext(), R.raw.mario1); 
    	mpPuzzle = MediaPlayer.create(mActivity.getApplicationContext(), R.raw.mario2); 
    	mpPoison = MediaPlayer.create(mActivity.getApplicationContext(), R.raw.mario3); 
    	mpGameover = MediaPlayer.create(mActivity.getApplicationContext(), R.raw.gameover);
    	mpExplosion = MediaPlayer.create(mActivity.getApplicationContext(), R.raw.explosion);
    	mpBomb = MediaPlayer.create(mActivity.getApplicationContext(), R.raw.bomb);


        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Function for initializing the renderer.
    private void initRendering()
    {
        mTeapot = new CubeObject();
        exploded = new flatObject();
        mRenderer = Renderer.getInstance();
        
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
            : 1.0f);
        
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            CubeShaders.CUBE_MESH_VERTEX_SHADER,
            CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");
        
        try
        {
            mBuildingsModel = new SampleApplication3DModel();
            mBuildingsModel.loadModel(mActivity.getResources().getAssets(),
                "ImageTargets/Buildings.txt");
        } catch (IOException e)
        {
            Log.e(LOGTAG, "Unable to load buildings");
        }
        
        // Hide the Loading Dialog
        mActivity.loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        
     // OpenGL setup for Virtual Buttons
        vbShaderProgramID = SampleUtils.createProgramFromShaderSrc(
            LineShaders.LINE_VERTEX_SHADER, LineShaders.LINE_FRAGMENT_SHADER);
        
        mvpMatrixButtonsHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
            "modelViewProjectionMatrix");
        vbVertexHandle = GLES20.glGetAttribLocation(vbShaderProgramID,
            "vertexPosition");
        lineOpacityHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
            "opacity");
        lineColorHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
            "color");
    }
    
    
    // The render function.
    private void renderFrame()
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera
            
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            
            TrackableResult trackableResult = state.getTrackableResult(0);

         // The image target specific result:
            assert (trackableResult.getType() == ImageTargetResult
                .getClassType());
            ImageTargetResult imageTargetResult = (ImageTargetResult) trackableResult;
            
            Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            
            // Set transformations:
            float[] modelViewProjection = new float[16];
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            
            
            int ind = trackable.getName().equalsIgnoreCase("stones") ? 0
                    : 4;
            ind = trackable.getName().equalsIgnoreCase("tarmac") ? 1
                    : ind;
            ind = trackable.getName().equalsIgnoreCase("acid") ? 2
                      : ind;
            ind = trackable.getName().equalsIgnoreCase("chips") ? 3
                      : ind;
            ind = trackable.getName().equalsIgnoreCase("clay") ? 4
                    : ind;
            int textureIndex=1;
            if(ImageTargets.NEW_ROUND){
            if (cubeState[ind]==1)
            	textureIndex=6;
            if (cubeState[ind]==2)
            	textureIndex=7;
            if (cubeState[ind]==4){
            	textureIndex=4;
            	if(clicked[ind]==1)
            		textureIndex=8;
            }
            if (cubeState[ind]==5)
            	textureIndex=5;
            }else
            	textureIndex=1;

            
            float vbVertices[] = new float[imageTargetResult
               .getNumVirtualButtons() * 24];
            short vbCounter = 0;
           
           // Iterate through this target's virtual buttons:
           for (int i = 0; i < imageTargetResult.getNumVirtualButtons(); ++i)
           {
               VirtualButtonResult buttonResult = imageTargetResult
                   .getVirtualButtonResult(i);
               VirtualButton button = buttonResult.getVirtualButton();
               
               int buttonIndex = 0;
               // Run through button name array to find button index
               for (int j = 0; j < ImageTargets.NUM_BUTTONS; ++j)
               {
                   if (button.getName().compareTo(
                       mActivity.virtualButtonColors[j]) == 0)
                   {
                       buttonIndex = j;
                       break;
                   }
               }
               
               // If the button is pressed, than use this texture:
               if (buttonResult.isPressed())
               {
                   
                   //change cube image
                   int indx = trackable.getName().equalsIgnoreCase("stones") ? 0
                           : 4;
                   indx = trackable.getName().equalsIgnoreCase("tarmac") ? 1
                           : indx;
                   indx = trackable.getName().equalsIgnoreCase("acid") ? 2
                             : indx;
                   indx = trackable.getName().equalsIgnoreCase("chips") ? 3
                             : indx;
                   indx = trackable.getName().equalsIgnoreCase("clay") ? 4
                           : indx;
                   if(ImageTargets.NEW_ROUND){
                	   if(indx==rn1){
                	   //resetNums();
                	   mpPuzzle.start();
                	   textureIndex=1;
                	   ImageTargets.NEW_ROUND=false;
                	   for (int k=0;k<5;k++){
                		   cubeState[k]= 0;
                		   clicked[k]=0;
                	   }
                	   mActivity.showQuestion();
                   }
                   if(indx==rn2){
                	   mpBox.start();
                	   textureIndex = 7;
                	   cubeState[indx] = 2;
                	   if(clicked[indx]==0){
                		   ImageTargets.AID_COUNT++;
                		   clicked[indx]=1;
                	   }
                	   //mActivity.updateAid();
                   }
                   if(indx==rn4){
              	    	mpExplosion.start();
                	   textureIndex=4;
                	   cubeState[indx]=4;
                	   if(clicked[indx]==0){
                		   ImageTargets.HEALTH=ImageTargets.HEALTH-ImageTargets.POISON_DAMAGE;
                		   clicked[indx]=1;
                	   }
                	   mActivity.updateHealth();
                	   if(ImageTargets.HEALTH<=0){
                	    	mpGameover.start();
                		   mActivity.gameOver();
                	   }
                   }
                   if(indx==rn5){
              	    	mpBomb.start();
                	   textureIndex=5;
                	   cubeState[indx]=5;
                	   if(clicked[indx]==0){
                		   ImageTargets.HEALTH=ImageTargets.HEALTH-ImageTargets.POISON_DAMAGE;
                		   clicked[indx]=1;
                	   }
                	   mActivity.updateHealth();
                	   if(ImageTargets.HEALTH<=0){
               	    	mpGameover.start();
               		   mActivity.gameOver();
               	   }
                   }
                   
                   if(indx==rn3){
                	   mpPoison.start();
                	   textureIndex = 6;
                	   cubeState[indx] = 1;
                	   if(clicked[indx]==0){
                		   ImageTargets.HEALTH=ImageTargets.HEALTH-ImageTargets.POISON_DAMAGE;
                		   clicked[indx]=1;
                	   }
                	   mActivity.updateHealth();
                	   if(ImageTargets.HEALTH<=0){
               	    	mpGameover.start();
               		   mActivity.gameOver();
               	   }
                   
                   }
                   }

               }
               
               /*Area vbArea = button.getArea();
               assert (vbArea.getType() == Area.TYPE.RECTANGLE);
               Rectangle vbRectangle[] = new Rectangle[5];
               vbRectangle[0] = new Rectangle(-108.68f, 65f, 109.50f, -65.87f);
               vbRectangle[1] = new Rectangle(-108.68f, 65f, 109.50f, -65.87f);
               vbRectangle[2] = new Rectangle(-108.68f, 65f, 109.50f, -65.87f);
               vbRectangle[3] = new Rectangle(-108.68f, 65f, 109.50f, -65.87f);
               vbRectangle[4] = new Rectangle(-108.68f, 65f, 109.50f, -65.87f);

               // We add the vertices to a common array in order to have one
               // single
               // draw call. This is more efficient than having multiple
               // glDrawArray calls
               vbVertices[vbCounter] = vbRectangle[buttonIndex].getLeftTopX();
               vbVertices[vbCounter + 1] = vbRectangle[buttonIndex]
                   .getLeftTopY();
               vbVertices[vbCounter + 2] = 0.0f;
               vbVertices[vbCounter + 3] = vbRectangle[buttonIndex]
                   .getRightBottomX();
               vbVertices[vbCounter + 4] = vbRectangle[buttonIndex]
                   .getLeftTopY();
               vbVertices[vbCounter + 5] = 0.0f;
               vbVertices[vbCounter + 6] = vbRectangle[buttonIndex]
                   .getRightBottomX();
               vbVertices[vbCounter + 7] = vbRectangle[buttonIndex]
                   .getLeftTopY();
               vbVertices[vbCounter + 8] = 0.0f;
               vbVertices[vbCounter + 9] = vbRectangle[buttonIndex]
                   .getRightBottomX();
               vbVertices[vbCounter + 10] = vbRectangle[buttonIndex]
                   .getRightBottomY();
               vbVertices[vbCounter + 11] = 0.0f;
               vbVertices[vbCounter + 12] = vbRectangle[buttonIndex]
                   .getRightBottomX();
               vbVertices[vbCounter + 13] = vbRectangle[buttonIndex]
                   .getRightBottomY();
               vbVertices[vbCounter + 14] = 0.0f;
               vbVertices[vbCounter + 15] = vbRectangle[buttonIndex]
                   .getLeftTopX();
               vbVertices[vbCounter + 16] = vbRectangle[buttonIndex]
                   .getRightBottomY();
               vbVertices[vbCounter + 17] = 0.0f;
               vbVertices[vbCounter + 18] = vbRectangle[buttonIndex]
                   .getLeftTopX();
               vbVertices[vbCounter + 19] = vbRectangle[buttonIndex]
                   .getRightBottomY();
               vbVertices[vbCounter + 20] = 0.0f;
               vbVertices[vbCounter + 21] = vbRectangle[buttonIndex]
                   .getLeftTopX();
               vbVertices[vbCounter + 22] = vbRectangle[buttonIndex]
                   .getLeftTopY();
               vbVertices[vbCounter + 23] = 0.0f;
               vbCounter += 24;
            */   
           }
           
           // We only render if there is something on the array
           if (vbCounter > 0)
           {
               // Render frame around button
               GLES20.glUseProgram(vbShaderProgramID);
               
               GLES20.glVertexAttribPointer(vbVertexHandle, 3,
                   GLES20.GL_FLOAT, false, 0, fillBuffer(vbVertices));
               
               GLES20.glEnableVertexAttribArray(vbVertexHandle);
               
               GLES20.glUniform1f(lineOpacityHandle, 1.0f);
               GLES20.glUniform3f(lineColorHandle, 1.0f, 1.0f, 1.0f);
               
               GLES20.glUniformMatrix4fv(mvpMatrixButtonsHandle, 1, false,
                   modelViewProjection, 0);
               
               // We multiply by 8 because that's the number of vertices per
               // button
               // The reason is that GL_LINES considers only pairs. So some
               // vertices
               // must be repeated.
               GLES20.glDrawArrays(GLES20.GL_LINES, 0,
                   imageTargetResult.getNumVirtualButtons() * 8);
               
               SampleUtils.checkGLError("VirtualButtons drawButton");
               
               GLES20.glDisableVertexAttribArray(vbVertexHandle);
           }
            
            // deal with the modelview and projection matrices
            
            if (!mActivity.isExtendedTrackingActive())
            {
                Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                    OBJECT_SCALE_FLOAT);
                Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                    OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
            } else
            {
                Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
                Matrix.scaleM(modelViewMatrix, 0, kBuildingScale,
                    kBuildingScale, kBuildingScale);
            }
            
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            
            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);
            
            if (!mActivity.isExtendedTrackingActive())
            {
            	int indx = trackable.getName().equalsIgnoreCase("stones") ? 0
                        : 4;
                  indx = trackable.getName().equalsIgnoreCase("tarmac") ? 1
                        : indx;
                  indx = trackable.getName().equalsIgnoreCase("acid") ? 2
                          : indx;
                  indx = trackable.getName().equalsIgnoreCase("chips") ? 3
                          : indx;
            	
            	if(cubeState[indx]==4&&clicked[indx]==1){
	                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
		                    false, 0, exploded.getVertices());
		                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
		                    false, 0, exploded.getNormals());
		                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
		                    GLES20.GL_FLOAT, false, 0, exploded.getTexCoords());
            	}else{
	                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
	                    false, 0, mTeapot.getVertices());
	                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
	                    false, 0, mTeapot.getNormals());
	                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
	                    GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
            	}
            	
                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);
                
                // activate texture 0, bind it, and pass to shader
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(textureIndex).mTextureID[0]);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                
                // pass the model view matrix to the shader
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjection, 0);
                
                // finally draw the teapot
            	if(cubeState[indx]==4&&clicked[indx]==1){
	                	GLES20.glDrawElements(GLES20.GL_TRIANGLES,
	                    exploded.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
	                    exploded.getIndices());
            	}else{
                	GLES20.glDrawElements(GLES20.GL_TRIANGLES,
    	                    mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
    	                    mTeapot.getIndices());
            	
            	}
                
                // disable the enabled arrays
                GLES20.glDisableVertexAttribArray(vertexHandle);
                GLES20.glDisableVertexAttribArray(normalHandle);
                GLES20.glDisableVertexAttribArray(textureCoordHandle);
            } else
            {
                GLES20.glDisable(GLES20.GL_CULL_FACE);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mBuildingsModel.getVertices());
                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mBuildingsModel.getNormals());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                    GLES20.GL_FLOAT, false, 0, mBuildingsModel.getTexCoords());
                
                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);
                
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(3).mTextureID[0]);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjection, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                    mBuildingsModel.getNumObjectVertex());
                
                SampleUtils.checkGLError("Renderer DrawBuildings");
            }
            
            SampleUtils.checkGLError("Render Frame");
            
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        mRenderer.end();
    }
    
    private Buffer fillBuffer(float[] array)
    {
        // Convert to floats because OpenGL doesnt work on doubles, and manually
        // casting each input value would take too much time.
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length); // each
                                                                     // float
                                                                     // takes 4
                                                                     // bytes
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (float d : array)
            bb.putFloat(d);
        bb.rewind();
        
        return bb;
        
    }
    
    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }
    
    
    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
        
    }
    
    
    
  private static int getRandomNum(){
	  int rn=(int)(Math.random() * 5);
	  while (nums[rn]!=0)
		  rn=(int)(Math.random() * 5);
	  nums[rn]=1;
	  return rn;
    }
  public static void resetNums(){
	  for (int i=0; i<5; i++)
		  nums[i]=0;
	    rn1 = getRandomNum();
	    rn2 = getRandomNum();
	    rn3 = getRandomNum();
	    rn4 = getRandomNum();
	    rn5 = getRandomNum();
  }
  }
    

