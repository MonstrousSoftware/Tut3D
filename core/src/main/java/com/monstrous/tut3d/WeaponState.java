package com.monstrous.tut3d;


//  which weapon is active and status of weapon
public class WeaponState {
    public boolean haveGun;
    public WeaponType currentWeaponType;
    public float fireTimer;   // timer between shots, <= 0 means ready to fire
    public boolean firing;


    public WeaponState() {
        reset();
    }

    public void reset(){
        haveGun = false;
        currentWeaponType = WeaponType.BALL;
        fireTimer = 0;
    }

    public void switchWeapon() {
        if(currentWeaponType == WeaponType.BALL && haveGun)
            currentWeaponType = WeaponType.GUN;
        else
            currentWeaponType = WeaponType.BALL;
    }

    public void update(float deltaTime) {
        fireTimer -= deltaTime;
    }

    public boolean isWeaponReady() {
        if(fireTimer > 0)           // prevent spamming the trigger
            return false;
        fireTimer = currentWeaponType.repeatRate;    // in seconds: fire rate limiter
        return true;
    }
}
