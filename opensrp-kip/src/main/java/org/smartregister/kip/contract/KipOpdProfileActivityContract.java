package org.smartregister.kip.contract;

import org.smartregister.opd.contract.OpdProfileActivityContract;

import java.util.Map;

public interface KipOpdProfileActivityContract extends OpdProfileActivityContract {
    interface View extends OpdProfileActivityContract.View {
        void updateVaccineStockForm(Map<String, String> vaccineStock);

        String getVaccineDispenseForm();

        void startForm(String formName);
    }
}
