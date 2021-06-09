package org.smartregister.kip.exceptions;

import androidx.annotation.NonNull;

public class Covid19VaccineRecordException extends Exception {
    public Covid19VaccineRecordException() {
       super("Could not process this OPD covid 19 vaccination  record Event");
    }

    public Covid19VaccineRecordException(@NonNull String message) {
        super("Could not process this OPD covid 19 vaccination  record Event because " + message);
    }
}
