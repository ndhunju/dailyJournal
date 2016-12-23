package com.ndhunju.dailyjournal.controller;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhunju on 7/28/2016.
 * This adapter is responsible for displaying list of item
 * that has an icon followed by a text.
 */
public class IconTextAdapter extends ArrayAdapter<String> {

    /**
     * <b>Note. This helper method returns instance of {@link IconTextAdapter} not
     * any of its sub classes.</b>
     */
    public static IconTextAdapter newInstance(Context context) {
        IconTextAdapter adapter = new IconTextAdapter(context);
        return adapter;
    }

    List<Integer> iconIds;
    TextView textView;
    ImageView imageView;
    Drawable drawable;
    int tint;

    public IconTextAdapter(Context context) {
        super(context, R.layout.item_nav_icon_text);
        iconIds = new ArrayList<>();
    }

    public IconTextAdapter add(@StringRes int itemResId, @DrawableRes int itemIconId) {
        add(getContext().getString(itemResId));
        iconIds.add(itemIconId);
        return this;
    }

    public IconTextAdapter setIconTint(@ColorInt int tint) {
        this.tint = tint;
        return this;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_nav_icon_text, parent, false);
        textView  = (TextView) convertView.findViewById(R.id.text);
        imageView = (ImageView) convertView.findViewById(R.id.icon_image);

        textView.setText(getItem(position));
        drawable = getItemIcon(position);
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), tint);
        imageView.setImageDrawable(drawable);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return iconIds.get(position);
    }

    @Override
    public String getItem(int position) {
        return super.getItem(position);
    }

    public Drawable getItemIcon(int position) {
        return ContextCompat.getDrawable(getContext(), iconIds.get(position));
    }
}
