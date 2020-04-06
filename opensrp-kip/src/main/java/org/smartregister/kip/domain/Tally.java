package org.smartregister.kip.domain;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-07-11
 */

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.Date;

public class Tally implements Serializable {

    private String indicator;
    @JsonProperty
    private long id;
    @JsonProperty
    private String value;
    private MohIndicator mohIndicator;
    private String providerId;
    private Date updatedAt;

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

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public MohIndicator getMohIndicator() {
        return mohIndicator;
    }

    public void setMohIndicator(MohIndicator mohIndicator) {
        this.mohIndicator = mohIndicator;
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
}
