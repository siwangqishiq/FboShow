package com.xinlan.fboshow;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class MainView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private Context mCtx;

    public MainView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mCtx = context;

        setEGLContextClientVersion(3);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(this);
    }

    int mSrcTextureId;
    int mProgramId;

    float mImageVertex[] = {
            -1.0f, 1.0f,
            0.0f, 1.0f,
            -1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            -1.0f, 0.0f
    };
    FloatBuffer mVertexBuf;

    float mImageCoord[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };
    FloatBuffer mCoordBuf;
    int mTextureLoc;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgramId = EsUtils.buildShaderProgram(mCtx, R.raw.vertex, R.raw.fragment);
        mSrcTextureId = EsUtils.loadTexture(mCtx, R.drawable.xinyuan);
        mVertexBuf = EsUtils.allocateBuf(mImageVertex);
        mCoordBuf = EsUtils.allocateBuf(mImageCoord);

        mTextureLoc = GLES30.glGetUniformLocation(mProgramId, "uTextureId");

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES30.glUseProgram(mProgramId);
        GLES30.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 2 * 4, mVertexBuf);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 2 * 4, mCoordBuf);
        GLES30.glEnableVertexAttribArray(1);

        GLES30.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mSrcTextureId);
        GLES30.glUniform1ui(mTextureLoc, 0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mImageVertex.length / 2);
    }
}//end class
