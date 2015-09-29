package com.vmware.cam.tasks;

import com.vmware.cam.util.SimpleSleep;
import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.anyString;

/**
 * Created by baominw on 9/26/15.
 */
public class ChangeHcsConfiguration {
    public static void execute(Expect expect, String server) {
        try {
            expect.sendLine("echo '\nvlsi.client.timeout=30000\n" +
                    "\n" +
                    "workflowEngine.preventRebalance=true\n" +
                    "workflowEngine.preventRebalanceTimeMillis=60000\n" +
                    "workflowEngine.preventRebalanceExecutorCapacity=10\n" +
                    "\n" +
                    "workflowEngine.preventRebalance=true\n" +
                    "workflowEngine.preventRebalanceTimeMillis=60000\n" +
                    "workflowEngine.preventRebalanceExecutorCapacity=10\n" +
                    "\n" +
                    "#in minutes\n" +
                    "replicationGroupRemoteActionWorkflowTimeout=20\n" +
                    "\n" +
                    "# Maximum sleep time between Cloud HMS PrimaryFailbackGroup.status polling\n" +
                    "workflow.remoteFailbackFailover.pollingSleepTimeMillis=500\n" +
                    "\n" +
                    "# Maximum duration for a RemoteFailbackFailoverWorkflow polling step\n" +
                    "workflow.remoteFailbackFailover.pollingMaxStepTimeMillis=5000\n" +
                    "\n" +
                    "# Maximum duration of the total polling time frame (maybe many steps)\n" +
                    "workflow.remoteFailbackFailover.pollingMaxTotalTimeMillis=300000\n" +
                    "\n" +
                    "\n" +
                    "# remote (HMS, VC, HBR, etc.) task wait timeout and polling interval\n" +
                    "remote-task-timeout=600000\n" +
                    "remote-task-polling-interval=10000\n" +
                    "\n" +
                    "service.routing.key=hcs\n" +
                    "\n" +
                    "vc-gateway-expiration-minutes=5'  >> /opt/vmware/hms/conf/hcs-config.properties.rpmsave");
            SimpleSleep.sleep(1);
            expect.expect(anyString());

            expect.sendLine("mv /opt/vmware/hms/conf/hcs-config.properties /opt/vmware/hms/conf/hcs-config.properties.new");
            SimpleSleep.sleep(1);
            expect.expect(anyString());

            expect.sendLine("mv /opt/vmware/hms/conf/hcs-config.properties.rpmsave /opt/vmware/hms/conf/hcs-config.properties");
            SimpleSleep.sleep(1);
            expect.expect(anyString());
            System.out.println("append new configurations into hcs-config.properties in " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
