package com.ndhunju.dailyjournal.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dhunju on 9/29/2015.
 */
public class CheckedListAdapter<T extends CheckedListAdapter.Id> extends BaseAdapter {

    private List<T> mList;
    private Context mContext;
    private boolean[] mChecked;
    private OnMultiChoiceClickListener mListener;

    public CheckedListAdapter(Context context, List<T> list, OnMultiChoiceClickListener listener){
        mContext = context;
        mList = list;
        mChecked = new boolean[list.size()];
        mListener = listener;
    }



    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mList.get(i).getId();
    }

    public boolean isChecked(int pos){
        return mChecked[pos];
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final CheckBox checkBox;
        if(view == null) {
             checkBox = new CheckBox(mContext);
        }else{
            checkBox = (CheckBox)view;
        }

        checkBox.setText(getItem(i).getText());
        checkBox.setPadding(5, 5, 5, 5);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChecked[i] = !mChecked[i];
                mListener.onClick(i, mChecked[i]);
                checkBox.setChecked(mChecked[i]);

            }
        });

        return checkBox;
    }

    public interface Id{
        public long getId();
        public String getText();
    }

    public interface OnMultiChoiceClickListener{
        public void onClick(int pos, boolean selected);
    }
}
