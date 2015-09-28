package com.vmware.cam;

import com.vmware.cam.service.CamUiUpgrade;
import com.vmware.cam.service.HbrUpgrade;
import com.vmware.cam.service.HmsUpgrade;
import com.vmware.cam.util.FailedNodeList;

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
        HBR,
        HMS,
        CAMUI
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
        } else if (args[0].equals("hms")) {
            component = Component.HMS;
        } else if (args[0].equals("camui")) {
            component = Component.CAMUI;
        } else {
            System.out.println("component name is not correct");
            usage();
            return;
        }

        String configFileName = "";
        String hbrListFileName = "";
        String hmsListFileName = "";
        String vcdcellListFileName = "";
        String failedListFileName = "";
        String username = "";
        String password = "";

        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                configFileName = arg.substring("--config=".length());
            } else if (arg.startsWith("--hbrlist=")) {
                hbrListFileName = arg.substring("--hbrlist=".length());
            } else if (arg.startsWith("--hmslist=")) {
                hmsListFileName = arg.substring("--hmslist=".length());
            } else if (arg.startsWith("--vcdcelllist=")) {
                vcdcellListFileName = arg.substring("--vcdcelllist=".length());
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
        if (username.equals("")) {
            usage();
            return;
        }
        if (password.equals("")) {
            usage();
            return;
        }
        if (failedListFileName.equals("")) {
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
            e.printStackTrace();
            return;
        }

        FailedNodeList failedNodeList = FailedNodeList.getInstance(failedListFileName);
        failedNodeList.init();

        if (component == Component.HBR) {
            if (hbrListFileName.equals("")) {
                usage();
                return;
            }

            String[] hbrServerList = readListFile(hbrListFileName);
            int maxThroughput = hbrServerList.length;
            if (maxThroughput == 0) {
                System.out.println("hbr.list is empty");
                return;
            }

            CountDownLatch latch = new CountDownLatch(maxThroughput);

            ExecutorService executorService = Executors.newFixedThreadPool(maxThroughput);
            for (String hbrServer : hbrServerList) {
                executorService.submit(new HbrUpgrade(hbrServer, username, password, properties, failedNodeList, latch));
            }

            latch.await();
            executorService.shutdown();
        } else if (component == Component.HMS) {
            if (hmsListFileName.equals("")) {
                usage();
                return;
            }

            String[] hmsServerList = readListFile(hmsListFileName);
            int maxThroughput = hmsServerList.length;
            if (maxThroughput == 0) {
                System.out.println("hms.list is empty");
                return;
            }

            CountDownLatch latch = new CountDownLatch(maxThroughput);

            ExecutorService executorService = Executors.newFixedThreadPool(maxThroughput);
            for (String hmsServer : hmsServerList) {
                executorService.submit(new HmsUpgrade(hmsServer, username, password, properties, failedNodeList, latch));
            }

            latch.await();
            executorService.shutdown();
        } else {
            if (vcdcellListFileName.equals("")) {
                usage();
                return;
            }

            String[] vcdcellList = readListFile(vcdcellListFileName);
            int maxThroughput = vcdcellList.length;
            if (maxThroughput == 0) {
                System.out.println("vcdcell.list is empty");
                return;
            }

            CountDownLatch latch = new CountDownLatch(maxThroughput);

            ExecutorService executorService = Executors.newFixedThreadPool(maxThroughput);
            for (String vcdcell : vcdcellList) {
                executorService.submit(new CamUiUpgrade(vcdcell, username, password, properties, failedNodeList, latch));
            }

            latch.await();
            executorService.shutdown();
        }

        failedNodeList.close();
    }

    private static void usage() {
        System.out.println("Usage: upgrade [hbr|hms|camui] --config=configfile [--hbrlist=hbr.list|--hmslist=hms.list|--vcdcelllist=vcdcell.list] --failedList=failed.list --user=username --password=password");
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
