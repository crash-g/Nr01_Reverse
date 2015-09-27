package com.example.crash.nr01_reverse;

/**
 * Created by crash on 20/06/2015.
 */
public class Ball {
    int value;

    public Ball(int value) {
        if(0 <= value && value <= 9) {
            this.value = value;
        }
    }

    public void increaseValue() {
        if(value < 9) {
            ++value;
        }
    }
    public void decreaseValue() {
        if(value > 0) {
            --value;
        }
    }
    public int getValue() {
        return value;
    }
}
