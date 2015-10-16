package com.vmware.cam.tasks;

import com.vmware.cam.util.ScpRcTo;
import net.sf.expectit.Expect;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;

/**
 * Created by yiqunc on 10/13/15.
 */
public class ChangeCamConfiguration {
    public static void execute(Expect expect, ScpRcTo scp, String server, String configFileName) {
        try {
            scp.scpRcTo("UpdateCamConfig.sh", "/tmp/UpdateCamConfig.sh");
            scp.scpRcTo("cam-config.properties.template", "/tmp/cam-config.properties.template");
            scp.scpTo(configFileName, "/tmp/upgrade.properties");
            expect.sendLine("chmod 755 /tmp/UpdateCamConfig.sh && echo success");
            expect.expect(times(2, contains("success")));

            expect.sendLine("/tmp/UpdateCamConfig.sh -f /tmp/upgrade.properties -t /tmp/cam-config.properties.template -p cam.config -b /tmp/");
            expect.expect(contains("success"));
            System.out.println("update new configurations in cam-config.properties.template on " + server);

            expect.sendLine("/tmp/UpdateCamConfig.sh -f /tmp/cam-config.properties.template");
            expect.expect(contains("success"));
            System.out.println("update new configurations in cam-config.properties on " + server);
        
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

