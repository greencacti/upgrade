package com.vmware.cam.tasks;

import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class ActivitateService {
    public static void execute(Expect expect, String server, String service) {
        try {
            expect.sendLine("chkconfig --add " + service);
            expect.expect(contains("5:on"));
            System.out.println(service + " is activitated in " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
