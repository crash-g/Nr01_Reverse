package com.example.crash.nr01_reverse;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Environment;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Stack;

/**
 * Created by crash on 20/06/2015.
 */
public class Grid {
    private static Rect[] positions = new Rect[40];
    private static Rect[] buttonPositions = new Rect[6];

    private static Bitmap[] numbers;
    private static Bitmap[] buttons;
    private static int squareSize;
    private static int offset;
    private static int buttonOffset;

    private Ball[] grid = new Ball[40];
    private boolean reverse;
    private Stack<Swap> undoStack = new Stack<Swap>();
    private boolean[] buttonVisibility = {false,true,false,true,true,true};
    private int gridNumber;

    private class Swap {
        int i;
        int j;
        boolean reverse;

        Swap(int i, int j, boolean reverse) {
            this.i = i;
            this.j = j;
            this.reverse = reverse;
        }
    }

    public Grid(int[] v, Bitmap[] numbers, Bitmap[] buttons, int gridNumber) {
        if(v.length == 40) {
            for(int i=0; i<v.length; i++) {
                if(v[i] == 1) {
                    grid[i] = new Ball(1);
                }
            }
        }
        Grid.numbers = numbers;
        Grid.buttons = buttons;
        this.gridNumber = gridNumber;
        reverse = true;

        if(gridNumber == 0) {
            buttonVisibility[4] = true;
            buttonVisibility[5] = false;
        }
        else {
            buttonVisibility[4] = true;
            buttonVisibility[5] = true;
        }
    }
    public void isLast() {
        buttonVisibility[4] = false;
        buttonVisibility[5] = true;
    }
    public synchronized static void measure(int width, int height) {
        buttonOffset = width/12;
        offset = Math.min(width/12,(height-buttonOffset)/12);
        squareSize = Math.min((width-2*offset)/5,(height-buttonOffset-2*offset)/8);
        int left,top,right,bottom;
        for(int i=0; i<positions.length; i++) {
            left = offset + (i%5)*squareSize;
            top = buttonOffset + offset + ((int) Math.floor(i/5))*squareSize;
            right = left +squareSize;
            bottom = top + squareSize;

            positions[i] = new Rect(left,top,right,bottom);
        }
        int shortEdgeButton = buttonOffset;
        for(int i=0; i<buttonPositions.length; i++) {
            left = i*2*shortEdgeButton;
            top = 0;
//            top = ((int) Math.floor(i/3))*shortEdgeButton;
            right = left + 2*shortEdgeButton;
            bottom = top + shortEdgeButton;

            buttonPositions[i] = new Rect(left,top,right,bottom);
        }
    }
    public void touch(Point start, Point finish) {
        if(start.y >= buttonOffset) {
            swipe(start,finish);
        }
        else {
            int button = -1;
            for(int i=0; i<buttonPositions.length; i++) {
                if(buttonPositions[i].contains(start.x,start.y)) {
                    button = i;
                    break;
                }
            }
            switch (button) {
                case 0:
                    if(buttonVisibility[0]) {
                        reverse = true;
                        buttonVisibility[0] = false;
                        buttonVisibility[1] = true;
                        Renderer.requestRender();
                    }
                    break;
                case 1:
                    if(buttonVisibility[1]) {
                        reverse = false;
                        buttonVisibility[0] = true;
                        buttonVisibility[1] = false;
                        Renderer.requestRender();
                    }
                    break;
                case 2:
                    if(buttonVisibility[2]) {
                        undo();
                    }
                    break;
                case 3:
                    export();
                    break;
                case 4:
                    if(buttonVisibility[4]) {
                        Renderer.nextGrid();
                    }
                    break;
                case 5:
                    if(buttonVisibility[5]) {
                        Renderer.previousGrid();
                    }
                    break;
                default:
                    break;
            }
        }
    }
    private void swipe(Point start, Point finish) {
        double distance = Math.pow(start.x - finish.x,2)+Math.pow(start.y - finish.y,2);
        if(distance < Math.pow(squareSize/2,2)) {
            return;
        }
        int s = -1;
        for(int i=0; i<positions.length; i++) {
            if(positions[i].contains(start.x,start.y)) {
                s = i;
                break;
            }
        }
        distance = Math.sqrt(distance);
        double orient = (finish.y - start.y)/distance;
        double threshold = Math.sqrt(2)/2;
        if(orient >= threshold) { //down
            if(canSwap(s,s+5,reverse)) {
                swap(s,s+5,reverse);
            }
        }
        else if(orient <= -threshold) {  //up
            if(canSwap(s,s-5,reverse)) {
                swap(s,s-5,reverse);
            }
        }
        else if(finish.x - start.x > 0) {  //right
            if(s%5 != 4) {
                if(canSwap(s,s+1,reverse)) {
                    swap(s,s+1,reverse);
                }
            }
        }
        else {  //left
            if(s%5 != 0) {
                if(canSwap(s,s-1,reverse)) {
                    swap(s,s-1,reverse);
                }
            }
        }
    }
    private boolean canSwap(int i, int j, boolean reverse) {
        if (0 <= i && i < grid.length && grid[i] != null) {
            if (0 <= j && j < grid.length && grid[j] != null) {
                if (0 <= j && j < grid.length) {
                    if (reverse) {
                        if (grid[i].getValue() < 9 && grid[j].getValue() < 9) {
                            return true;
                        }
                    } else {
                        if (grid[i].getValue() > 1 && grid[j].getValue() > 1) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    private void swap(int i, int j, boolean reverse) {
        Ball t = grid[i];
        grid[i] = grid[j];
        grid[j] = t;

        if(reverse) {
            grid[i].increaseValue();
            grid[j].increaseValue();
        }
        else {
            grid[i].decreaseValue();
            grid[j].decreaseValue();
        }
        undoStack.push(new Swap(i,j,reverse));
        if(!buttonVisibility[2]) {
            buttonVisibility[2] = true;
        }

        Renderer.requestRender();
    }
    public void undo() {
        if(!undoStack.isEmpty()) {
            Swap s = undoStack.pop();
            swap(s.i,s.j,!s.reverse);
            undoStack.pop();
            if(undoStack.isEmpty()) {
                buttonVisibility[2] = false;
            }
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        for(int i=0; i<buttonPositions.length; i++) {
            if(buttonVisibility[i]) {
                canvas.drawBitmap(buttons[i],null,buttonPositions[i],paint);
            }
        }
        for(int i=0; i<grid.length; i++) {
            if(grid[i] != null) {
                canvas.drawBitmap(numbers[grid[i].getValue()],null,positions[i],paint);
            }
        }
    }
    private void export() {
        if(isExternalStorageWritable()) {
            try {
                FileWriter fw = new FileWriter(getStorageDir());
                JsonWriter jWriter = new JsonWriter(fw);
                jWriter.setIndent("  ");

                jWriter.beginArray();
                for(int i=0; i<grid.length; i++) {
                    if(grid[i] != null) {
                        jWriter.beginObject();
                        jWriter.name("col").value(i % 5);
                        jWriter.name("row").value((int) Math.floor(i/5));
                        jWriter.name("value").value(grid[i].getValue());
                        jWriter.endObject();
                    }
                }
                jWriter.endArray();
                jWriter.close();
            }
            catch (IOException e) {
                Log.e(Renderer.TAG,"Cannot create grid file"+e.getMessage());
            }
        }
        else {
            Log.e(Renderer.TAG,"External storage is not available");
        }
    }
    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    private File getStorageDir() throws IOException {
        // Get the directory for the user's public pictures directory.
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, "Nr01_level"+gridNumber+".json");
        if (path.mkdirs()) {
            Log.i(Renderer.TAG, "Directory created");
        }
//        file.createNewFile();
        return file;
    }

}
