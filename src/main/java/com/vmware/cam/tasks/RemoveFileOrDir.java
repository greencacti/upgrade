package com.vmware.cam.tasks;

import com.vmware.cam.util.SimpleSleep;
import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;

/**
 * Created by baominw on 9/28/15.
 */
public class RemoveFileOrDir {
    public static void execute(Expect expect, String server, String filePath) {
        try {
            // install CAM UI
            expect.sendLine("rm -rf " + filePath + " && echo 'success'");
            SimpleSleep.sleep(1);
            expect.expect(contains("success"));
            System.out.println("remove " + filePath + " in " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
