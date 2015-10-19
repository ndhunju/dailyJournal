package com.ndhunju.dailyjournal.controller.preference;

import android.content.Context;
import android.database.DataSetObserver;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;

import java.util.List;

/**
 * Created by dhunju on 10/9/2015.
 */
public class PreferenceListAdapter implements ListAdapter {

    List<PreferenceActivity.Header> mHeaders;
    Context context;

    public PreferenceListAdapter(List<PreferenceActivity.Header> headers, Context con){
        mHeaders = headers;
        context = con;
    }
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return mHeaders.size();
    }

    @Override
    public Object getItem(int i) {
        return mHeaders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mHeaders.get(i).id;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(view == null){
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.preference_adapter, null);
            //cache view fields into the holder
            viewHolder.name =  ((TextView) view.findViewById(R.id.preference_adapter_tv));
            viewHolder.icon = ((ImageView)view.findViewById(R.id.preference_adapter_iv));
            view.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.name.setText(mHeaders.get(i).titleRes);
        viewHolder.icon.setImageDrawable(context.getResources().getDrawable(mHeaders.get(i).iconRes));
        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return mHeaders.isEmpty();
    }

    /**
     * Defines a class that hold resource IDs of each item layout
     * row to prevent having to look them up each time data is
     * bound to a row.
     */
    static class ViewHolder {
        TextView name;
        ImageView icon;
    }
}
