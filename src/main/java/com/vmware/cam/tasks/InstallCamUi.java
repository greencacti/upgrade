package com.vmware.cam.tasks;

import com.vmware.cam.expect.Expecter;
import com.vmware.cam.util.ScpRcTo;
import com.vmware.cam.util.SimpleSleep;

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
            expect.expect(contains("#"));

            // Print PID
            expect.sendLine("cat /var/run/vmware-vcd-cell.pid && echo show-cell-pid-1-success");
            System.out.println(sequence(contains("show-cell-pid-1-success"),contains("show-cell-pid-1-success")));
            expect.expect(times(2, contains("show-cell-pid-1-success")));
            expect.expect(contains("#"));
            
            // quiesce on vmware-vcd
            expect.sendLine("/opt/vmware/vcloud-director/bin/cell-management-tool -u " + username + " cell --quiesce true -p '" + password + "' && echo cell-quiesce-success");

            try {
                expect.expect(times(2, contains("cell-quiesce-success")));
                expect.expect(contains("#"));
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
            expect.sendLine("/opt/vmware/vcloud-director/bin/cell-management-tool -u " + username + " cell --shutdown -p '" + password + "' && echo cell-shutdown-success");
            expect.expect(times(2, contains("cell-shutdown-success")));
            expect.expect(contains("#"));
            System.out.println("Shut down VCD cell " + server);

            // start vmware-vcd
            expect.sendLine("service vmware-vcd start");
            expect.expect(times(2, contains("OK")));
            expect.expect(contains("#"));
            System.out.println("Start vCD cell successfully for " + server);

            // Print PID
            expect.sendLine("cat /var/run/vmware-vcd-cell.pid && echo show-cell-pid-2-success");
            expect.expect(times(2, contains("show-cell-pid-2-success")));
            expect.expect(contains("#"));
      
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
