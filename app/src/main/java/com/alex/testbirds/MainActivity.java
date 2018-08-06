package com.alex.testbirds;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alex.testbirds.databinding.ActivityMainBinding;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;
import static android.os.Process.myUid;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getStartAction().observe(this, aVoid -> finish());
        binding.setViewModel(viewModel);
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.BLOCKED)) {
            Toast.makeText(this, getString(R.string.app_is_blocked, extras.getString(Constants.APP_NAME)), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkForPermission(this)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.permissions_title);
            alertDialogBuilder.setMessage(R.string.permissions_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.permissions_ok_button,
                            (dialog, id) -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    viewModel.setEditMode(true);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    menu.add(R.string.menu_clear_all);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    viewModel.clearAll();
                    mode.finish();
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    viewModel.saveSelectedApps();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), context.getPackageName());
        return mode == MODE_ALLOWED;
    }
}
