package com.vmware.cam.tasks;

import net.sf.expectit.Expect;

/**
 * Created by baominw on 9/27/15.
 */
public class InstallUpdate {
    public static void execute(Expect expect, String server) {
        try {
            expect.sendLine("/opt/vmware/bin/vamicli update --install latest");
            System.out.println("Installation is ongoing for " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
