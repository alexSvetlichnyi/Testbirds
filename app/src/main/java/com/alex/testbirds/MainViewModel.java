package com.alex.testbirds;

import android.app.ActivityManager;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainViewModel extends AndroidViewModel {
    private InstalledAppsAdapter adapter;
    private final List<AppInfo> installedApps = new ArrayList<>();
    private Set<String> selectedApps = new HashSet<>();
    private final MutableLiveData<Void> startAction = new MutableLiveData<>();
    public final ObservableField<Drawable> startServiceDrawable = new ObservableField<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        loadSelectedApps();
        PackageManager packageManager = application.getPackageManager();
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo app : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
            String appName = app.loadLabel(packageManager).toString();
            Boolean selected = selectedApps.contains(app.packageName);
            if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                if (!app.packageName.equals(getApplication().getPackageName())) {
                    installedApps.add(new AppInfo(appName, app.packageName, selected));
                }
            }
        }
        startServiceDrawable.set(getStartServiceDrawable());
    }

    public InstalledAppsAdapter getAdapter() {
        if (adapter == null) {
            adapter = new InstalledAppsAdapter(installedApps);
        }
        return adapter;
    }

    private void loadSelectedApps() {
        SharedPreferences sharedPreferences =   PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        selectedApps = sharedPreferences.getStringSet(Constants.APPS_LIST, new HashSet<>());
    }

    public void setEditMode(boolean editMode) {
        adapter.setEditMode(editMode);
    }

    public void clearAll() {
        setEditMode(false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(Constants.APPS_LIST, new HashSet<>()).apply();
        selectedApps.clear();
        adapter.clearAll();
    }

    public void saveSelectedApps() {
        setEditMode(false);
        Set<String> items = new HashSet<>();
        for (AppInfo appInfo : installedApps) {
            if (appInfo.isSelected()) {
                items.add(appInfo.getPackageName());
            }
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(Constants.APPS_LIST, items).apply();
    }

    @SuppressWarnings("unused")
    public void onStartClick(View view) {
        if (isMyServiceRunning()) {
            TestbirdsService.shouldContinue = false;
            startServiceDrawable.set(ContextCompat.getDrawable(getApplication(), android.R.drawable.ic_media_play));
        } else {
            saveSelectedApps();
            getApplication().startService(new Intent(this.getApplication(), TestbirdsService.class));
            startAction.setValue(null);
        }
    }

    public MutableLiveData<Void> getStartAction() {
        return startAction;
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (TestbirdsService.class.getName().equals(service.service.getClassName())) {
                    //service.
                    return true;
                }
            }
        }
        return false;
    }

    private Drawable getStartServiceDrawable() {
        return ContextCompat.getDrawable(getApplication(), isMyServiceRunning() ? android.R.drawable.ic_media_pause :
                android.R.drawable.ic_media_play);
    }
}
