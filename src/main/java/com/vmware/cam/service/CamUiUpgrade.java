package com.vmware.cam.service;

import com.vmware.cam.expect.Expecter;
import com.vmware.cam.tasks.InstallCamUi;
import com.vmware.cam.util.FailedNodeList;
import com.vmware.cam.util.ScpRcTo;

import net.sf.expectit.Expect;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Created by baominw on 9/26/15.
 */
public class CamUiUpgrade implements Runnable {
    private final Expecter expecter;
    private final CountDownLatch latch;
    private final String vcdcell;
    private final Properties properties;
    private final FailedNodeList failedNodeList;
    private final ScpRcTo scp;
    private final String username;
    private final String password;

    public CamUiUpgrade(String vcdcell, String username, String password, Properties properties, FailedNodeList failedNodeList, CountDownLatch latch) {
        this.expecter = new Expecter(vcdcell, username, password);
        this.vcdcell = vcdcell;
        this.properties = properties;
        this.latch = latch;
        this.failedNodeList = failedNodeList;
        this.scp = new ScpRcTo(vcdcell, username, password);
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        try {
            // connect to vcd cell server
            String debug = properties.getProperty("debug");
            if (debug == null) {
                System.out.println("missing debug in upgrade.properties");
                throw new RuntimeException();
            }
            boolean isDebugEnabled = Boolean.parseBoolean(debug);
            expecter.start(isDebugEnabled);
            Expect expect = expecter.getExpect();

            // install CAM UI rpm
            String camUiUrl = properties.getProperty("camUiUrl");
            if (camUiUrl == null) {
                System.out.println("missing camUiUrl in upgrade.properties");
                throw new RuntimeException();
            }
            
            // remote location for UI package on vcd cell
            String remoteCamUiUrl = properties.getProperty("remoteCamUiUrl");
            if (remoteCamUiUrl == null) {
                System.out.println("missing remoteCamUiUrl in upgrade.properties");
                throw new RuntimeException();
            }

            scp.start();
            InstallCamUi.execute(expect, scp, vcdcell, camUiUrl, remoteCamUiUrl, username, password);

            scp.stop();
            expecter.stop();
            latch.countDown();
        } catch (Throwable e) {
            failedNodeList.addFailedNode(vcdcell);
            e.printStackTrace();
            latch.countDown();
        }
    }
}
