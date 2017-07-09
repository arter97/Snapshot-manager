package com.arter97.snapshotmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshStat();

        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.main_fab);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                Intent menuIntent;

                switch (menuItem.getItemId()) {
                    case R.id.action_add:
                        // Add EditText to AlertDialog
                        final EditText edittext = new EditText(MainActivity.this.getApplicationContext());
                        edittext.setSingleLine();
                        edittext.setTextColor(getColor(android.R.color.white));

                        FrameLayout container = new FrameLayout(MainActivity.this);
                        FrameLayout.LayoutParams params =
                                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        // Set a visually-pleasing margin
                        params.setMargins(10, 0, 10, 0);
                        edittext.setLayoutParams(params);
                        container.addView(edittext);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setView(container);
                        builder.setTitle(R.string.fab_add_title)
                                .setMessage("\n" + getString(R.string.fab_add_msg))
                                .setCancelable(true)
                                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                })
                                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Get today's date
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                                        String date = sdf.format(Calendar.getInstance().getTime());

                                        // Get user's input
                                        String input = Utils.sanitize(MainActivity.this, edittext.getText().toString());
                                        String name = date;
                                        if (!input.isEmpty())
                                            name += "-" + input;

                                        for (String s : Utils.listSnapshots(MainActivity.this)) {
                                            if (s.equals(name)) {
                                                Toast.makeText(getBaseContext(), getString(R.string.snapshot_exists), Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }

                                        // Execute btrfs
                                        // TODO : Make it R/O and change it to R/W during restore
                                        Runtime rt = Runtime.getRuntime();
                                        String[] commands = {"su", "-c",
                                                "btrfs subvolume snapshot /data /data/btrfs-snapshot/" + name};

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

                                        Toast.makeText(getBaseContext(), getString(R.string.snapshot_created), Toast.LENGTH_SHORT).show();

                                        refreshStat();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                        break;

                    case R.id.action_delete:
                        if (Utils.listSnapshots(MainActivity.this).length == 0) {
                            Toast.makeText(getBaseContext(), getString(R.string.no_snapshots), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        menuIntent = new Intent(MainActivity.this, DeleteSnapshot.class);
                        startActivity(menuIntent);
                        break;

                    case R.id.action_history:
                        if (Utils.listSnapshots(MainActivity.this).length == 0) {
                            Toast.makeText(getBaseContext(), getString(R.string.no_snapshots), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        menuIntent = new Intent(MainActivity.this, RestoreSnapshot.class);
                        startActivity(menuIntent);
                        break;

                    case R.id.action_help:
                        // TODO
                        break;
                }

                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshStat();
    }

    private void refreshStat() {
        boolean green = false;
        String latestDate = "";

        // Refresh last snapshot stat
        try {
            String[] snapshot = Utils.listSnapshots(this);
            latestDate = snapshot[snapshot.length - 1].substring(0, 8);

            // Get today's date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String date = sdf.format(Calendar.getInstance().getTime());

            // Latest snapshot should be taken within 2 days ago
            if (Integer.parseInt(date) - 2 <= Integer.parseInt(latestDate)) {
                green = true;
            }
        } catch (Exception e) {
            // Do nothing, just alert the user
        }

        // Set the screen accordingly to the protected status
        if (green) {
            ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getDrawable(R.drawable.green));
            ((TextView) findViewById(R.id.textView)).setText(getString(R.string.green));
            ((TextView) findViewById(R.id.textView2)).setText(getString(R.string.snapshot_stat, latestDate));

            getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorGreen));
            getWindow().setStatusBarColor(getColor(R.color.colorGreen));
        } else {
            ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getDrawable(R.drawable.red));
            ((TextView) findViewById(R.id.textView)).setText(getString(R.string.red));
            if (latestDate.isEmpty())
                ((TextView) findViewById(R.id.textView2)).setText(getString(R.string.snapshot_stat_bad));
            else
                ((TextView) findViewById(R.id.textView2)).setText(getString(R.string.snapshot_stat, latestDate) +
                        "\n\n" + getString(R.string.snapshot_stat_bad));

            getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorRed));
            getWindow().setStatusBarColor(getColor(R.color.colorRed));
        }
    }
}
