package com.alex.testbirds;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckedTextView;

public class AppItemViewModel extends BaseObservable {
    private final AppInfo appInfo;
    private final boolean editMode;

    public AppItemViewModel(AppInfo appInfo, boolean editMode) {
        this.appInfo = appInfo;
        this.editMode = editMode;
    }

    @Bindable
    public String getAppName() {
        return appInfo.getAppName();
    }

    @Bindable
    public boolean isSelected() {
        return appInfo.isSelected();
    }

    @Bindable
    public boolean isEditModeSelected() {
        return editMode;
    }

    @SuppressWarnings("unused")
    public void onItemClick(View view) {
        appInfo.toggle();
        notifyPropertyChanged(BR.selected);
    }

    @BindingAdapter("checkBoxVisibility")
    public static void setCheckBoxVisibility(CheckedTextView checkedTextView, boolean visibility) {
        TypedValue typedValue = new TypedValue();

        // I used getActivity() as if you were calling from a fragment.
        // You just want to call getTheme() on the current activity, however you can get it
        checkedTextView.getContext().getTheme().resolveAttribute(android.R.attr.listChoiceIndicatorMultiple, typedValue, true);
        checkedTextView.setCheckMarkDrawable(visibility ? checkedTextView.getContext().getDrawable(typedValue.resourceId) : null);
    }
}
