package com.vmware.cam.tasks;

import com.vmware.cam.util.ScpRcTo;

import net.sf.expectit.Expect;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;

/**
 * Created by baominw on 9/28/15.
 */
public class InstallCamUi {
    public static void execute(Expect expect, ScpRcTo scp, String server, String camUiUrl, String remoteCamUiUrl, String username, String password) {
        try {
            
            scp.scpTo(camUiUrl, remoteCamUiUrl);
            
            //install CAM UI
            expect.sendLine("rpm -U " + remoteCamUiUrl);
            expect.expect(contains("Patch completed successfully!"));
            System.out.println("Install CAM UI successfully for " + server);

            // Print PID
            expect.sendLine("cat /var/run/vmware-vcd-cell.pid");
            
            // quiesce on vmware-vcd
            expect.sendLine("/opt/vmware/vcloud-director/bin/cell-management-tool -u " + username + " cell --quiesce true -p '" + password + "' && echo cell-quiesce-success");
            expect.expect(times(2, contains("cell-quiesce-success")));
            System.out.println("Quiesce operation succeeded on VCD cell " + server);

            // shutdown vmware-vcd
            expect.sendLine("/opt/vmware/vcloud-director/bin/cell-management-tool -u " + username + " cell --shutdown -p '" + password + "' && echo cell-shutdown-success");
            expect.expect(times(2, contains("cell-shutdown-success")));
            System.out.println("Shut down VCD cell " + server);

            // start vmware-vcd
            expect.sendLine("service vmware-vcd start");
            expect.expect(times(2, contains("OK")));
            System.out.println("Start vCD cell successfully for " + server);

            // Print PID
            expect.sendLine("cat /var/run/vmware-vcd-cell.pid");
      
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
