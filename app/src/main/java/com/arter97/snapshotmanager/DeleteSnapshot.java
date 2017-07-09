package com.arter97.snapshotmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class DeleteSnapshot extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_snapshot);

        final ListView listView = (ListView) findViewById(R.id.delete_listview);
        final ArrayList<String> listItems = new ArrayList<>(Arrays.asList(Utils.listSnapshots(this)));
        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice,
                listItems);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // adapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.delete_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int selected = 0;
                final SparseBooleanArray selArray = listView.getCheckedItemPositions();

                for (int i = 0; i < selArray.size(); i++) {
                    if (selArray.get(selArray.keyAt(i)))
                        selected++;
                }

                if (selected < 1) {
                    Toast.makeText(getBaseContext(), getString(R.string.sel_more), Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(DeleteSnapshot.this);
                builder.setTitle(getString(R.string.fab_rm))
                        .setMessage(getString(R.string.fab_rm_msg, selected))
                        .setCancelable(true)
                        .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Execute btrfs
                                Runtime rt = Runtime.getRuntime();

                                for (int i = 0; i < selArray.size(); i++) {
                                    if (selArray.get(selArray.keyAt(i))) {
                                        String[] commands = {"su", "-c",
                                                "btrfs subvolume delete -c /data/btrfs-snapshot/" + listItems.get(selArray.keyAt(i))};
                                        Process proc = null;
                                        try {
                                            proc = rt.exec(commands);
                                            proc.waitFor();
                                        } catch (Exception e) {
                                            Log.e(getString(R.string.app_name), "Error while executing btrfs!", e);
                                            Utils.safeDestroy(proc);
                                            return;
                                        }

                                        Utils.safeDestroy(proc);
                                    }
                                }
                                Toast.makeText(getBaseContext(), getString(R.string.snapshot_deleted), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
}
