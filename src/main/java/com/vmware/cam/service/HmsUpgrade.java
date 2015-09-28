package com.vmware.cam.service;

import com.vmware.cam.expect.Expecter;
import com.vmware.cam.tasks.*;
import com.vmware.cam.util.FailedNodeList;
import net.sf.expectit.Expect;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Created by baominw on 9/26/15.
 */
public class HmsUpgrade implements Runnable {
    private final Expecter expecter;
    private final CountDownLatch latch;
    private final String hmsServer;
    private final Properties properties;
    private final FailedNodeList failedNodeList;

    public HmsUpgrade(String hmsServer, String username, String password, Properties properties, FailedNodeList failedNodeList, CountDownLatch latch) {
        this.expecter = new Expecter(hmsServer, username, password);
        this.hmsServer = hmsServer;
        this.properties = properties;
        this.latch = latch;
        this.failedNodeList = failedNodeList;
    }

    @Override
    public void run() {
        try {
            // connect to hms server
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
            ChangeProviderRuntime.execute(expect, hmsServer, localRepositoryAddress);

            // check the update
            String hmsBuildNumber = properties.getProperty("hmsBuildNumber");
            if (hmsBuildNumber == null) {
                System.out.println("missing hmsBuildNumber in upgrade.properties");
                throw new RuntimeException();
            }
            CheckUpdate.execute(expect, hmsServer, hmsBuildNumber);

            // install the update
            InstallUpdate.execute(expect, hmsServer);

            // verify the update
            String hmsVersion = properties.getProperty("hmsVersion");
            if (hmsVersion == null) {
                System.out.println("missing hmsVersion in upgrade.properties");
                throw new RuntimeException();
            }
            VerifyUpdate.execute(expect, hmsServer, hmsVersion);

            // restart HMS Service
            RestartHms.execute(expect, hmsServer);

            expecter.stop();
            latch.countDown();
        } catch (Throwable e) {
            failedNodeList.addFailedNode(hmsServer);
            e.printStackTrace();
            latch.countDown();
        }
    }
}
