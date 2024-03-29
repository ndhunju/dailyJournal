package com.ndhunju.dailyjournal.controller.service;

import android.app.Notification;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.PreferenceService;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AutoBackupJobService
        extends JobService
        implements AutoBackupWithRestApiHelper.EventListener {

    private static final int JOB_ID = 1;
    private static final int MAX_RETRY = 3;

    public static void schedule(Context context, long startTime, long repeatInterval) {
        ComponentName component = new ComponentName(context, AutoBackupJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component);
        // delay the job with given minLatencyMillis
        builder.setMinimumLatency(repeatInterval);
        // finish the job before passed maxExecutionDelayMillis
        builder.setOverrideDeadline(repeatInterval + (1000 * 60 * 10)); // can delay up to 10 mins

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        // when you schedule a job, old job with same id is removed
        // https://stackoverflow.com/q/45508335/1157458
        jobScheduler.schedule(builder.build());
    }

    public static void cancel(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    private int retryCount;
    private JobParameters parameters;

    @Override
    public boolean onStartJob(JobParameters params) {
        AutoBackupWithRestApiHelper autoBackupHelper = new AutoBackupWithRestApiHelper(this);
        autoBackupHelper.setEventListener(this);
        autoBackupHelper.startBackup();
        parameters = params;
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // whether or not you would like JobScheduler to automatically retry your failed job.
        boolean doRetry = retryCount++ < MAX_RETRY;
        if (!doRetry) {
            // not retrying anymore
            stopForeground(true);
            // schedule another job
            PreferenceService.from(this).updateAutoBackup();
            // Log this failure
            AnalyticsService.INSTANCE.logEvent("didStopAutoBackupServiceAfterMaxRetry");
        }
        return doRetry;
    }

    @Override
    public void onStartForeground(int id, Notification notification) {
        startForeground(id, notification);
    }

    @Override
    public void onFinishBackUp() {
        jobFinished(parameters, false);
        stopForeground(true);
        PreferenceService.from(this).updateAutoBackup(); // schedule another job
    }
}
