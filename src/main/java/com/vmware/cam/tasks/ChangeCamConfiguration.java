package com.vmware.cam.tasks;

import com.vmware.cam.util.ScpRcTo;
import net.sf.expectit.Expect;
import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by yiqunc on 10/13/15.
 */
public class ChangeCamConfiguration {
    public static void execute(Expect expect, ScpRcTo scp, String server, String configFileName) {
        try {
            scp.scpRcTo("UpdateCamConfig.sh", "/root/UpdateCamConfig.sh");
            scp.scpRcTo("cam-config.properties.template", "/root/cam-config.properties.template");
            scp.scpTo(configFileName, "/root/upgrade.properties");
            expect.sendLine("chmod 755 /root/UpdateCamConfig.sh && echo success");
            expect.expect(contains("success"));

            expect.sendLine("/root/UpdateCamConfig.sh -f /root/upgrade.properties -t /root/cam-config.properties.template -p cam.config -b /root/");
            expect.expect(contains("success"));
            System.out.println("update new configurations in cam-config.properties.template on " + server);

            expect.sendLine("/root/UpdateCamConfig.sh -f /root/cam-config.properties.template");
            expect.expect(contains("success"));
            System.out.println("update new configurations in cam-config.properties on " + server);
        
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

