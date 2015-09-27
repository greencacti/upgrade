package com.vmware.cam.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by baominw on 9/27/15.
 */
public class FailedUpgradeList {
    private static final Object lock = new Object();
    private static volatile FailedUpgradeList instance = null;
    private String fileName = "";
    private BufferedWriter writer = null;

    private FailedUpgradeList(String fileName) {
        this.fileName = fileName;
    }

    public static FailedUpgradeList getInstance(String fileName) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new FailedUpgradeList(fileName);
                }
            }
        }

        return instance;
    }

    public void init() {
        if (writer == null) {
            synchronized (lock) {
                if (writer == null) {
                    try {
                        File file = new File(fileName);
                        if(!file.exists()){
                            file.createNewFile();
                        }
                        writer = new BufferedWriter(new FileWriter(file));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public void addFailedNode(String line) {
        synchronized (lock) {
            try {
                writer.write(line);
                writer.newLine();
                writer.flush();
            } catch (Exception e) {

            }
        }
    }

    public void close() {
        if (writer != null) {
            synchronized (lock) {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Exception e) {

                    }

                }
            }
        }
    }
}
