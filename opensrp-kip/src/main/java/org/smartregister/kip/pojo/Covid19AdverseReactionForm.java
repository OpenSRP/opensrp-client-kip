package org.smartregister.kip.pojo;

public class Covid19AdverseReactionForm {

    private String id;
    private String baseEntityId;
    private String Reaction_Vaccine;
    private String aefi_start_date;
    private String mild_reaction;
    private String other_mild_reaction;
    private String severe_reaction;
    private String other_severe_reaction;
    private String age;
    private String date;
    private String createdAt;

    public Covid19AdverseReactionForm() {
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

    public String getReaction_Vaccine() {
        return Reaction_Vaccine;
    }

    public void setReaction_Vaccine(String reaction_Vaccine) {
        Reaction_Vaccine = reaction_Vaccine;
    }

    public String getAefi_start_date() {
        return aefi_start_date;
    }

    public void setAefi_start_date(String aefi_start_date) {
        this.aefi_start_date = aefi_start_date;
    }

    public String getMild_reaction() {
        return mild_reaction;
    }

    public void setMild_reaction(String mild_reaction) {
        this.mild_reaction = mild_reaction;
    }

    public String getOther_mild_reaction() {
        return other_mild_reaction;
    }

    public void setOther_mild_reaction(String other_mild_reaction) {
        this.other_mild_reaction = other_mild_reaction;
    }

    public String getSevere_reaction() {
        return severe_reaction;
    }

    public void setSevere_reaction(String severe_reaction) {
        this.severe_reaction = severe_reaction;
    }

    public String getOther_severe_reaction() {
        return other_severe_reaction;
    }

    public void setOther_severe_reaction(String other_severe_reaction) {
        this.other_severe_reaction = other_severe_reaction;
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

    public Covid19AdverseReactionForm(String id, String baseEntityId, String reaction_Vaccine, String aefi_start_date, String mild_reaction, String other_mild_reaction, String severe_reaction, String other_severe_reaction, String age, String date, String createdAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        Reaction_Vaccine = reaction_Vaccine;
        this.aefi_start_date = aefi_start_date;
        this.mild_reaction = mild_reaction;
        this.other_mild_reaction = other_mild_reaction;
        this.severe_reaction = severe_reaction;
        this.other_severe_reaction = other_severe_reaction;
        this.age = age;
        this.date = date;
        this.createdAt = createdAt;
    }


}
