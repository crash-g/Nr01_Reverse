package com.example.crash.nr01_reverse;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Created by crash on 20/06/2015.
 */
public class Renderer implements Runnable {
    public static final String TAG = "Renderer";

    private static Thread renderingThread;

    private static Context context;
    private static Resources res;

    private static boolean isRendering, reDraw;
    private final SurfaceHolder mHolder;
    private static Canvas canvas;
    private static Paint paint;
    private final static Bitmap[] numbers = new Bitmap[10];
    private final static Bitmap[] buttons = new Bitmap[6];

    private final static Vector<Grid> levelGrids = new Vector<>();
    private static int activeGrid;

    public Renderer(Context context, SurfaceHolder holder, Resources res) {
        mHolder = holder;
        Renderer.context = context;
        Renderer.res = res;
        isRendering = true;
        reDraw = true;

        renderingThread = new Thread(this);
    }
    public void start() {
        renderingThread.start();
    }

    private void init() {
        int id = res.getIdentifier("raw/zero",null,context.getPackageName());
        numbers[0]= BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/uno", null, context.getPackageName());
        numbers[1] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/due", null, context.getPackageName());
        numbers[2] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/tre", null, context.getPackageName());
        numbers[3] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/quattro", null, context.getPackageName());
        numbers[4] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/cinque", null, context.getPackageName());
        numbers[5] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/sei", null, context.getPackageName());
        numbers[6] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/sette", null, context.getPackageName());
        numbers[7] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/otto", null, context.getPackageName());
        numbers[8] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/nove", null, context.getPackageName());
        numbers[9] = BitmapFactory.decodeResource(res,id);

        for(int i=0; i<10; i++) {
            if(numbers[i] == null) {
                Log.e(TAG,"Cannot load bitmap "+i);
            }
        }

        id = res.getIdentifier("raw/reverse",null,context.getPackageName());
        buttons[0] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/classic",null,context.getPackageName());
        buttons[1] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/undo",null,context.getPackageName());
        buttons[2] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/export",null,context.getPackageName());
        buttons[3] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/up",null,context.getPackageName());
        buttons[4] = BitmapFactory.decodeResource(res,id);
        id = res.getIdentifier("raw/down",null,context.getPackageName());
        buttons[5] = BitmapFactory.decodeResource(res,id);

        try {
            id = res.getIdentifier("raw/levels",null,context.getPackageName());
            InputStream ins = res.openRawResource(id);
            BufferedReader br = new BufferedReader(new InputStreamReader(ins));

            int[] v = new int [40];
            int count = 0;
            String line = br.readLine();
            while(line != null) {
                line = line.replaceAll("\\s+","");
                for(int i=0; i<40; i++) {
                    int value = Character.getNumericValue(line.charAt(i));
                    v[i] = value;
                }
                levelGrids.add(new Grid(v,numbers,buttons,count));
                ++count;
                line = br.readLine();
            }
            br.close();
            levelGrids.get(levelGrids.size()-1).isLast();
            activeGrid = 0;
        }
        catch(IOException e) {
            Log.e(TAG,"Cannot load levels");
        }

        paint = new Paint();
        paint.setAntiAlias(true);
    }
    @Override
    public void run() {
        init();

        while(isRendering) {
            if (reDraw) {
                reDraw = false;
                draw();
            }
        }
    }
    private void draw() {
        try {
            canvas = mHolder.lockCanvas();
            synchronized (mHolder) {
                canvas.drawColor(Color.BLACK);
                levelGrids.get(activeGrid).draw(canvas,paint);
            }
        } finally {
            if (canvas != null) {
                mHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void requestExitAndWait() {
        isRendering = false;
        try {
            renderingThread.join();
        } catch (InterruptedException e) {
            Log.d(TAG, "Could not stop renderer");
        }
    }
    public static void requestRender() {
        reDraw = true;
    }

    public static void previousGrid() {
        if(activeGrid > 0) {
            --activeGrid;
            requestRender();
        }
    }
    public static void nextGrid() {
        if(activeGrid < levelGrids.size()-1) {
            ++activeGrid;
            requestRender();
        }
    }

    public static Grid getActiveGrid() {
        return levelGrids.get(activeGrid);
    }
}
