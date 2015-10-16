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
    private final String server;
    private final String username;
    private final String password;

    private Session session = null;
    private Channel channel = null;
    private Expect expect = null;

    public Expecter(String server, String username, String password) {
        this.server = server;
        this.username = username;
        this.password = password;
    }

    public void start(boolean isDebugEnabled) {
        try {
            JSch jSch = new JSch();
            session = jSch.getSession(username, server);
            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("shell");
            channel.connect();

            System.out.println("Login to " + server + " successfully");

            ExpectBuilder expectBuilder = new ExpectBuilder()
                    .withOutput(channel.getOutputStream())
                    .withInputs(channel.getInputStream(), channel.getExtInputStream())
                    .withTimeout(300, TimeUnit.SECONDS)
                    .withInputFilters(removeColors())
                    .withExceptionOnFailure();

            if (isDebugEnabled) {
                expectBuilder.withEchoOutput(System.out)
                        .withEchoInput(System.err);
            }

            expect = expectBuilder.build();
            expect.expect(anyString());
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
