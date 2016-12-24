package com.ndhunju.dailyjournal.controller.party;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.IconTextAdapter;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 10/15/2015.
 * Adapter for PartyList
 */
public class PartyCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PartyDAO.Observer{

    private Context mContext;
    private Services mServices;
    private List<Party> mParties;
    private OnItemClickListener itemClickListener;
    private int activatedItemPos;

    private List<Party> filteredParties;
    private boolean inFilterMode;

    interface OnItemClickListener {
        void onItemClick(View view, int position, long id);

        /**
         * For one of the items, context menu was opened and a menu item was clicked
         * @param view : view associated with the item ( not context menu item)
         * @param position : position of the item ( not context menu item)
         * @param id : id of the context menu item
         */
        void onContextItemClick(View view, int position, long id);
    }

    public PartyCardAdapter(Context context, List<Party> objects) {
        mParties = objects;
        mContext = context;
        mServices = Services.getInstance(context);
        filteredParties = new ArrayList<>();
        activatedItemPos = -1;
        setHasStableIds(true);
        inFilterMode = false;
    }

    public void filter(CharSequence filter) {
        if (TextUtils.isEmpty(filter)) {
            inFilterMode = false;
            notifyDataSetChanged();
            return;
        }

        inFilterMode = true;
        filteredParties.clear();
        for (Party party : mParties)
            if (party.getName().toLowerCase().contains(filter.toString().toLowerCase()))
                filteredParties.add(party);
        notifyDataSetChanged();
    }

    public void setActivatedItemPos(int pos) {
        activatedItemPos = pos;
        notifyItemChanged(pos);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PartyVH(LayoutInflater.from(getContext()).inflate(R.layout.party_card, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((PartyVH) holder).bind(getItem(position), position);
    }

    @Override
    public void onPartyAdded(Party party) {
        inFilterMode = false;
        // we don't know the exact position. Hence, invalidate whole data
        dataSetChanged();
    }

    @Override
    public void onPartyChanged(Party party) {
        inFilterMode = false;

        // check if party's position was passed via tag
        if (party.getTag() instanceof Integer) {
            int pos = (Integer) party.getTag();
            // make sure the id matches
            if (isInBound(pos) && mParties.get(pos).getId() == party.getId()) {
                mParties.set(pos, party);
                notifyItemChanged(pos);
                return;
            }
        }

        // find the correct party in the list
        for (int i = 0; i < mParties.size() ; i++) {
            if (mParties.get(i).getId() == party.getId()) {
                mParties.set(i, party);
                notifyItemChanged(i);
                return;
            }
        }

        // party not found in the list. invalidate the list
        dataSetChanged();
    }

    @Override
    public void onPartyDeleted(Party party) {
        inFilterMode = false;

        // check if party's position was passed via tag
        if (party.getTag() instanceof Integer) {
            int pos = (Integer) party.getTag();
            // make sure it's the right party
            if (isInBound(pos) && mParties.get(pos).getId() == party.getId()) {
                mParties.remove(pos);
                notifyItemRemoved(pos);
                return;
            }
        }

        // find the right party in the list
        for (int i = 0; i < mParties.size() ; i++) {
            if (mParties.get(i).getId() == party.getId()) {
                mParties.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }

        // party not found in the list. Invalidate the list
        dataSetChanged();
    }

    public boolean isInBound(int index) {
        return inFilterMode
                ? filteredParties.size() < index && index > -1
                : mParties.size() < index && index > -1;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public int getItemCount() {
        return inFilterMode ? filteredParties.size() : mParties.size();
    }

    public Party getItem(int pos) {
        return inFilterMode ? filteredParties.get(pos) : mParties.get(pos);
    }

    private Context getContext() {
        return mContext;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    public void dataSetChanged() {
        inFilterMode = false;
        mParties = mServices.getParties();
        notifyDataSetChanged();

    }

    private class PartyVH extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView picImageView;
        TextView partyNameTV;
        TextView balanceTV;

        public PartyVH(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            picImageView = (ImageView)itemView.findViewById(R.id.party_card_circle_iv);
            partyNameTV  = (TextView) itemView.findViewById(R.id.party_card_party_name_tv);
            balanceTV    = (TextView) itemView.findViewById(R.id.party_card_party_balance_tv);
        }

        public void bind(Party party, int position) {
            //make the image circular
            RoundedBitmapDrawable bitmapDrawable = party.getPicturePath().equals("")?
                    RoundedBitmapDrawableFactory.create(getContext().getResources(),
                            BitmapFactory.decodeResource(getContext().getResources(), R.drawable.party_default_pic))
                    : RoundedBitmapDrawableFactory.create(getContext().getResources(),
                    party.getPicturePath());

            bitmapDrawable.setCircular(true);
            picImageView.setImageDrawable(bitmapDrawable);

            partyNameTV.setText(party.getName());

            double balance = party.calculateBalances();
            balanceTV.setText(UtilsFormat.formatCurrency(balance, getContext()));
            balanceTV.setTextColor(getContext().getResources().getColor(balance < 0 ? R.color.red_medium : R.color.green_medium));
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(v, getAdapterPosition(), getItemId());
        }

        @Override
        public boolean onLongClick(final View view) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final IconTextAdapter adapter;
            builder.setAdapter(adapter = IconTextAdapter.newInstance(getContext())
                    .add(R.string.str_edit, android.R.drawable.ic_menu_edit)
                    .add(R.string.str_delete, android.R.drawable.ic_menu_delete)
                    .setIconTint(-1),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (itemClickListener != null)
                                itemClickListener.onContextItemClick(view, getAdapterPosition(), adapter.getItemId(i));
                        }
            });
            builder.create().show();

            return true;
        }
    }
}
