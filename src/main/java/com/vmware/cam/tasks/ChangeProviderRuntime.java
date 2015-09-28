package com.vmware.cam.tasks;

import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.anyString;

/**
 * Created by baominw on 9/26/15.
 */
public class ChangeProviderRuntime {
    public static void execute(Expect expect, String server, String localRepositoryAddress) {
        try {
            expect.sendLine("echo '" +
                    "<service>\n" +
                    "  <properties>\n" +
                    "    <property name=\"localRepositoryAddress\" value=\"" + localRepositoryAddress + "\" />\n" +
                    "    <property name=\"localRepositoryPasswordFormat\" value=\"base64\" />\n" +
                    "  </properties>\n" +
                    "</service>'" +
                    "> /opt/vmware/var/lib/vami/update/provider/provider-runtime.xml");
            expect.expect(anyString());
            System.out.println("update provider-runtime.xml successfully for " + server);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
