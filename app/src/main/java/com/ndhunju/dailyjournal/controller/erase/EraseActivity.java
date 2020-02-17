package com.ndhunju.dailyjournal.controller.erase;

import android.app.FragmentTransaction;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.NavDrawerActivity;
import com.ndhunju.dailyjournal.service.LockService;

/**
 * Created by ndhunju on 9/4/17.
 * This activity groups together all the tools relevant to erasing data.
 */

public class EraseActivity extends NavDrawerActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().add(R.id.content_frame,
                new ErasePreferenceFragment(), ErasePreferenceFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        // get user's pin before allowing to erase
        LockService.showLockScreen(this);
    }
}
