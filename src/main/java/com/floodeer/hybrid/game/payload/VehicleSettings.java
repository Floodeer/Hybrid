package com.floodeer.hybrid.game.payload;

public class VehicleSettings {

    public double rotationAmount = 30;
    public double movementAmount = 0.5;
    public boolean rotateBody = true;
    public boolean rotateLeftArm = false;
    public boolean rotateRightArm = false;

    public String axis = "x";

    public void loopAxis() {
        switch (axis) {
            case "x":
                axis = "y";
                break;
            case "y":
                axis = "z";
                break;
            case "z":
                axis = "x";
                break;
        }
    }

    public void setRotateLeftArm() {
        rotateBody = false;
        rotateLeftArm = true;
        rotateRightArm = false;
    }

    public void setRotateRightArm() {
        rotateBody = false;
        rotateLeftArm = false;
        rotateRightArm = true;
    }

    public void setRotateBody() {
        rotateBody = true;
        rotateLeftArm = false;
        rotateRightArm = false;
    }
}
