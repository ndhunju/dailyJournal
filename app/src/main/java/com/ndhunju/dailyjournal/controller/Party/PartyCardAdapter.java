package com.ndhunju.dailyjournal.controller.party;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.List;

/**
 * Created by dhunju on 10/15/2015.
 * Adapter for PartyList
 */
public class PartyCardAdapter extends ArrayAdapter<Party> {

    public PartyCardAdapter(Context context, List<Party> objects) {
        super(context, R.layout.party_card, objects);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.party_card, null);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50,50,50,50);
            convertView.setLayoutParams(lp);
            viewHolder.picImageView = (ImageView)convertView.findViewById(R.id.party_card_circle_iv);
            viewHolder.partyNameTV = (TextView)convertView.findViewById(R.id.party_card_party_name_tv);
            viewHolder.balanceTV = (TextView)convertView.findViewById(R.id.party_card_party_balance_tv);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Party currentParty = getItem(position);
        viewHolder.picImageView.setImageDrawable(currentParty.getPicturePath().equals("")?
                getContext().getResources().getDrawable(R.drawable.party_default_pic):
                Drawable.createFromPath(currentParty.getPicturePath()));
        viewHolder.partyNameTV.setText(currentParty.getName());

        double balance = currentParty.calculateBalances();
        viewHolder.balanceTV.setText(UtilsFormat.formatCurrency(balance, getContext()));
        viewHolder.balanceTV.setTextColor(getContext().getResources().getColor(balance < 0 ? R.color.red_medium : R.color.green_medium));

        return convertView;
    }

    static class ViewHolder{

        ImageView picImageView;
        TextView partyNameTV;
        TextView balanceTV;
    }
}
