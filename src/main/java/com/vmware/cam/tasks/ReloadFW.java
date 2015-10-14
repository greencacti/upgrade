package com.vmware.cam.tasks;

import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class ReloadFW {
    public static void execute(Expect expect, String server) {
        try {
            expect.sendLine("/etc/init.d/SuSEfirewall2_setup reload");
            expect.expect(contains("done"));
            System.out.println("Reload firewall configuration in " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
