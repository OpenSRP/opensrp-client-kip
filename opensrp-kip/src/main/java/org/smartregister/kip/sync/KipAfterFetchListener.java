package org.smartregister.kip.sync;

import org.smartregister.domain.FetchStatus;
import org.smartregister.sync.AfterFetchListener;

import static org.smartregister.event.Event.ON_DATA_FETCHED;

public class KipAfterFetchListener implements AfterFetchListener {

    @Override
    public void afterFetch(FetchStatus fetchStatus) {
    }

    public void partialFetch(FetchStatus fetchStatus) {
        ON_DATA_FETCHED.notifyListeners(fetchStatus);
    }
}
