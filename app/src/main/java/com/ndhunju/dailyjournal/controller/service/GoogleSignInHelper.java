package com.ndhunju.dailyjournal.controller.service;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Helper class that groups relevant objects like {@link Scope} and provides helper methods
 */
public class GoogleSignInHelper {

    public static final GoogleSignInHelper INSTANCE = new GoogleSignInHelper();

    public static GoogleSignInHelper get() {
        return INSTANCE;
    }

    Scope[] requiredScopes = {
//            GoogleSignInOptions.zat, // openId
//            GoogleSignInOptions.zar, // profile
            // Without email, gives "IllegalArgumentException: the name must not be empty: null"
            GoogleSignInOptions.zas, // email
            new Scope(DriveScopes.DRIVE_FILE)
    };

    private GoogleSignInHelper() {}

    public GoogleSignInOptions buildGoogleSigInOptions() {
        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder();

        for (Scope scope: requiredScopes) {
            builder.requestScopes(scope);
        }

        return builder.build();
    }

    public Set<Scope> requiredScopesAsSet() {
        return Sets.newHashSet(requiredScopes);
    }

    public List<String> requiredScopesAsStringList() {
        List<String> scopes = new ArrayList<>();
        for (Scope scope: requiredScopes) {
            scopes.add(scope.toString());
        }

        return scopes;
    }
}
