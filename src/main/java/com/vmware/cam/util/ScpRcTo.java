package com.vmware.cam.util;

import com.jcraft.jsch.*;

import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author yiqunc
 */

public class ScpRcTo {

    private final String server;
    private final String username;
    private final String password;

    private Session session = null;
    //private Channel channel = null;

    public ScpRcTo(String server, String username, String password) {
        this.server = server;
        this.username = username;
        this.password = password;
    }

    public void start() {
        try {
            JSch jSch = new JSch();
            session = jSch.getSession(username, server);
            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            //channel = session.openChannel("exec");

            System.out.println("Login to " + server + " successfully");

        } catch (JSchException jschException) {
            if (jschException.getCause() instanceof SocketException) {
                System.out.println(server + " is unreachable");
            } else if (jschException.getMessage().equals("Auth fail")) {
                System.out.println("incorrect username or password");
            }

            throw new RuntimeException(jschException);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            //channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
        }
    }

    // This logic is copied from
    // http://www.jcraft.com/jsch/examples/ScpTo.java.html
    // modification:
    // 1) remove ptimestamp flag which sets file timestamp
    // 2) throw runtime exception instead of System.exit
    // 3) add console output
    public void scpTo(String srcFile, String destFile) {

        FileInputStream fis = null;

        try {

            // exec 'scp -t destFile' remotely
            String command = "scp -t " + destFile;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                throw new RuntimeException("[SCP] Failed to upload local file "
                        + srcFile + " to remote file " + destFile
                        + " on server " + server);
            }

            File _srcFile = new File(srcFile);

            // send "C0644 filesize filename", where filename should not include
            // '/'
            long filesize = _srcFile.length();
            command = "C0644 " + filesize + " ";
            if (srcFile.lastIndexOf('/') > 0) {
                command += srcFile.substring(srcFile.lastIndexOf('/') + 1);
            } else {
                command += srcFile;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                throw new RuntimeException("[SCP] Failed to upload local file "
                        + srcFile + " to remote file " + destFile
                        + " on server " + server);
            }

            // send a content of srcFile
            fis = new FileInputStream(srcFile);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0)
                    break;
                out.write(buf, 0, len);
                out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                throw new RuntimeException("[SCP] Failed to upload local file "
                        + srcFile + " to remote file " + destFile
                        + " on server " + server);
            }
            out.close();
            channel.disconnect();

            System.out.println("Succeeded to upload local file " + srcFile
                    + " to remote file " + destFile + " on server " + server);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (fis != null)
                    fis.close();
            } catch (Exception ee) {
            }
            throw new RuntimeException(e);
        }
    }
    
    public void scpRcTo(String srcFile, String destFile) {
        URL resource = getClass().getClassLoader().getResource(srcFile);
        
        InputStream fis = getClass().getClassLoader().getResourceAsStream(srcFile);

        try {

            // exec 'scp -t destFile' remotely
            String command = "scp -t " + destFile;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                throw new RuntimeException("[SCP] Failed to upload local file "
                        + srcFile + " to remote file " + destFile
                        + " on server " + server);
            }

            File _srcFile = Paths.get(resource.toURI()).toFile();;

            // send "C0644 filesize filename", where filename should not include
            // '/'
            long filesize = _srcFile.length();
            command = "C0644 " + filesize + " ";
            if (srcFile.lastIndexOf('/') > 0) {
                command += srcFile.substring(srcFile.lastIndexOf('/') + 1);
            } else {
                command += srcFile;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                throw new RuntimeException("[SCP] Failed to upload local file "
                        + srcFile + " to remote file " + destFile
                        + " on server " + server);
            }

            // send a content of srcFile
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0)
                    break;
                out.write(buf, 0, len);
                out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                throw new RuntimeException("[SCP] Failed to upload local file "
                        + srcFile + " to remote file " + destFile
                        + " on server " + server);
            }
            out.close();
            channel.disconnect();
            SimpleSleep.sleep(1);

            System.out.println("Succeeded to upload local file " + srcFile
                    + " to remote file " + destFile + " on server " + server);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (fis != null)
                    fis.close();
            } catch (Exception ee) {
            }
            throw new RuntimeException(e);
        }
    }

    // This logic is copied from
    // http://www.jcraft.com/jsch/examples/ScpTo.java.html
    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0)
            return b;
        if (b == -1)
            return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    public static void main(String[] args) {
        ScpRcTo scp = new ScpRcTo("10.110.173.97", "root", "vmware11");
        scp.start();
        scp.scpRcTo("UpdateCamConfig.sh", "/root/UpdateCamConfig.sh");
        scp.scpRcTo("cam-config.properties.template", "/root/cam-config.properties.template");
        scp.scpTo("/Users/yiqunc/Workspaces/cam/cam_deploy/upgrade/src/main/resources/upgrade.properties", "/root/upgrade.properties");
        scp.stop();
    }
}