package com.vmware.cam.tasks;

import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;

/**
 * Created by baominw on 9/28/15.
 */
public class InstallCamUi {
    public static void execute(Expect expect, String server, String camUiUrl) {
        try {
            // install CAM UI
            expect.sendLine("rpm -U " + camUiUrl);
            expect.expect(contains("Patch completed successfully!"));
            System.out.println("Install CAM UI successfully for " + server);

            // restart vmware-vcd
            expect.sendLine("service vmware-vcd restart");
            expect.expect(times(4, contains("OK")));
            System.out.println("Restart vCD cell successfully for " + server);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
