package org.smartregister.kip.interactors;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.smartregister.kip.widgets.KipDatePickerFactory;
import org.smartregister.kip.widgets.KipEditTextFactory;
import org.smartregister.kip.widgets.KipSpinnerFactory;

/**
 * Created by keyman on 11/04/2017.
 */
public class KipJsonFormInteractor extends JsonFormInteractor {

    private static final JsonFormInteractor INSTANCE = new KipJsonFormInteractor();

    private KipJsonFormInteractor() {
        super();
    }

    @Override
    protected void registerWidgets() {
        super.registerWidgets();
        map.put(JsonFormConstants.EDIT_TEXT, new KipEditTextFactory());
        map.put(JsonFormConstants.DATE_PICKER, new KipDatePickerFactory());
        map.put(JsonFormConstants.SPINNER, new KipSpinnerFactory());
//        map.put(JsonFormConstants.LABEL, new PathCalculateLabelFactory());
    }

    public static JsonFormInteractor getInstance() {
        return INSTANCE;
    }
}
