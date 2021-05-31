package org.smartregister.kip.fragment;

import org.smartregister.child.fragment.BaseChildRegistrationDataFragment;
import org.smartregister.kip.util.KipConstants;

import java.util.Collections;
import java.util.List;

/**
 * Created by ndegwamartin on 2019-05-30.
 */
public class ChildRegistrationDataFragment extends BaseChildRegistrationDataFragment {


    @Override
    public String getRegistrationForm() {
        return KipConstants.JSON_FORM.CHILD_ENROLLMENT;
    }

    @Override
    protected List<String> addUnFormattedNumberFields(String... key) {
        return Collections.singletonList("mother_guardian_number");
    }
}
