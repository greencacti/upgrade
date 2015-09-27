package com.vmware.cam.tasks;

import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class CheckUpdate {
    public static void execute(Expect expect, String hbrServer, String hmsBuildNumber) {
        try {
            expect.sendLine("/opt/vmware/bin/vamicli update --check");
            expect.expect(contains(hmsBuildNumber));
            System.out.println("\nCheck update successfully for " + hbrServer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
