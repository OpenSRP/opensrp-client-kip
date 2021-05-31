package org.smartregister.kip.exceptions;

import android.support.annotation.NonNull;

public class Covid19CalculateRiskFactorException extends Exception {
    public Covid19CalculateRiskFactorException() {
       super("Could not process this OPD calculate risk factor Event");
    }

    public Covid19CalculateRiskFactorException(@NonNull String message) {
        super("Could not process this OPD calculate risk factor Event because " + message);
    }
}
