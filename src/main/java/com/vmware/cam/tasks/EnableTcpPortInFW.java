package com.vmware.cam.tasks;

import net.sf.expectit.Expect;
import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/27/15.
 */
public class EnableTcpPortInFW {
    public static void execute(Expect expect, String server, String port) {
        try {
            expect.sendLine("sed -i'' 's/^\\(FW_SERVICES_EXT_TCP=.*\\)\\(\"\\s*$\\)/\\1 "+ port + "\\2/' /etc/sysconfig/SuSEfirewall2 && echo enable-tcp-success$((5400+25))");
            expect.expect(contains("enable-tcp-success5425"));
            System.out.println(port + "(TCP port) is enabled in " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
