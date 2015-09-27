package com.vmware.cam;

import com.vmware.cam.util.FailedUpgradeList;
import com.vmware.cam.service.HbrUpgrade;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by baominw on 9/26/15.
 */
public class Main {
    private static enum Component {
        HBR
    }

    private static final String COMMENT_PREFIX = "#";

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 5) {
            System.out.println("missing parameters");
            usage();
            return;
        }

        Component component = null;
        if (args[0].equals("hbr")) {
            component = Component.HBR;
        } else {
            System.out.println("component name is not correct");
            usage();
            return;
        }

        String configFileName = "";
        String hbrListFileName = "";
        String failedListFileName = "";
        String username = "";
        String password = "";

        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                configFileName = arg.substring("--config=".length());
            } else if (arg.startsWith("--hbrlist=")) {
                hbrListFileName = arg.substring("--hbrlist=".length());
            } else if (arg.startsWith("--user=")) {
                username = arg.substring("--user=".length());
            } else if (arg.startsWith("--password=")) {
                password = arg.substring("--password=".length());
            } else if (arg.startsWith("--failedList=")) {
                failedListFileName = arg.substring("--failedList=".length());
            }
        }

        if (configFileName.equals("")) {
            usage();
            return;
        }
        if (hbrListFileName.equals("")) {
            usage();
            return;
        }
        if (username.equals("")) {
            usage();
            return;
        }
        if (password.equals("")) {
            usage();
            return;
        }
        if(failedListFileName.equals("")) {
            usage();
            return;
        }

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(configFileName));
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("upgrade.properties is not found");
            usage();
            return;
        } catch (IOException e) {
            usage();
            return;
        }

        FailedUpgradeList failedUpgradeList = FailedUpgradeList.getInstance(failedListFileName);
        failedUpgradeList.init();

        if (component == Component.HBR) {
            String[] hbrServerList = readListFile(hbrListFileName);
            int maxThroughput = hbrServerList.length;
            if (maxThroughput == 0) {
                System.out.println("hbr.list is empty");
                usage();
                return;
            }

            CountDownLatch latch = new CountDownLatch(maxThroughput);

            ExecutorService executorService = Executors.newFixedThreadPool(maxThroughput);
            for (String hbrServer : hbrServerList) {
                executorService.submit(new HbrUpgrade(hbrServer, username, password, properties, failedUpgradeList, latch));
            }

            latch.await();
            executorService.shutdown();
        }

        failedUpgradeList.close();
    }

    private static void usage() {
        System.out.println("Usage: upgrade hbr --config=configfile --hbrlist=hbr.list --failedList=failed.list --user=username --password=password");
    }

    private static String[] readListFile(String fileName) {
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
            if (is == null) {
                throw new RuntimeException("could not find file:" + fileName);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line = null;
            List<String> result = new LinkedList<String>();
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(COMMENT_PREFIX) || line.equals("")) {
                    continue;
                }

                result.add(line);
            }

            return result.toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }
}
