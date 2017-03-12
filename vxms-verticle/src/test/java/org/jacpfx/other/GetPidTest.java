package org.jacpfx.other;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by amo on 17.01.17.
 */
public class GetPidTest {

    @Test
    @Ignore
    public void testGetPid() {
        System.out.println("test: "+getPID());
    }

    public static String getPID() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        if (processName != null && processName.length() > 0) {
            try {
                return processName.split("@")[0];
            }
            catch (Exception e) {
                return "0";
            }
        }

        return "0";
    }
}
