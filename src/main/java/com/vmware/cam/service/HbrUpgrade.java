package com.vmware.cam.service;

import com.vmware.cam.util.FailedUpgradeList;
import com.vmware.cam.expect.Expecter;
import com.vmware.cam.tasks.*;
import net.sf.expectit.Expect;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Created by baominw on 9/26/15.
 */
public class HbrUpgrade implements Runnable {
    private final Expecter expecter;
    private final CountDownLatch latch;
    private final String hbrServer;
    private final Properties properties;
    private final FailedUpgradeList failedUpgradeList;

    public HbrUpgrade(String hbrServer, String username, String password, Properties properties, FailedUpgradeList failedUpgradeList, CountDownLatch latch) {
        this.expecter = new Expecter(hbrServer, username, password);
        this.hbrServer = hbrServer;
        this.properties = properties;
        this.latch = latch;
        this.failedUpgradeList = failedUpgradeList;
    }

    @Override
    public void run() {
        try {
            // connect to hbr server
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
            ChangeProviderRuntime.execute(expect, hbrServer, localRepositoryAddress);

            // check the update
            String hmsBuildNumber = properties.getProperty("hmsBuildNumber");
            if (hmsBuildNumber == null) {
                System.out.println("missing hmsBuildNumber in upgrade.properties");
                throw new RuntimeException();
            }
            CheckUpdate.execute(expect, hbrServer, hmsBuildNumber);

            // install the update
            InstallUpdate.execute(expect, hbrServer);

            // verify the update
            String hmsVersion = properties.getProperty("hmsVersion");
            if (hmsVersion == null) {
                System.out.println("missing hmsVersion in upgrade.properties");
                throw new RuntimeException();
            }
            VerifyUpdate.execute(expect, hbrServer, hmsVersion);

            // restart HBR Service
            RestartHbr.execute(expect, hbrServer);

            expecter.stop();
            latch.countDown();
        }
        catch(Exception e) {
            failedUpgradeList.addFailedNode(hbrServer);
            latch.countDown();
            throw new RuntimeException(e);
        }
    }
}