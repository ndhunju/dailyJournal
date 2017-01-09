package com.ndhunju.dailyjournal.controller.party;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;

import java.util.ArrayList;

/**
 * Created by dhunju on 10/2/2015.
 */
class PartyRowAdapter extends ArrayAdapter<Party> {


    public PartyRowAdapter(Context context, ArrayList<Party> parties){
        super(context, R.layout.party_row, parties);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.party_row, null);
            //cache view fields into the holder
            holder.displayName = (TextView)convertView.findViewById(R.id.party_row_party_name_tv);
            holder.imageView = (ImageView)convertView.findViewById(R.id.party_row_circle_iv);

            //tag the view with the holder instance for later lookup
            convertView.setTag(holder);
        }else{
            //since the convertView is not null, getInt the holder instance
            holder = (ViewHolder)convertView.getTag();
        }

        Party currentParty  =  getItem(position);
        holder.displayName.setText(currentParty.getName());
        holder.imageView.setImageDrawable(currentParty.getPicturePath().equals("") ?
                getContext().getResources().getDrawable(R.drawable.default_party_pic) :
                Drawable.createFromPath(currentParty.getPicturePath()));

        return convertView;
    }

    /**
     * Defines a class that hold resource IDs of each item layout
     * row to prevent having to look them up each time data is
     * bound to a row.
     */
    static class ViewHolder {
        TextView displayName;
        ImageView imageView;
    }

}
