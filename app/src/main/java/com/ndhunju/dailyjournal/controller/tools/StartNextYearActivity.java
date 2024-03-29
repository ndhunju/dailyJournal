package com.ndhunju.dailyjournal.controller.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.backup.BackupActivity;
import com.ndhunju.dailyjournal.controller.backup.BackupPreferenceFragment;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ndhunju on 9/4/17.
 * This activity is responsible for guiding user through the steps of starting a new financial year.
 */

public class StartNextYearActivity extends BaseActivity {

    private static final int REQUEST_CODE_BACKUP_COMPLETE = 5416;

    ViewGroup mStepsContainer;
    Button mNextButton;

    Services mServices;
    Calendar nextFinancialYear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_next_year);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mServices = Services.getInstance(this);

        // calculate next financial year
        nextFinancialYear = Calendar.getInstance();
        nextFinancialYear.setTime(mServices.getFinancialYear());
        nextFinancialYear.add(Calendar.YEAR, 1);

        mStepsContainer = findViewById(R.id.next_year_steps_container);

        mNextButton = findViewById(R.id.activity_next_year_next_btn);
        mNextButton.setOnClickListener(v -> {
            if (mNextButton.getText().equals(getString(R.string.str_done))) {
                finish();
            } else if (hasNextStep()) {
                mNextButton.setText(getString(R.string.str_next));
                nextStep().run();
            } else {
                mNextButton.setText(getString(R.string.str_done));
            }
        });


        addStep(new BackUpStep());
        addStep(new DeleteJournals());
        addStep(new MoveParties());
        addStep(new UpdateFinancialYear());

        for (Step step : getAllSteps()) {
            mStepsContainer.addView(step.getCheckBox());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("StartNextYear");
    }

    private void onStepsFinished() {
        mNextButton.setText(getString(R.string.str_done));
    }

    int currentStepIndex = 0;
    List<Step> steps = new ArrayList<>();

    List<Step> getAllSteps() {
        return steps;
    }

    private void addStep(Step step) {
        steps.add(step);
    }

    private boolean hasNextStep() {
        return currentStepIndex < steps.size();
    }

    private Step nextStep() {
        return steps.get(currentStepIndex);
    }

    private void incrementStepIndex() {
        currentStepIndex++;
        if (currentStepIndex >= steps.size()) {
            onStepsFinished();
        }
    }

    private void enableNextButton(boolean enable) {
        mNextButton.setEnabled(enable);
    }

    private class BackUpStep extends Step {

        BackUpStep() {
            super(getActivity());
        }

        @Override
        String getMessage() {
            return getString(R.string.msg_begin_next_year_step_backup, UtilsFormat.formatDate(mServices.getFinancialYear(), getActivity()));
        }

        @Override
        public void run() {
            super.run();
            UtilsView.alert(
                    getActivity(),
                    getString(R.string.msg_begin_next_year_step_backup_start_msg),
                    (dialog, which) -> startActivityForResult(
                            new Intent(getActivity(), BackupActivity.class).putExtra(
                                            BackupPreferenceFragment.KEY_FINISH_ON_BACKUP_SUCCESS,
                                            true
                                    )
                            , REQUEST_CODE_BACKUP_COMPLETE
                    )
            );
        }
    }

    private class DeleteJournals extends Step {

        DeleteJournals() {
            super(getActivity());
        }

        @Override
        String getMessage() {
            return getString(R.string.msg_begin_next_year_step_delete_journal, UtilsFormat.getJournalFromPref(getActivity()));
        }

        @Override
        public void run() {
            super.run();
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    return mServices.eraseAllJournalsOnly();
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    onFinish(aBoolean);
                }
            }.execute();
        }
    }

    private class MoveParties extends Step {

        MoveParties() {
            super(getActivity());
        }

        @Override
        public void run() {
            super.run();
            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... params) {
                    return mServices.addBalanceAsOpeningJournalAndDeleteParty(nextFinancialYear.getTimeInMillis());
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    onFinish(aBoolean);
                }
            }.execute();
        }

        @Override
        String getMessage() {
            return getString(R.string.msg_begin_next_year_step_move_parties, UtilsFormat.getPartyFromPref(getActivity()));
        }
    }

    private class UpdateFinancialYear extends Step {

        public UpdateFinancialYear() {
            super(getActivity());
        }

        @Override
        public void run() {
            super.run();
            mServices.forceSetFinancialYear(nextFinancialYear.getTime());
            onFinish(true);
        }

        @Override
        String getMessage() {
            return getString(R.string.msg_begin_next_year_step_update_year, UtilsFormat.formatDate(nextFinancialYear.getTime(), getActivity()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_BACKUP_COMPLETE:
                if (resultCode == RESULT_OK) {
                    boolean success = data != null && data.getBooleanExtra(BackupPreferenceFragment.KEY_BACKUP_RESULT, false);
                    if (hasNextStep()) {
                        nextStep().onFinish(success);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Activity getActivity() {
        return this;
    }

    private abstract class Step implements Runnable, View.OnTouchListener {

        CheckBox mCheck;

        Step(Context context) {
            mCheck = new CheckBox(context);
            mCheck.setText(getMessage());
            mCheck.setOnTouchListener(this);
        }

        abstract String getMessage();

        CheckBox getCheckBox() {
            return mCheck;
        }

        @Override
        public void run() {
            enableNextButton(false);
        }

        public void onFinish(boolean success) {
            enableNextButton(true);
            mCheck.setChecked(success);
            if (success) {
                incrementStepIndex();
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true; // don't let user change the state
        }
    }

}
