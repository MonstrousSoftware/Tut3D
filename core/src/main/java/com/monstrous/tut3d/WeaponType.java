package com.monstrous.tut3d;


// types of weapon and their handling characteristics
public enum WeaponType {

   BALL (0.2f),
   GUN (0.5f);

   public final float repeatRate;            // seconds

   WeaponType(float repeatRate ){
       this.repeatRate = repeatRate;
   }
}
