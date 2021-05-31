package org.smartregister.kip.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.opensrp.api.domain.Address;
import org.opensrp.api.domain.Location;

import java.util.Map;
import java.util.Set;

public class KipLocation extends Location {

    private String uuid;
    private String parentUuid;

    public KipLocation() {
    }

    public KipLocation(String uuid, String parentUuid) {
        this.uuid = uuid;
        this.parentUuid = parentUuid;
    }

    public KipLocation(String locationId, String name, Address address, Location parentLocation, String uuid, String parentUuid) {
        super(locationId, name, address, parentLocation);
        this.uuid = uuid;
        this.parentUuid = parentUuid;
    }

    public KipLocation(String locationId, String name, Address address, Map<String, String> identifiers, Location parentLocation, Set<String> tags, Map<String, Object> attributes, String uuid, String parentUuid) {
        super(locationId, name, address, identifiers, parentLocation, tags, attributes);
        this.uuid = uuid;
        this.parentUuid = parentUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
