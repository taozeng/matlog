package com.pluscubed.logcat.helper;

import android.text.TextUtils;

import com.pluscubed.logcat.util.ArrayUtil;
import com.pluscubed.logcat.util.UtilLogger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Helper functions for running processes.
 *
 * @author nolan
 */
public class RuntimeHelper {

    private static UtilLogger log = new UtilLogger(RuntimeHelper.class);
    /**
     * Exec the arguments, using root if necessary.
     *
     * @param args
     */
    public static Process exec(List<String> args) throws IOException {
        Process process;
        // since JellyBean, sudo is required to read other apps' logs
        if (VersionHelper.getVersionSdkIntCompat() >= VersionHelper.VERSION_JELLYBEAN
                && !SuperUserHelper.isFailedToObtainRoot()) {
            process = Runtime.getRuntime().exec("su");

            PrintStream outputStream = null;
            try {
                outputStream = new PrintStream(new BufferedOutputStream(process.getOutputStream(), 8192));
                outputStream.println(TextUtils.join(" ", args));
                outputStream.flush();
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
        else
        {
            process = Runtime.getRuntime().exec(ArrayUtil.toArray(args, String.class));
        }
        log.i("exec command: %s", TextUtils.join(" ", args) + ", " + process);
        return process;
    }

    public static void destroy(Process process) {
        log.d("stop command: %s", "" + process);
        // if we're in JellyBean, then we need to kill the process as root, which requires all this
        // extra UnixProcess logic
        if (VersionHelper.getVersionSdkIntCompat() >= VersionHelper.VERSION_JELLYBEAN
                && !SuperUserHelper.isFailedToObtainRoot()) {
            SuperUserHelper.destroy(process);
        } else {
            process.destroy();
        }
    }

}