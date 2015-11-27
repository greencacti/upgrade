package com.vmware.cam.tasks;

import net.sf.expectit.Expect;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * Created by baominw on 9/28/15.
 */
public class RemoveFileOrDir {
    public static void execute(Expect expect, String server, String filePath) {
        try {
            // install CAM UI
            expect.sendLine("rm -rf " + filePath + " && echo remove-file-success$((5400+25))");
            expect.expect(contains("remove-file-success5425"));
            System.out.println("remove " + filePath + " in " + server);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
