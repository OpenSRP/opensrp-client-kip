package org.smartregister.kip.exceptions;

import androidx.annotation.NonNull;

public class Covid19VaccinationWaitingListException extends Exception {
    public Covid19VaccinationWaitingListException() {
       super("Could not process this OPD covid 19 vaccination waiting list  Event");
    }

    public Covid19VaccinationWaitingListException(@NonNull String message) {
        super("Could not process this OPD covid 19 vaccination waiting list Event because " + message);
    }

}
