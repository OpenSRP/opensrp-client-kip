package org.smartregister.kip.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by coder on 6/6/17.
 */
public class MohIndicator implements Serializable {
    private long id;
    private String indicatorCode;
    private String antigen;
    private String age;
    private Date createdAt;
    private Date updatedAt;

    public MohIndicator() {
    }

    public MohIndicator(long id, String indicatorCode, String antigen, String age, Date createdAt, Date updatedAt) {
        this.id = id;
        this.indicatorCode = indicatorCode;
        this.antigen = antigen;
        this.age = age;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIndicatorCode() {
        return indicatorCode;
    }

    public void setIndicatorCode(String indicatorCode) {
        this.indicatorCode = indicatorCode;
    }

    public void setAntigen(String antigen) {
        this.antigen = antigen;
    }

    public String getAntigen() {
        return antigen;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAge() {
        return age;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}
