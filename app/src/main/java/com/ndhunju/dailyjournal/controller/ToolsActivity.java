package com.ndhunju.dailyjournal.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.backup.BackupActivity;
import com.ndhunju.dailyjournal.controller.erase.EraseActivity;
import com.ndhunju.dailyjournal.controller.export.ExportPrintableActivity;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.util.UtilsView;

/**
 * Created by ndhunju on 1/7/17.
 * This activity groups together all the tools this app
 * has to offer.
 */

public class ToolsActivity extends NavDrawerActivity {

    private static final int REQUEST_CODE_BACKUP_DIR_PRINTABLE = 1264;
    private static final int REQUEST_PERMISSIONS_WRITE_STORAGE = 2323;

    RecyclerView recyclerView;
    int columnCount = 2;      // default column count

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addContentFrame(R.layout.activity_tools);

        // get column count value from the resources
        columnCount = getResources().getInteger(R.integer.activity_tools_column_count);

        // wire up and set up
        recyclerView = (RecyclerView) findViewById(R.id.activity_tools_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), columnCount,
                GridLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new ToolsAdapter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.str_debug);
        return super.onCreateOptionsMenu(menu);
    }

    private int numOfTimesClicked = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        numOfTimesClicked++;

        if (numOfTimesClicked > 3) {
            numOfTimesClicked = 0;
            startActivity(new Intent(getContext(), InternalToolsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("Tools");
    }

    private Activity getActivity() {
        return this;
    }

    private class ToolsAdapter extends RecyclerView.Adapter {

        int[] itemStringResId = {R.string.title_activity_daily_report,
                R.string.activity_home_find_journal_by_date,
                R.string.activity_home_search_journal_notes,
                R.string.activity_home_backup,
                R.string.activity_home_charts,
                R.string.str_export_printable,
                R.string.title_activity_erase,
                R.string.title_activity_start_next_year};

        int[] itemIconResId = {R.drawable.ic_daily_report_24dp,
                R.drawable.ic_date_range,
                R.drawable.ic_search,
                R.drawable.ic_save,
                R.drawable.ic_chart_pie,
                R.drawable.ic_content_copy_black_24dp,
                R.drawable.ic_delete_black_24dp,
                R.drawable.ic_forward_black_24dp};

        public ToolsAdapter() {}

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ToolVH(LayoutInflater.from(getContext()).inflate(R.layout.item_activity_home, parent, false),
                    parent.getMeasuredWidth()/columnCount) ;
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
                    case R.string.str_export_printable:
                        showExportPrintableOptions();
                        break;
                    case R.string.title_activity_erase:
                        startActivity(new Intent(getContext(), EraseActivity.class));
                        break;
                    case R.string.title_activity_start_next_year:
                        startActivity(new Intent(getContext(), StartNextYearActivity.class));
                        break;
                    case R.string.title_activity_daily_report:
                        startActivity(new Intent(getContext(), DailyReportActivity.class));
                }
            }
        }
    }

    private void showExportPrintableOptions() {
        Intent openExportActivity = new Intent(getContext(), ExportPrintableActivity.class);
        startActivity(openExportActivity);
    }


    private boolean checkWriteStoragePermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getActivity().requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_WRITE_STORAGE);
            }
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showExportPrintableOptions();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
