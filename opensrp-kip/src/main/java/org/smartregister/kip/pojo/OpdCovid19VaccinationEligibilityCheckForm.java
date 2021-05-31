package org.smartregister.kip.pojo;

public class OpdCovid19VaccinationEligibilityCheckForm {
    private String id;
    private String baseEntityId;
    private String visitId;
    private String temperature;
    private String covid19History;
    private String oralConfirmation;
    private String respiratorySymptoms;
    private String otherRespiratorySymptoms;
    private String allergies;
    private String otherAllergies;
    private String age;
    private String date;
    private String createdAt;

    public OpdCovid19VaccinationEligibilityCheckForm() {
    }

    public OpdCovid19VaccinationEligibilityCheckForm(String id, String baseEntityId, String visitId, String temperature, String covid19History, String oralConfirmation, String respiratorySymptoms, String otherRespiratorySymptoms, String allergies, String otherAllergies, String age, String date, String createdAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.visitId = visitId;
        this.temperature = temperature;
        this.covid19History = covid19History;
        this.oralConfirmation = oralConfirmation;
        this.respiratorySymptoms = respiratorySymptoms;
        this.otherRespiratorySymptoms = otherRespiratorySymptoms;
        this.allergies = allergies;
        this.otherAllergies = otherAllergies;
        this.age = age;
        this.date = date;
        this.createdAt = createdAt;
    }

    public String getOtherRespiratorySymptoms() {
        return otherRespiratorySymptoms;
    }

    public void setOtherRespiratorySymptoms(String otherRespiratorySymptoms) {
        this.otherRespiratorySymptoms = otherRespiratorySymptoms;
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

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getCovid19History() {
        return covid19History;
    }

    public void setCovid19History(String covid19History) {
        this.covid19History = covid19History;
    }

    public String getOralConfirmation() {
        return oralConfirmation;
    }

    public void setOralConfirmation(String oralConfirmation) {
        this.oralConfirmation = oralConfirmation;
    }

    public String getRespiratorySymptoms() {
        return respiratorySymptoms;
    }

    public void setRespiratorySymptoms(String respiratorySymptoms) {
        this.respiratorySymptoms = respiratorySymptoms;
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
