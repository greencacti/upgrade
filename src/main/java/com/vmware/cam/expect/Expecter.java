package com.vmware.cam.expect;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.vmware.cam.util.SimpleSleep;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;

import java.net.SocketException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.matcher.Matchers.anyString;

/**
 * Created by baominw on 9/26/15.
 */
public class Expecter {
    private final String hbrServer;
    private final String username;
    private final String password;

    private Session session = null;
    Channel channel = null;
    Expect expect = null;

    public Expecter(String hbrServer, String username, String password) {
        this.hbrServer = hbrServer;
        this.username = username;
        this.password = password;
    }

    public void start(boolean isDebugEnabled) {
        try {
            JSch jSch = new JSch();
            session = jSch.getSession(username, hbrServer);
            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("shell");
            channel.connect();

            System.out.println("Login to " + hbrServer + " successfully");

            ExpectBuilder expectBuilder = new ExpectBuilder()
                    .withOutput(channel.getOutputStream())
                    .withInputs(channel.getInputStream(), channel.getExtInputStream())
                    .withTimeout(60, TimeUnit.SECONDS)
                    .withInputFilters(removeColors())
                    .withExceptionOnFailure();

            if (isDebugEnabled) {
                expectBuilder.withEchoOutput(System.out)
                        .withEchoInput(System.err);
            }

            expect = expectBuilder
                    .build();
            SimpleSleep.sleep(1);
            expect.expect(anyString());
        } catch (JSchException jschException) {
            if (jschException.getCause() instanceof SocketException) {
                System.out.println(hbrServer + " is unreachable");
            } else if (jschException.getMessage().equals("Auth fail")) {
                System.out.println("incorrect username or password");
            }

            jschException.printStackTrace();
            throw new RuntimeException();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public Expect getExpect() {
        return expect;
    }

    public void stop() {
        try {
            expect.close();
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
        }
    }
}
