package com.monstrous.tut3d;


public class Settings {
    static public float eyeHeight = 1.5f;   // meters

    static public float walkSpeed = 5f;    // m/s
    static public float runFactor = 3f;    // m/s
    static public float turnSpeed = 120f;   // degrees/s
    static public float jumpForce = 0.5f;


    static public boolean invertLook = false;
    static public boolean freeLook = true;
    static public float headBobDuration = 0.6f; // s
    static public float headBobHeight = 0.04f;  // m
    static public float degreesPerPixel = 0.1f; // mouse sensitivity

    static public float gravity = -9.8f; // meters / s^2

    static public final int shadowMapSize = 4096;

    static public float ballMass = 0.2f;
    static public float ballForce = 100f;

    static public float playerMass = 1.0f;
    static public float playerLinearDamping = 0.05f;
    static public float playerAngularDamping = 0.5f;

    static public final String GLTF_FILE = "models/step12.gltf";
}
