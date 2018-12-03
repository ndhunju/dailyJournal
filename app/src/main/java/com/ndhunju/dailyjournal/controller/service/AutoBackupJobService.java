package com.ndhunju.dailyjournal.controller.service;

import android.app.Notification;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.ndhunju.dailyjournal.service.PreferenceService;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AutoBackupJobService extends JobService implements AutoBackupHelper.EventListener {

    private static final int JOB_ID = 1;
    private static final int MAX_RETRY = 3;

    public static void schedule(Context context, long startTime, long repeatInterval) {
        ComponentName component = new ComponentName(context, AutoBackupJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component);
        builder.setMinimumLatency(startTime);
        builder.setOverrideDeadline(1000 * 60 * 10); // +-10 mins

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public static void cancel(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    private int retryCount;

    @Override
    public boolean onStartJob(JobParameters params) {
        AutoBackupHelper autoBackupHelper = new AutoBackupHelper(this);
        autoBackupHelper.setEventListener(this);
        autoBackupHelper.startBackup();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // whether or not you would like JobScheduler to automatically retry your failed job.
        boolean doRetry = retryCount++ < MAX_RETRY;
        if (!doRetry) {
            stopForeground(true);
            // schedule another job
            PreferenceService.from(this).updateAutoBackup();
        }
        return doRetry;
    }

    @Override
    public void onStartForeground(int id, Notification notification) {
        startForeground(id, notification);
    }


    @Override
    public void onFinishBackUp() {
        stopForeground(true);
        PreferenceService.from(this).updateAutoBackup(); // schedule another job
        stopSelf();
    }
}
