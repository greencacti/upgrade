package com.vmware.cam.tasks;

import com.vmware.cam.util.SimpleSleep;
import net.sf.expectit.Expect;

import java.io.IOException;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class VerifyUpdate {
    public static void execute(Expect expect, String server, String hmsVersion) {
        // wait until the installation is successful
        SimpleSleep.sleep(120);
        try {
            int counter = 15;
            while (counter > 0) {
                try {
                    expect.sendLine("tail -6 /opt/vmware/var/log/vami/updatecli.log");
                    expect.expect(contains("Install Finished"));
                } catch (IOException e) {
                    counter--;
                    continue;
                }

                break;
            }

            if (counter > 0) {
                System.out.println("Installation is successful for " + server);
            } else {
                System.out.println("Installation is failed for " + server);
                throw new RuntimeException();
            }

            // check the installed version
            expect.sendLine("rpm -qa|grep hms");
            expect.expect(contains(hmsVersion));
            System.out.println(hmsVersion + " is installed for " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
