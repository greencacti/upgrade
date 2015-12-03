package com.vmware.cam.service;

import com.vmware.cam.expect.Expecter;
import com.vmware.cam.tasks.*;
import com.vmware.cam.util.FailedNodeList;
import com.vmware.cam.util.ScpRcTo;
import com.vmware.cam.util.SimpleSleep;

import net.sf.expectit.Expect;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Created by baominw on 9/26/15.
 */
public class HcsUpgrade implements Runnable {
    private final Expecter expecter;
    private final CountDownLatch latch;
    private final String hcsServer;
    private final Properties properties;
    private final FailedNodeList failedNodeList;
    private final ScpRcTo scp;
    private final String configFileName;

    public HcsUpgrade(String hcsServer, String username, String password, Properties properties, FailedNodeList failedNodeList, CountDownLatch latch, String configFileName) {
        this.expecter = new Expecter(hcsServer, username, password);
        this.scp = new ScpRcTo(hcsServer, username, password);
        this.hcsServer = hcsServer;
        this.properties = properties;
        this.latch = latch;
        this.failedNodeList = failedNodeList;
        this.configFileName = configFileName;
    }

    @Override
    public void run() {
        try {
            // connect to hcs server
            String debug = properties.getProperty("debug");
            if (debug == null) {
                System.out.println("missing debug in upgrade.properties");
                throw new RuntimeException();
            }
            boolean isDebugEnabled = Boolean.parseBoolean(debug);
            expecter.start(isDebugEnabled);
            Expect expect = expecter.getExpect();

            // change the provider-runtime.xml
            String localRepositoryAddress = properties.getProperty("localRepositoryAddress");
            if (localRepositoryAddress == null) {
                System.out.println("missing localRepositoryAddress in upgrade.properties");
                throw new RuntimeException();
            }
            ChangeProviderRuntime.execute(expect, hcsServer, localRepositoryAddress);

            // check the update
            String hmsBuildNumber = properties.getProperty("hmsBuildNumber");
            if (hmsBuildNumber == null) {
                System.out.println("missing hmsBuildNumber in upgrade.properties");
                throw new RuntimeException();
            }
            CheckUpdate.execute(expect, hcsServer, hmsBuildNumber);

            // install the update
            InstallUpdate.execute(expect, hcsServer);

            // verify the update
            String hmsVersion = properties.getProperty("hmsVersion");
            if (hmsVersion == null) {
                System.out.println("missing hmsVersion in upgrade.properties");
                throw new RuntimeException();
            }
            VerifyUpdate.execute(expect, hcsServer, hmsVersion);

            // change HCS Configuration
            ChangeHcsConfiguration.execute(expect, hcsServer);
            
            // activitate CAM Service
            ActivitateService.execute(expect, hcsServer, "hcs");

            // reboot HCS appliance
            RebootAppliance.execute(expect, hcsServer);
            expecter.stop();
            SimpleSleep.sleep(30);

            // reconnect to hcs server
            ReconnectToServer.execute(expecter, hcsServer, isDebugEnabled);
            expect = expecter.getExpect();

            expecter.stop();
            latch.countDown();
        } catch (Throwable e) {
            failedNodeList.addFailedNode(hcsServer);
            e.printStackTrace();
            latch.countDown();
        }
    }
}
