package org.smartregister.kip.pojo;

public class OpdCovid19VaccinationForm {
    private String id;
    private String baseEntityId;
    private String visitId;
    private String covid19Antigens;
    private String siteOfAdministration;
    private String administrationDate;
    private String administrationRoute;
    private String lotNumber;
    private String vaccineExpiry;
    private String age;
    private String date;
    private String createdAt;

    public OpdCovid19VaccinationForm() {
    }

    public OpdCovid19VaccinationForm(String id, String baseEntityId, String visitId, String covid19Antigens, String siteOfAdministration, String administrationDate, String administrationRoute, String lotNumber, String vaccineExpiry, String age, String date, String createdAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.visitId = visitId;
        this.covid19Antigens = covid19Antigens;
        this.siteOfAdministration = siteOfAdministration;
        this.administrationDate = administrationDate;
        this.administrationRoute = administrationRoute;
        this.lotNumber = lotNumber;
        this.vaccineExpiry = vaccineExpiry;
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

    public String getCovid19Antigens() {
        return covid19Antigens;
    }

    public void setCovid19Antigens(String covid19Antigens) {
        this.covid19Antigens = covid19Antigens;
    }

    public String getSiteOfAdministration() {
        return siteOfAdministration;
    }

    public void setSiteOfAdministration(String siteOfAdministration) {
        this.siteOfAdministration = siteOfAdministration;
    }

    public String getAdministrationDate() {
        return administrationDate;
    }

    public void setAdministrationDate(String administrationDate) {
        this.administrationDate = administrationDate;
    }

    public String getAdministrationRoute() {
        return administrationRoute;
    }

    public void setAdministrationRoute(String administrationRoute) {
        this.administrationRoute = administrationRoute;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getVaccineExpiry() {
        return vaccineExpiry;
    }

    public void setVaccineExpiry(String vaccineExpiry) {
        this.vaccineExpiry = vaccineExpiry;
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
