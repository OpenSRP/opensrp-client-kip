package org.smartregister.kip.pojo;


public class OpdCovid19CalculateRiskFactorForm {
    private String id;
    private String baseEntityId;
    private String visitId;
    private String preExistingConditions;
    private String otherPreExistingConditions;
    private String occupation;
    private String otherOccupation;
    private String age;
    private String date;
    private String createdAt;

    public OpdCovid19CalculateRiskFactorForm() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public OpdCovid19CalculateRiskFactorForm(String id, String baseEntityId, String visitId, String preExistingConditions, String otherPreExistingConditions, String occupation, String otherOccupation, String age, String date, String createdAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.visitId = visitId;
        this.preExistingConditions = preExistingConditions;
        this.otherPreExistingConditions = otherPreExistingConditions;
        this.occupation = occupation;
        this.otherOccupation = otherOccupation;
        this.age = age;
        this.date = date;
        this.createdAt = createdAt;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getPreExistingConditions() {
        return preExistingConditions;
    }

    public void setPreExistingConditions(String preExistingConditions) {
        this.preExistingConditions = preExistingConditions;
    }

    public String getOtherPreExistingConditions() {
        return otherPreExistingConditions;
    }

    public void setOtherPreExistingConditions(String otherPreExistingConditions) {
        this.otherPreExistingConditions = otherPreExistingConditions;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getOtherOccupation() {
        return otherOccupation;
    }

    public void setOtherOccupation(String otherOccupation) {
        this.otherOccupation = otherOccupation;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
