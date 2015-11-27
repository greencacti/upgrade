package com.vmware.cam.tasks;

import net.sf.expectit.Expect;
import static net.sf.expectit.matcher.Matchers.contains;

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
                    "vc-gateway-expiration-minutes=5'  >> /opt/vmware/hms/conf/hcs-config.properties.rpmsave && echo update-rpmsave-success$((5400+25))");
            expect.expect(contains("update-rpmsave-success5425"));

            expect.sendLine("mv /opt/vmware/hms/conf/hcs-config.properties /opt/vmware/hms/conf/hcs-config.properties.new && echo rename-config-success$((5400+25))");
            expect.expect(contains("rename-config-success5425"));
            
            expect.sendLine("mv /opt/vmware/hms/conf/hcs-config.properties.rpmsave /opt/vmware/hms/conf/hcs-config.properties && echo update-config-success$((5400+25))");
            expect.expect(contains("update-config-success5425"));
            System.out.println("append new configurations into hcs-config.properties in " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
