package org.smartregister.kip.domain;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jason Rogena - jrogena@ona.io on 15/06/2017.
 */

public class Tally implements Serializable {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private MohIndicator indicator;
    private long id;
    private String value;
    private String providerId;
    private Date updatedAt;


    @JsonProperty
    private Date createdAt;

    public Tally() {
    }

    public MohIndicator getIndicator() {
        return indicator;
    }

    public void setIndicator(MohIndicator indicator) {
        this.indicator = indicator;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Tally) {
            Tally tally = (Tally) o;
            if (getIndicator().getIndicatorCode().equals(tally.getIndicator().getIndicatorCode())) {
                return true;
            }
        }
        return false;
    }
}
