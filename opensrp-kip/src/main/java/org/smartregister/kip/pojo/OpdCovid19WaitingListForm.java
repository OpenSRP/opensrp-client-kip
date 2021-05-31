package org.smartregister.kip.pojo;

public class OpdCovid19WaitingListForm {
    private String id;
    private String baseEntityId;
    private String visitId;
    private String waitingList;
    private String age;
    private String date;
    private String createdAt;

    public OpdCovid19WaitingListForm() {
    }

    public OpdCovid19WaitingListForm(String id, String baseEntityId, String visitId, String waitingList, String age, String date, String createdAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.visitId = visitId;
        this.waitingList = waitingList;
        this.age = age;
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

    public String getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(String waitingList) {
        this.waitingList = waitingList;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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
