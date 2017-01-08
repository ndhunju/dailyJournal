package com.ndhunju.dailyjournal.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.backup.BackupActivity;
import com.ndhunju.dailyjournal.util.UtilsView;

/**
 * Created by ndhunju on 1/7/17.
 * This activity groups together all the tools this app
 * has to offer.
 */

public class ToolsActivity extends NavDrawerActivity {

    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addContentFrame(R.layout.activity_tools);

        // wire up and set up
        recyclerView = (RecyclerView) findViewById(R.id.activity_tools_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2,
                GridLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new ToolsAdapter());
    }

    private class ToolsAdapter extends RecyclerView.Adapter {

        int[] itemStringResId = {R.string.activity_home_find_journal_by_date, R.string.activity_home_search_journal_notes,
                R.string.activity_home_backup, R.string.activity_home_charts};
        int[] itemIconResId = {R.drawable.ic_date_range, R.drawable.ic_search,
                R.drawable.ic_save, R.drawable.ic_chart_pie};

        public ToolsAdapter() {}

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ToolVH(LayoutInflater.from(getContext()).inflate(R.layout.item_activity_home, parent, false),
                    parent.getMeasuredWidth()/2) ;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ToolVH) holder).bind(position);
        }

        @Override
        public int getItemCount() {
            return itemStringResId.length;
        }

        class ToolVH extends RecyclerView.ViewHolder implements View.OnClickListener{

            TextView itemName;
            ImageView itemIcon;

            public ToolVH(View itemView, int itemSize) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemName = (TextView) itemView.findViewById(R.id.item_home_activity_name);
                itemIcon = (ImageView)itemView.findViewById(R.id.item_home_activity_icon);

                int margin = UtilsView.dpToPx(getContext(), 5);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemSize - 2*margin, itemSize - 2*margin);
                params.setMargins(margin, margin, margin, margin);
                itemView.setLayoutParams(params);

                itemIcon.setMinimumWidth(itemSize/2);
                itemIcon.setMinimumHeight(itemSize/2);
            }

            public void bind(int position) {
                itemView.setTag(itemStringResId[position]);
                itemName.setText(itemStringResId[position]);
                itemIcon.setImageResource(itemIconResId[position]);
            }

            @Override
            public void onClick(View v) {
                switch ((int) v.getTag()) {
                    case R.string.activity_home_find_journal_by_date:
                        startActivity(new Intent(getContext(), SpannedLedgerListActivity.class));
                        break;
                    case R.string.activity_home_backup:
                        startActivity(new Intent(getContext(), BackupActivity.class));
                        break;
                    case R.string.activity_home_search_journal_notes:
                        startActivity(new Intent(getContext(), SearchNotesActivity.class));
                        break;
                    case R.string.activity_home_charts:
                        startActivity(new Intent(getContext(), ChartsActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        break;
                }
            }
        }
    }

}
