package com.vmware.cam.tasks;

import com.vmware.cam.util.SimpleSleep;
import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class RebootAppliance {
    public static void execute(Expect expect, String server) {
        try {
            expect.sendLine("reboot");
            expect.expect(contains("The system is going down for reboot NOW"));
            System.out.println(server + " is rebooted");
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
