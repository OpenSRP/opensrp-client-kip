package org.smartregister.kip.pojo;

public class OpdSMSReminderForm {

    private String id;
    private String baseEntityId;
    private String visitId;
    private String smsReminder;
    private String date;
    private String createdAt;

    public OpdSMSReminderForm() {
    }

    public OpdSMSReminderForm(String id, String baseEntityId, String visitId, String smsReminder, String date, String createdAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.visitId = visitId;
        this.smsReminder = smsReminder;
        this.date = date;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getSmsReminder() {
        return smsReminder;
    }

    public void setSmsReminder(String smsReminder) {
        this.smsReminder = smsReminder;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
