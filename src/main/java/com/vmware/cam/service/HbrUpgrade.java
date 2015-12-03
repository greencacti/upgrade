package com.vmware.cam.service;

import com.vmware.cam.expect.Expecter;
import com.vmware.cam.tasks.*;
import com.vmware.cam.util.FailedNodeList;
import com.vmware.cam.util.SimpleSleep;
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
    private final FailedNodeList failedNodeList;

    public HbrUpgrade(String hbrServer, String username, String password, Properties properties, FailedNodeList failedNodeList, CountDownLatch latch) {
        this.expecter = new Expecter(hbrServer, username, password);
        this.hbrServer = hbrServer;
        this.properties = properties;
        this.latch = latch;
        this.failedNodeList = failedNodeList;
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

            // reboot hbr appliance
            RebootAppliance.execute(expect, hbrServer);
            expecter.stop();
            SimpleSleep.sleep(30);

            // reconnect to hbr server
            ReconnectToServer.execute(expecter, hbrServer, isDebugEnabled);
            expect = expecter.getExpect();

            // enable 2144 and 9998 (TCP Port) in FW
            EnableTcpPortInFW.execute(expect, hbrServer, "2144");
            EnableTcpPortInFW.execute(expect, hbrServer, "9998");

            // reload firewall configuration
            ReloadFW.execute(expect, hbrServer);

            // restart HBR Service
            RestartService.execute(expect, hbrServer, "hbrsrv");

            expecter.stop();
            latch.countDown();
        } catch (Throwable e) {
            failedNodeList.addFailedNode(hbrServer);
            e.printStackTrace();
            latch.countDown();
        }
    }
}
