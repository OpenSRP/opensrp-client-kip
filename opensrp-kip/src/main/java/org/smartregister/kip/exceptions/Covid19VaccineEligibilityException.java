package org.smartregister.kip.exceptions;

import android.support.annotation.NonNull;

public class Covid19VaccineEligibilityException extends Exception {
    public Covid19VaccineEligibilityException() {
       super("Could not process this OPD covid 19 vaccination eligibility  Event");
    }

    public Covid19VaccineEligibilityException(@NonNull String message) {
        super("Could not process this OPD covid 19 vaccination eligibility  Event because " + message);
    }
}
