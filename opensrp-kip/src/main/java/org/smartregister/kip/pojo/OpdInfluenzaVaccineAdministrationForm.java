package org.smartregister.kip.pojo;

public class OpdInfluenzaVaccineAdministrationForm {

    private String id;
    private String baseEntityId;
    private String visitId;
    private String influenzaVaccines;
    private String influenzaSiteOfAdministration;
    private String influenzaAdministrationDate;
    private String influenzaAdministrationRoute;
    private String influenzaLotNumber;
    private String influenzaVaccineExpiry;
    private String age;
    private String date;
    private String createdAt;

    public OpdInfluenzaVaccineAdministrationForm() {
    }

    public OpdInfluenzaVaccineAdministrationForm(String id, String baseEntityId, String visitId, String influenzaVaccines, String influenzaSiteOfAdministration, String influenzaAdministrationDate, String influenzaAdministrationRoute, String influenzaLotNumber, String influenzaVaccineExpiry, String age, String date, String createdAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.visitId = visitId;
        this.influenzaVaccines = influenzaVaccines;
        this.influenzaSiteOfAdministration = influenzaSiteOfAdministration;
        this.influenzaAdministrationDate = influenzaAdministrationDate;
        this.influenzaAdministrationRoute = influenzaAdministrationRoute;
        this.influenzaLotNumber = influenzaLotNumber;
        this.influenzaVaccineExpiry = influenzaVaccineExpiry;
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

    public String getInfluenzaVaccines() {
        return influenzaVaccines;
    }

    public void setInfluenzaVaccines(String influenzaVaccines) {
        this.influenzaVaccines = influenzaVaccines;
    }

    public String getInfluenzaSiteOfAdministration() {
        return influenzaSiteOfAdministration;
    }

    public void setInfluenzaSiteOfAdministration(String influenzaSiteOfAdministration) {
        this.influenzaSiteOfAdministration = influenzaSiteOfAdministration;
    }

    public String getInfluenzaAdministrationDate() {
        return influenzaAdministrationDate;
    }

    public void setInfluenzaAdministrationDate(String influenzaAdministrationDate) {
        this.influenzaAdministrationDate = influenzaAdministrationDate;
    }

    public String getInfluenzaAdministrationRoute() {
        return influenzaAdministrationRoute;
    }

    public void setInfluenzaAdministrationRoute(String influenzaAdministrationRoute) {
        this.influenzaAdministrationRoute = influenzaAdministrationRoute;
    }

    public String getInfluenzaLotNumber() {
        return influenzaLotNumber;
    }

    public void setInfluenzaLotNumber(String influenzaLotNumber) {
        this.influenzaLotNumber = influenzaLotNumber;
    }

    public String getInfluenzaVaccineExpiry() {
        return influenzaVaccineExpiry;
    }

    public void setInfluenzaVaccineExpiry(String influenzaVaccineExpiry) {
        this.influenzaVaccineExpiry = influenzaVaccineExpiry;
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
