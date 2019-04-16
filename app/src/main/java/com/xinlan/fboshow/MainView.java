package com.xinlan.fboshow;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class MainView extends GLSurfaceView implements GLSurfaceView.Renderer {

    public MainView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context){
        setEGLContextClientVersion(3);
        setEGLConfigChooser(8 , 8 , 8, 8 , 16 , 0 );
        setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glClearColor(1.0f , 1.0f , 0.0f , 1.0f);
        GLES30.glViewport(0 , 0, width , height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }
}//end class
