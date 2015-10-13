package com.vmware.cam.tasks;

import com.vmware.cam.expect.Expecter;
import com.vmware.cam.util.SimpleSleep;
import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class ReconnectToServer {
    public static void execute(Expecter expecter, String server, boolean isDebugEnabled) {
        try {
            while (true) {
                try {
                    expecter.start(isDebugEnabled);
                    System.out.println("Reconnect to " + server + " successfully");
                    break;
                } catch (Exception e) {
                    SimpleSleep.sleep(10);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
