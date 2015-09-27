package com.vmware.cam.util;

/**
 * Created by baominw on 9/26/15.
 */
public class SimpleSleep {
    public static void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (Exception e) {

        }
    }
}
