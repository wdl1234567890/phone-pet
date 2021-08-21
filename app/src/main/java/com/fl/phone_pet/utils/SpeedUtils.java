package com.fl.phone_pet.utils;

import com.fl.phone_pet.MyService;

public class SpeedUtils {

    public static int maxSpeed = 625;
    public static int climbTimeConst = 40;

    public static long getCurrentSpeedTime(){
        return (long)(climbTimeConst * (maxSpeed/Math.pow(MyService.speed, 2)));
    }

    public static long getCurrentFrequestTime(){
        return 3000 * MyService.frequest;
    }
}
