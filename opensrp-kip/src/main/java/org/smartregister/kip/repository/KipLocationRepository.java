package org.smartregister.kip.repository;

import android.database.Cursor;

import org.opensrp.api.domain.Location;
import org.smartregister.repository.LocationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class KipLocationRepository extends LocationRepository {
    public static final String LOCATIONS_TABLE_NAME = "locations";
    public static final String ID_COLUMN = "_id";
    public static final String UUID_COLUMN = "uuid";
    public static final String NAME_COLUMN = "name";
    public static final String TAG_COLUMN = "geojson";
    public static final String PARENT_UUID_COLUMN = "parent_uuid";
    public static final String[] LOCATIONS_TABLE_COLUMNS = {ID_COLUMN, UUID_COLUMN, NAME_COLUMN, TAG_COLUMN, PARENT_UUID_COLUMN};

    public KipLocationRepository(KipRepository repository) {
        super(repository);
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
                Location location = new Location();
                location.setLocationId(cursor.getString(1));
                location.setName(cursor.getString(2));

                String tagString = cursor.getString(3);
                String[] tagsArray = tagString.split(":");
                Set<String> tags = new HashSet<>(Arrays.asList(tagsArray));
                location.setTags(tags);

                locations.add(location);

                cursor.moveToNext();
            }
        }
        return locations;
    }
}
