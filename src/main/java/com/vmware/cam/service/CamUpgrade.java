package com.vmware.cam.service;

import com.vmware.cam.expect.Expecter;
import com.vmware.cam.tasks.*;
import com.vmware.cam.util.FailedNodeList;
import com.vmware.cam.util.ScpRcTo;
import net.sf.expectit.Expect;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Created by baominw on 9/26/15.
 */
public class CamUpgrade implements Runnable {
    private final Expecter expecter;
    private final CountDownLatch latch;
    private final String camServer;
    private final Properties properties;
    private final FailedNodeList failedNodeList;
    private final ScpRcTo scp;
    private final String configFileName;

    public CamUpgrade(String camServer, String username, String password, Properties properties, FailedNodeList failedNodeList, CountDownLatch latch, String configFileName) {
        this.expecter = new Expecter(camServer, username, password);
        this.scp = new ScpRcTo(camServer, username, password);
        this.camServer = camServer;
        this.properties = properties;
        this.latch = latch;
        this.failedNodeList = failedNodeList;
        this.configFileName = configFileName;
    }

    @Override
    public void run() {
        try {
            // connect to cam server
            String debug = properties.getProperty("debug");
            if (debug == null) {
                System.out.println("missing debug in upgrade.properties");
                throw new RuntimeException();
            }
            boolean isDebugEnabled = Boolean.parseBoolean(debug);
            expecter.start(isDebugEnabled);
            Expect expect = expecter.getExpect();

            // change CAM configuration
            scp.start();
            ChangeCamConfiguration.execute(expect, scp, camServer, configFileName);
            scp.stop();

            // Remove cam.log
            RemoveFileOrDir.execute(expect, camServer, "/opt/vmware/hms/logs/cam.log");

            // activitate CAM Service
            ActivitateService.execute(expect, camServer, "cam");

            // restart CAM Service
            RestartService.execute(expect, camServer, "cam");

            // check CAM Service
            CheckCamStatus.execute(expect, camServer);

            expecter.stop();
            latch.countDown();
        } catch (Throwable e) {
            failedNodeList.addFailedNode(camServer);
            e.printStackTrace();
            latch.countDown();
        }
    }
}
