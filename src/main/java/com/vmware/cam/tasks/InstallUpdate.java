package com.vmware.cam.tasks;

import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.anyString;

/**
 * Created by baominw on 9/27/15.
 */
public class InstallUpdate {
    public static void execute(Expect expect, String hbrServer) {
        try {
            expect.sendLine("/opt/vmware/bin/vamicli update --install latest");
            System.out.println("\nInstallation is ongoing for " + hbrServer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
