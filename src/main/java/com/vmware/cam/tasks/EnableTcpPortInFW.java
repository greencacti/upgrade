package com.vmware.cam.tasks;

import com.vmware.cam.util.SimpleSleep;

import net.sf.expectit.Expect;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;

/**
 * Created by baominw on 9/27/15.
 */
public class EnableTcpPortInFW {
    public static void execute(Expect expect, String server, String port) {
        try {
            expect.sendLine("sed -i'' 's/^\\(FW_SERVICES_EXT_TCP=.*\\)\\(\"\\s*$\\)/\\1 "+ port + "\\2/' /etc/sysconfig/SuSEfirewall2 && echo enable-tcp-success");
            expect.expect(times(2, contains("enable-tcp-success")));
            System.out.println(port + "(TCP port) is enabled in " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
