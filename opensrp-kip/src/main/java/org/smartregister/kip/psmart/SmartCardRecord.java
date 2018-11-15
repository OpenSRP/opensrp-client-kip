package org.smartregister.kip.psmart;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by manu on 11/11/18.
 */

public class SmartCardRecord {
    private String type;
    private String personUuid;
    private String plainPayload;
    private String encryptedPayload;
    private boolean writtenToCard;
    private Date dateTimeLastWrittenToCard;
    private boolean syncedToServer;
    private Date dateTimeLastSyncedToServer;
    private String uuid;

    public SmartCardRecord() {
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPersonUuid() {
        return this.personUuid;
    }

    public void setPersonUuid(String personUuid) {
        this.personUuid = personUuid;
    }

    public String getPlainPayload() {
        return this.plainPayload;
    }

    public void setPlainPayload(String plainPayload) {
        this.plainPayload = plainPayload;
    }

    public String getEncryptedPayload() {
        return this.encryptedPayload;
    }

    public void setEncryptedPayload(String encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isWrittenToCard() {
        return this.writtenToCard;
    }

    public void setWrittenToCard(boolean writtenToCard) {
        this.writtenToCard = writtenToCard;
    }

    public Date getDateTimeLastWrittenToCard() {
        return this.dateTimeLastWrittenToCard;
    }

    public void setDateTimeLastWrittenToCard(Date dateTimeLastWrittenToCard) {
        this.dateTimeLastWrittenToCard = dateTimeLastWrittenToCard;
    }

    public boolean isSyncedToServer() {
        return this.syncedToServer;
    }

    public void setSyncedToServer(boolean syncedToServer) {
        this.syncedToServer = syncedToServer;
    }

    public Date getDateTimeLastSyncedToServer() {
        return this.dateTimeLastSyncedToServer;
    }

    public void setDateTimeLastSyncedToServer(Date dateTimeLastSyncedToServer) {
        this.dateTimeLastSyncedToServer = dateTimeLastSyncedToServer;
    }

    public boolean hasAllMandatoryFields() {
        return StringUtils.isNotBlank(this.personUuid) && StringUtils.isNotBlank(this.uuid);
    }
}
