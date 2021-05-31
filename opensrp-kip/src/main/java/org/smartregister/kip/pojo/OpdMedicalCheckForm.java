package org.smartregister.kip.pojo;

public class OpdMedicalCheckForm {

    private String id;
    private String baseEntityId;
    private String visitId;
    private String preExistingConditions;
    private String otherPreExistingConditions;
    private String allergies;
    private String otherAllergies;
    private String temperature;
    private String age;
    private String date;
    private String createdAt;

    public OpdMedicalCheckForm() {
    }

    public OpdMedicalCheckForm(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public OpdMedicalCheckForm(String id, String baseEntityId, String visitId, String preExistingConditions, String otherPreExistingConditions, String allergies, String otherAllergies, String temperature, String age, String date, String createdAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.visitId = visitId;
        this.preExistingConditions = preExistingConditions;
        this.otherPreExistingConditions = otherPreExistingConditions;
        this.allergies = allergies;
        this.otherAllergies = otherAllergies;
        this.temperature = temperature;
        this.age = age;
        this.date = date;
        this.createdAt = createdAt;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
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

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getOtherAllergies() {
        return otherAllergies;
    }

    public void setOtherAllergies(String otherAllergies) {
        this.otherAllergies = otherAllergies;
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
