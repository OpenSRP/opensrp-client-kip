package org.smartregister.kip.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Jason Rogena - jrogena@ona.io on 15/06/2017.
 */

public class DailyTally extends Tally implements Serializable {
    private Date day;

    public DailyTally() {
        super();
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }
}
