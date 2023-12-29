package com.ndhunju.dailyjournal.controller.party;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.ndhunju.dailyjournal.ObservableField;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.IconTextAdapter;
import com.ndhunju.dailyjournal.controller.JournalPagerActivity;
import com.ndhunju.dailyjournal.database.JournalDAO;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.List;

/**
 * This adapter is responsible for
 * - creating context menu for the items
 * - handling {@link android.view.View.OnClickListener} and
 * - updating views when dataset changes.
 */
public abstract class LedgerAdapter extends RecyclerView.Adapter implements JournalDAO.Observer {

    protected Party mParty;
    protected Context mContext;
    protected int mLayoutResId;
    protected Services mServices;
    protected List<Journal> mJournals;
    protected OnItemClickListener mOnItemClickListener;

    protected ObservableField<Boolean> showBalance = new ObservableField<>(true);

    public interface OnItemClickListener {
        void onItemClick (View view,int position, List<Journal> journals);
        /**
         * For one of the items, context menu was opened and a menu item was clicked
         * @param view : view associated with the item ( not context menu item)
         * @param position : position of the item ( not context menu item)
         * @param id : id of the context menu item
         */
        void onContextItemClick(View view, int position, long id);
    }

    public LedgerAdapter(Context context, Party party) {
        mParty = party;
        mContext = context;
        mServices = Services.getInstance(context);
        mJournals = mServices.getJournalsWithBalance(party.getId());
        setHasStableIds(true);
    }

    public LedgerAdapter(Context context, List<Journal> journals) {
        mContext = context;
        mJournals = journals;
        mServices = Services.getInstance(context);
        setHasStableIds(true);
    }

    public void setLayoutId(@LayoutRes int layoutResId) {
        mLayoutResId = layoutResId;
    }

    public void setJournals(List<Journal> journals) {
        mJournals = journals;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // set position as tag to use it later for context menu
        holder.itemView.setTag(position);
        // set long click listener for context menu
        holder.itemView.setOnLongClickListener((LedgerVH) holder);
        // set on click listener
        holder.itemView.setOnClickListener((LedgerVH) holder);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        holder.itemView.setOnClickListener(null);
        holder.itemView.setOnLongClickListener(null);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public int getItemCount() {
        return mJournals.size();
    }

    public Journal getItem(int position) {
        return mJournals.get(position);
    }

    public Context getContext() {
        return mContext;
    }

    public boolean isInBound(int index) {
        return mJournals.size() < index && index > -1;
    }

    public class LedgerVH extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        Journal currentJournal;

        public LedgerVH(View itemView) {
            super(itemView);
        }

        public void bind(Journal journal) {
            currentJournal = journal;
        }


        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) mOnItemClickListener.onItemClick(view, getAdapterPosition(), mJournals);
        }

        @Override
        public boolean onLongClick(final View view) {
            //prepare context menu
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final IconTextAdapter adapter;
            builder.setAdapter(adapter = IconTextAdapter.newInstance(getContext())
                    .add(R.string.str_edit, android.R.drawable.ic_menu_edit)
                    .add(R.string.str_delete, android.R.drawable.ic_menu_delete)
                    .setIconTint(-1), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mOnItemClickListener != null)
                                mOnItemClickListener.onContextItemClick(view, getAdapterPosition(), adapter.getItemId(i));
                        }
                    });
            builder.create().show();
            return true;

        }
    }
    @Override
    public void onJournalAdded(Journal journal) {
        // check if it is for current party
        if (journal.getPartyId() != mParty.getId()) {
            return;
        }

        if (journal.getDate() >= mJournals.get(mJournals.size()-1).getDate()) {
            // journal with later date added
            journal.setBalance(mJournals.get(mJournals.size()-1).getBalance() + (journal.getType() == Journal.Type.Credit ? -journal.getAmount() : journal.getAmount()));
            mJournals.add(journal);
            notifyItemInserted(mJournals.size()-1);
        } else {
            // journal added is not later or same as last journal added
            // so we don't know the exact position. Hence, invalidate whole data
            dataSetChanged();
        }
    }



    @Override
    public void onJournalChanged(Journal journal) {
        // check if it is for current party
        if (mParty != null && journal.getPartyId() != mParty.getId()) {
            return;
        }

        int pos;
        // check if Journal's position was set in the tag
        if (journal.getTag() instanceof Integer) {
            pos = (Integer) journal.getTag();
            // make sure the id matches
            if (isInBound(pos) && mJournals.get(pos).getId() == journal.getId()) {
                if (journal.getDate() != mJournals.get(pos).getDate()) {
                    // journal date changed. the order will defer
                    dataSetChanged();
                } else {
                    // update just the changed journal
                    mJournals.set(pos, journal);
                    notifyItemChanged(pos);
                    // the evaluated balance amount is no longer valid
                    showBalance.set(false);
                }
                return;
            }
        }

        // find if the changed journal is in the list
        for (int i = 0; i < mJournals.size() ; i++) {
            if (mJournals.get(i).getId() == journal.getId()) {
                if (mJournals.get(i).getDate() != journal.getDate()) {
                    // journal date changed. the order will defer
                    dataSetChanged();
                } else {
                    // update just the changed journal
                    mJournals.set(i, journal);
                    notifyItemChanged(i);
                    // the evaluated balance amount is no longer valid
                    showBalance.set(false);
                }
                return;
            }
        }

        dataSetChanged();

    }

    @Override
    public void onJournalDeleted(Journal journal) {
        // check if it is for current party
        if (journal.getPartyId() != mParty.getId()) {
            return;
        }

        // check if Journal's position was set in the tag
        if (journal.getTag() instanceof Integer) {
            int pos = (int) journal.getTag();
            // make sure it's the correct Journal
            if (isInBound(pos) && mJournals.get(pos).getId() == journal.getId()) {
                mJournals.remove(pos);
                notifyItemRemoved(pos);
                // the evaluated balance amount is no longer valid
                showBalance.set(false);
                return;
            }
        }

        // find if the deleted journal is in the list
        for (int i = 0; i < mJournals.size() ; i++)
            if (mJournals.get(i).getId() == journal.getId()) {
                mJournals.remove(i);
                notifyItemRemoved(i);
                // the evaluated balance amount is no longer valid
                showBalance.set(false);
                return;
            }

        dataSetChanged();
    }

    @Override
    public void onJournalDataSetChanged(long partyId) {
        if (mParty.getId() == partyId){
            dataSetChanged();
        }
    }

    public void dataSetChanged() {
        // get the latest data
        mJournals = mServices.getJournalsWithBalance(mParty.getId());
        // the balance amount is re-evaluated
        showBalance.set(true);
        notifyDataSetChanged();
    }

    public static void createJournalIntent(Activity activity, long[] journalIds, int position) {
        // Open journal activity to show the detail info of the clicked Journal
        Intent intent = new Intent(activity, JournalPagerActivity.class);
        Bundle extra = new Bundle();
        extra.putInt(Constants.KEY_JOURNAL_POS, position);
        // to pass array of long, you MUST use Bundle#putLongArray()
        extra.putLongArray(JournalPagerActivity.BUNDLE_JOURNAL_IDS, journalIds);
        intent.putExtras(extra);
        activity.startActivity(intent);

    }

    public static void onContextItemClick(final FragmentActivity activity, final LedgerAdapter ledgerAdapter, View view, final int position, long menuId) {
        switch ((int) menuId){
            case android.R.drawable.ic_menu_edit:
                // selected to edit the journal
                long[] id = new long[1];
                id[0] = ledgerAdapter.getItemId(position);
                LedgerAdapter.createJournalIntent(activity, id, position);
                break;
            case android.R.drawable.ic_menu_delete:
                // selected to delete the Journal
                String msg = String.format(activity.getString(R.string.msg_delete_confirm), activity.getString(R.string.str_journal));
                //Alert user before deleting the Journal
                UtilsView.alert(activity, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Services.getInstance(activity).deleteJournal(ledgerAdapter.getItem(position));
                        String msg = String.format(activity.getString(R.string.msg_deleted), activity.getString(R.string.str_journal));
                        UtilsView.toast(activity, msg);
                    }
                }, null);

                break;
        }

    }
}
