package com.ndhunju.dailyjournal.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

public class ItemDescriptionAdapter extends ArrayAdapter<ItemDescriptionAdapter.Item> {

    Item[] items;

    public ItemDescriptionAdapter(@NonNull Context context, Item[] items) {
        super(context, R.layout.list_item);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Item getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        return items[i].id;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null);
        }

        if (position >= items.length) {
            return view;
        }

        TextView nameTextView = view.findViewById(R.id.tvName);
        TextView descriptionTextView = view.findViewById(R.id.tvDesc);

        nameTextView.setText(items[position].name);
        descriptionTextView.setText(HtmlCompat.fromHtml(items[position].description, HtmlCompat.FROM_HTML_MODE_COMPACT));


        return view;
    }

    public static class Item {
        int id;
        String name;
        String description;

        public Item(String name, String description) {
            this.name = name;
            this.description = description;
            this.id = name.hashCode();
        }
    }
}
