package com.alex.testbirds;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.alex.testbirds.databinding.RowAppBinding;

import java.util.ArrayList;
import java.util.List;

public class InstalledAppsAdapter extends RecyclerView.Adapter<InstalledAppsAdapter.AppsViewHolder> {

    private List<AppInfo> apps = new ArrayList<>();
    private boolean editMode;

    public InstalledAppsAdapter(@NonNull List<AppInfo> apps) {
        this.apps = apps;
    }

    @NonNull
    @Override
    public AppsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowAppBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_app,
                parent, false);
        return new AppsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppsViewHolder holder, int position) {
        holder.dataBinder.setViewModel(new AppItemViewModel(apps.get(position), editMode));
        holder.dataBinder.executePendingBindings();
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }

    public void clearAll() {
        for (AppInfo appInfo : apps) {
            appInfo.resetSelected();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public static class AppsViewHolder extends RecyclerView.ViewHolder {

        final RowAppBinding dataBinder;

        AppsViewHolder(RowAppBinding mViewBinder) {
            super(mViewBinder.getRoot());
            this.dataBinder = mViewBinder;
        }
    }
}
