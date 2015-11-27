package com.vmware.cam.tasks;

import com.vmware.cam.util.SimpleSleep;
import net.sf.expectit.Expect;

import java.io.IOException;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class CheckCamStatus {
    public static void execute(Expect expect, String server) {
        // wait until the installation is successful
        SimpleSleep.sleep(30);
        try {
            int counter = 2;
            while (counter > 0) {
                try {
                    expect.sendLine("grep \"starting...OK\" /opt/vmware/hms/logs/cam.log");
                    expect.expect(contains("com.vmware.cam.CamService"));
                } catch (IOException e) {
                    counter--;
                    continue;
                }

                break;
            }

            if (counter > 0) {
                System.out.println("CAM is running in " + server);
            } else {
                System.out.println("CAM starts failed in " + server);
                throw new RuntimeException();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
