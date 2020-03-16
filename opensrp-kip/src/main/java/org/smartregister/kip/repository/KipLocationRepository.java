package org.smartregister.kip.repository;

import android.database.Cursor;

import org.jetbrains.annotations.NotNull;
import org.opensrp.api.domain.Location;
import org.smartregister.repository.LocationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class KipLocationRepository extends LocationRepository {
    private static final String LOCATIONS_TABLE_NAME = "locations";
    private static final String ID_COLUMN = "_id";
    private static final String UUID_COLUMN = "uuid";
    private static final String NAME_COLUMN = "name";
    private static final String TAG_COLUMN = "geojson";
    private static final String PARENT_UUID_COLUMN = "parent_uuid";
    private static final String[] LOCATIONS_TABLE_COLUMNS = {ID_COLUMN, UUID_COLUMN, NAME_COLUMN, TAG_COLUMN, PARENT_UUID_COLUMN};

    public KipLocationRepository() {
        super();
    }

    public List<Location> getLocationsByTag(String tag) {
        List<Location> locations = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(LOCATIONS_TABLE_NAME, LOCATIONS_TABLE_COLUMNS, TAG_COLUMN + " = ?", new String[]{tag}, null, null, " 1 ASC")) {
            locations = readAll(cursor);
        } catch (Exception e) {
            Timber.e(e, " --> getLocationsByTag");
        }
        return locations;
    }

    private List<Location> readAll(Cursor cursor) {
        List<Location> locations = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            cursor.moveToFirst();
            while (cursor.getCount() > 0 && !cursor.isAfterLast()) {
                locations.add(getLocation(cursor));
                cursor.moveToNext();
            }
        }
        return locations;
    }

    @NotNull
    private Location getLocation(Cursor cursor) {
        Location location = new Location();
        location.setLocationId(cursor.getString(cursor.getColumnIndex(ID_COLUMN)));
        location.setName(cursor.getString(cursor.getColumnIndex(NAME_COLUMN)));
        location.setTags(getTags(cursor));
        return location;
    }

    @NotNull
    private Set<String> getTags(Cursor cursor) {
        String tagString = cursor.getString(cursor.getColumnIndex(TAG_COLUMN));
        String[] tagsArray = tagString.split(":");
        return new HashSet<>(Arrays.asList(tagsArray));
    }
}
