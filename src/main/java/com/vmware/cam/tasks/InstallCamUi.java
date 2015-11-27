package com.vmware.cam.tasks;

import com.vmware.cam.util.ScpRcTo;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectIOException;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.sequence;
import static net.sf.expectit.matcher.Matchers.times;

/**
 * Created by baominw on 9/28/15.
 */
public class InstallCamUi {
    public static void execute(Expect expect, ScpRcTo scp, String server, String camUiUrl, String remoteCamUiUrl, String username, String password) {
        try {
            
            scp.scpTo(camUiUrl, remoteCamUiUrl);
            
            //install CAM UI
            expect.sendLine("rpm -U --force " + remoteCamUiUrl);
            expect.expect(contains("Patch completed successfully!"));
            System.out.println("Install CAM UI successfully for " + server);

            // Print PID
            expect.sendLine("cat /var/run/vmware-vcd-cell.pid && echo show-cell-pid-1-success$((5400+25))");
            expect.expect(contains("show-cell-pid-1-success5425"));
            
            // quiesce on vmware-vcd
            expect.sendLine("/opt/vmware/vcloud-director/bin/cell-management-tool -u " + username + " cell --quiesce true -p '" + password + "' && echo cell-quiesce-success$((5400+25))");

            try {
                expect.expect(contains("cell-quiesce-success5425"));
                System.out.println("Quiesce operation succeeded on VCD cell " + server);
            }catch(ExpectIOException e){
                if (e.getMessage().contains("timeout")){
                    System.out.println(e.getMessage());
                    System.out.println("Quiesce operation failed to complete in given time on VCD cell " + server + ". Continue to shutdown vcd-cell");
                }else{
                    throw e;
                }
            }

            // shutdown vmware-vcd
            expect.sendLine("/opt/vmware/vcloud-director/bin/cell-management-tool -u " + username + " cell --shutdown -p '" + password + "' && echo cell-shutdown-success$((5400+25))");
            expect.expect(contains("cell-shutdown-success5425"));
            System.out.println("Shut down VCD cell " + server);

            // start vmware-vcd
            expect.sendLine("service vmware-vcd start");
            expect.expect(times(2, contains("OK")));
            System.out.println("Start vCD cell successfully for " + server);

            // Print PID
            expect.sendLine("cat /var/run/vmware-vcd-cell.pid && echo show-cell-pid-2-success$((5400+25))");
            expect.expect(contains("show-cell-pid-2-success5425"));
      
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
