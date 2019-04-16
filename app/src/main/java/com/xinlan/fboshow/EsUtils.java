package com.xinlan.fboshow;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLUtils.texImage2D;

public final class EsUtils {
    private static final String TAG = "EsUtils";

    public static long framePerSecond = 0;
    public static long lastTime = 0;

    public static final int NO_TEXTURE_ID = -1;

    public static void debugFps() {
        framePerSecond++;
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime >= 1000) {
            lastTime = curTime;
            Log.d(TAG, "fps = " + framePerSecond);
            //System.out.println("fps = " +framePerSecond);
            framePerSecond = 0;
        }
    }

    public static FloatBuffer allocateBuf(float array[]) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * Float.BYTES)
                .order(ByteOrder.nativeOrder());
        FloatBuffer buf = bb.asFloatBuffer();
        buf.put(array);
        buf.position(0);
        return buf;
    }

    public static ByteBuffer allocateBuf(byte array[]) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * Byte.BYTES)
                .order(ByteOrder.nativeOrder());
        bb.put(array);
        bb.position(0);
        return bb;
    }

    public static float[] convertColor(float r, float g, float b, int a) {
        float[] colors = new float[4];
        colors[0] = clamp(0.0f, 1.0f, r / 255);
        colors[1] = clamp(0.0f, 1.0f, g / 255);
        colors[2] = clamp(0.0f, 1.0f, b / 255);
        colors[3] = clamp(0.0f, 1.0f, a / 255);
        return colors;
    }

    public static void convertColor(float r, float g, float b, float a, float[] colors) {
        colors[0] = clamp(0.0f, 1.0f, r / 255);
        colors[1] = clamp(0.0f, 1.0f, g / 255);
        colors[2] = clamp(0.0f, 1.0f, b / 255);
        colors[3] = clamp(0.0f, 1.0f, a / 255);
    }

    public static float clamp(float min, float max, float v) {
        if (v <= min)
            return min;
        if (v >= max)
            return max;
        return v;
    }

    public static void debugPrintMat(float[] m) {
        System.out.println(m[0] + " " + m[1] + " " + m[2] + " " + m[3]);
        System.out.println(m[4] + " " + m[5] + " " + m[6] + " " + m[7]);
        System.out.println(m[8] + " " + m[9] + " " + m[10] + " " + m[11]);
        System.out.println(m[12] + " " + m[13] + " " + m[14] + " " + m[15]);
    }


    /**
     * 载入立方体纹理资源
     *
     * @param context
     * @param cubeResources
     * @return
     */
    public static int loadCubeMap(Context context, int[] cubeResources) {
        if (context == null || cubeResources == null || cubeResources.length < 6)
            return -1;

        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL texture object !");
            return -1;
        }
        final BitmapFactory.Options option = new BitmapFactory.Options();
        option.inScaled = false;
        final int CUBE_MAP_SIZE = 6;
        final Bitmap[] cubeBits = new Bitmap[CUBE_MAP_SIZE];
        for (int i = 0; i < CUBE_MAP_SIZE; i++) {
            cubeBits[i] = BitmapFactory.decodeResource(context.getResources(), cubeResources[i], option);

            if (cubeBits[i] == null) {
                Log.e(TAG, "Resource ID " + cubeResources[i] + " could not be decoded.");

                glDeleteTextures(1, textureObjectIds, 0);
                return -1;
            }
        }//end for i

        glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, textureObjectIds[0]);

        GLES30.glTexParameterf(GLES30.GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_REPEAT);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);

        texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBits[0], 0);//左
        texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBits[1], 0);//右
        texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBits[2], 0);//下
        texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBits[3], 0);//上
        texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBits[4], 0);//前
        texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBits[5], 0);//后

        glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, 0);
        // free bitmap
        for (Bitmap bit : cubeBits) {
            bit.recycle();
        }//end for each

        return textureObjectIds[0];
    }

    public static int loadTexture(Context context, int resourceId) {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceId, options);

        if (bitmap == null) {
            glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        // Set filtering: a default must be set, or the texture will be
        // black.
        GLES30.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        // Load the bitmap into the bound texture.
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        glGenerateMipmap(GL_TEXTURE_2D);

        bitmap.recycle();
        // Unbind from the texture.
        glBindTexture(GL_TEXTURE_2D, 0);
        return textureObjectIds[0];
    }

    public static void deleteTexture(final int textureId) {
        if (textureId < 0)
            return;

        glDeleteTextures(1, new int[]{textureId}, 0);
    }

    public static int loadTexture(final Bitmap bit, final int textureId, final boolean recycle) {
        if (bit == null || bit.isRecycled())
            return NO_TEXTURE_ID;

        int[] textureIds = new int[]{textureId};

        if (textureId == NO_TEXTURE_ID) {
            glGenTextures(1, textureIds, 0);
            glBindTexture(GL_TEXTURE_2D, textureIds[0]);

            GLES30.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GLES30.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            GLES30.glTexParameterf(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_REPEAT);
            GLES30.glTexParameterf(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);

            texImage2D(GL_TEXTURE_2D, 0, bit, 0);
        } else {
            glBindTexture(GL_TEXTURE_2D, textureId);
            GLUtils.texSubImage2D(GL_TEXTURE_2D, 0, 0, 0, bit);
            textureIds[0] = textureId;
        }

        if (recycle) {
            bit.recycle();
        }

        return textureIds[0];
    }

    public static int buildShaderProgram(Context context, int vertexShaderCodeId, int fragShaderCodeId) {
        return buildShaderProgram(
                readTextFileFromRaw(context, vertexShaderCodeId),
                readTextFileFromRaw(context, fragShaderCodeId));
    }

    /**
     * 创建shader program
     *
     * @param vertexShaderCode
     * @param fragShaderCode
     * @return
     */
    public static int buildShaderProgram(String vertexShaderCode, String fragShaderCode) {
        int program = GLES30.glCreateProgram();

        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragShaderCode);

        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragShader);
        GLES30.glLinkProgram(program);

        final int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Linking of program failed.");
            Log.e(TAG, "Results of linking program:\n" + GLES30.glGetProgramInfoLog(program));
            glDeleteProgram(program);
            return -1;
        }

        return program;
    }

    /**
     * 导入 顶点着色器 或者 片段着色器源码
     *
     * @param type
     * @param shaderCode
     * @return
     */
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES30.glCreateShader(type);
        if (shader == 0) {
            Log.e(TAG, "create shader error!");
        }

        glShaderSource(shader, shaderCode);
        glCompileShader(shader);

        final int[] compileStatus = new int[1];
        glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Results of compiling source:" + GLES30.glGetShaderInfoLog(shader));
            glDeleteShader(shader);
            return -1;
        }

        return shader;
    }

    /**
     * 从资源文件中读出文本文件
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static String readTextFileFromRaw(Context context, int resourceId) {
        StringBuilder body = new StringBuilder();

        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open resource: " + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found: " + resourceId, nfe);
        }
        return body.toString();
    }
}
