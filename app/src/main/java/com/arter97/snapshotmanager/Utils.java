package com.arter97.snapshotmanager;

import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by arter97 on 7/9/17.
 *
 * Various helper functions.
 */

public class Utils {
    public static boolean isRunning(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
    public static void safeDestroy(Process process) {
        try {
            process.destroy();
        } catch (Exception e) {
            // Safely destroying, do nothing
        }
    }

    public static String sanitize(Context context, String orig) {
        char[] tmp = orig.toCharArray();
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] == '?' || tmp[i] < 32 || tmp[i] == 127)
                tmp[i] = '\0';
            else switch (tmp[i]) {
                case '\\':
                case '/':
                case '|':
                case '*':
                case '<':
                case '>':
                case ':':
                    tmp[i] = '_';
                    break;
                case '"':
                    tmp[i] = '\'';
                    break;
            }
        }

        String newstr = new String(tmp).replace(" ", "").replace("\0", "");
        if (!orig.equals(newstr))
            Log.d(context.getString(R.string.app_name), "Sanitized \"" + orig + "\" to \"" + newstr + "\"");

        return newstr;
    }

    public static String[] listSnapshots(Context context) {
        ArrayList<String> snapshots = new ArrayList<>();

        // Execute btrfs
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"su", "-c", "ls /data/btrfs-snapshot/"};

        Process proc = null;
        try {
            proc = rt.exec(commands);
        } catch (Exception e) {
            Log.e(context.getString(R.string.app_name), "Error while executing command!", e);
            Utils.safeDestroy(proc);
            return new String[] { "" };
        }

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));
        String line;

        try {
            while ((line = stdInput.readLine()) != null) {
                Log.i(context.getString(R.string.app_name), "Found snapshot : " + line);
                snapshots.add(line);
            }
        } catch (Exception e) {
            Log.e(context.getString(R.string.app_name), "Error while executing command!", e);
            Utils.safeDestroy(proc);
            return new String[] { "" };
        }

        Utils.safeDestroy(proc);

        // Sort
        Collections.sort(snapshots.subList(0, snapshots.size()));

        return snapshots.toArray(new String[0]);
    }
}
