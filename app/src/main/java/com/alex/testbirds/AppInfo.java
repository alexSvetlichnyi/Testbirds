package com.alex.testbirds;

public class AppInfo {
    private final String appName;
    private final String packageName;
    private boolean selected;

    public AppInfo(String appName, String packageName, boolean selected) {
        this.appName = appName;
        this.selected = selected;
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void toggle() {
        this.selected = !selected;
    }

    public void resetSelected() {
        this.selected = false;
    }

    public String getPackageName() {
        return packageName;
    }
}
