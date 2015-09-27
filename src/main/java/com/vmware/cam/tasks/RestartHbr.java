package com.vmware.cam.tasks;

import com.vmware.cam.util.SimpleSleep;
import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class RestartHbr {
    public static void execute(Expect expect, String hbrServer) {
        try {
            expect.sendLine("service hbrsrv restart");
            SimpleSleep.sleep(10);

            expect.sendLine("service hbrsrv status");
            expect.expect(contains("running"));
            System.out.println("\nHBR Service is started in " + hbrServer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
