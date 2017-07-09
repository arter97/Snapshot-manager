package com.arter97.snapshotmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class RestoreSnapshot extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_snapshot);

        final ListView listView = (ListView) findViewById(R.id.restore_listview);
        final ArrayList<String> listItems = new ArrayList<>(Arrays.asList(Utils.listSnapshots(this)));
        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                final String snapshot = listItems.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(RestoreSnapshot.this);
                builder.setTitle(getString(R.string.fab_history))
                        .setMessage(getString(R.string.fab_history_msg, snapshot))
                        .setCancelable(true)
                        .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Execute btrfs to list subvolumes
                                // TODO : Clean-up previous snapshot
                                Runtime rt = Runtime.getRuntime();
                                String[] commands = {"su", "-c", "btrfs subvolume list /data"};
                                Process proc = null;
                                try {
                                    proc = rt.exec(commands);
                                } catch (Exception e) {
                                    Log.e(getString(R.string.app_name), "Error while executing btrfs!", e);
                                    Utils.safeDestroy(proc);
                                    return;
                                }

                                BufferedReader stdInput = new BufferedReader(new
                                        InputStreamReader(proc.getInputStream()));
                                String line;
                                String[] arr;
                                String subvolid;

                                // read the output from the command
                                try {
                                    while ((line = stdInput.readLine()) != null) {
                                        arr = StringUtils.split(line);
                                        if (arr.length >= 9 && !arr[8].isEmpty() && arr[8].equals("btrfs-snapshot/" + snapshot)) {
                                            Log.i(getString(R.string.app_name), "Got subvolid : " + (subvolid = arr[1]));

                                            // Execute btrfs to set default subvolume
                                            String[] sub_commands = {"su", "-c", "btrfs subvolume set-default " + subvolid + " /data"};
                                            Process sub_proc = null;
                                            try {
                                                sub_proc = rt.exec(sub_commands);
                                                sub_proc.waitFor();
                                            } catch (Exception e) {
                                                Log.e(getString(R.string.app_name), "Error while executing btrfs to set default subvolume!", e);
                                                Utils.safeDestroy(sub_proc);
                                                return;
                                            }

                                            Utils.safeDestroy(sub_proc);

                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(getString(R.string.app_name), "Error while reading btrfs output!", e);
                                    Utils.safeDestroy(proc);
                                    return;
                                }

                                Utils.safeDestroy(proc);
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }
}
