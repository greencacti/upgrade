package com.vmware.cam.tasks;

import com.vmware.cam.util.SimpleSleep;
import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class RestartService {
    public static void execute(Expect expect, String server, String service) {
        try {
            expect.sendLine("service " + service + " restart && echo success");
            expect.expect(contains("success"));

            expect.sendLine("service " + service + " status");
            expect.expect(contains("running"));
            System.out.println(service + " service is started in " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
