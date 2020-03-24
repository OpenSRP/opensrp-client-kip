package org.smartregister.kip.util;

import org.smartregister.child.util.Constants;
import org.smartregister.domain.AlertStatus;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.util.VaccinateActionUtils;

import java.util.List;

/**
 * Created by ndegwamartin on 2019-05-30.
 */

public class DBQueryHelper {

    public static final String getHomeRegisterCondition() {
        return KipConstants.TABLE_NAME.CHILD + "." + Constants.KEY.DATE_REMOVED + " IS NULL ";
    }

    public static String getFilterSelectionCondition(boolean urgentOnly) {

        final String AND = " AND ";
        final String OR = " OR ";
        final String IS_NULL_OR = " IS NULL OR ";
        final String TRUE = "'true'";

        StringBuilder mainCondition = new StringBuilder(" ( " + Constants.KEY.DOD + " is NULL OR " + Constants.KEY.DOD + " = '' ) " +
                AND + " (" + Constants.CHILD_STATUS.INACTIVE + IS_NULL_OR + Constants.CHILD_STATUS.INACTIVE + " != " + TRUE + " ) " +
                AND + " (" + Constants.CHILD_STATUS.LOST_TO_FOLLOW_UP + IS_NULL_OR + Constants.CHILD_STATUS.LOST_TO_FOLLOW_UP + " != " + TRUE + " ) " +
                AND + " ( ");
        List<VaccineRepo.Vaccine> vaccines = ImmunizationLibrary.getInstance().getVaccineCacheMap().get(Constants.CHILD_TYPE).vaccineRepo;

        vaccines.remove(VaccineRepo.Vaccine.bcg2);
        vaccines.remove(VaccineRepo.Vaccine.ipv);
        vaccines.remove(VaccineRepo.Vaccine.opv0);
        vaccines.remove(VaccineRepo.Vaccine.opv4);
        vaccines.remove(VaccineRepo.Vaccine.measles1);
        vaccines.remove(VaccineRepo.Vaccine.mr1);
        vaccines.remove(VaccineRepo.Vaccine.rtss1);
        vaccines.remove(VaccineRepo.Vaccine.rtss2);
        vaccines.remove(VaccineRepo.Vaccine.rtss3);
        vaccines.remove(VaccineRepo.Vaccine.rtss4);
        vaccines.remove(VaccineRepo.Vaccine.measles2);
        vaccines.remove(VaccineRepo.Vaccine.mr2);
        vaccines.remove(VaccineRepo.Vaccine.mv1);
        vaccines.remove(VaccineRepo.Vaccine.mv2);
        vaccines.remove(VaccineRepo.Vaccine.mv3);
        vaccines.remove(VaccineRepo.Vaccine.mv4);

        final String URGENT = "'" + AlertStatus.urgent.value() + "'";
        final String NORMAL = "'" + AlertStatus.normal.value() + "'";
        final String COMPLETE = "'" + AlertStatus.complete.value() + "'";


        for (int i = 0; i < vaccines.size(); i++) {
            VaccineRepo.Vaccine vaccine = vaccines.get(i);
            if (i == vaccines.size() - 1) {
                mainCondition.append(" ").append(VaccinateActionUtils.addHyphen(vaccine.display())).append(" = ").append(URGENT).append(" ");
            } else {
                mainCondition.append(" ").append(VaccinateActionUtils.addHyphen(vaccine.display())).append(" = ").append(URGENT).append(OR);
            }
        }

        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.opv0.display())).append(" = ").append(URGENT).append(" ) ");
        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.opv0.display())).append(" != ").append(COMPLETE).append(" ) ");

        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.mr1.display())).append(" != ").append(COMPLETE).append(" ) ");
        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.mr1.display())).append(" = ").append(URGENT).append(" ) ");

        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.mr2.display())).append(" != ").append(COMPLETE).append(" ) ");
        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.mr2.display())).append(" = ").append(URGENT).append(" ) ");

        if (urgentOnly) {
            return mainCondition + " ) ";
        }

        mainCondition.append(OR);
        for (int i = 0; i < vaccines.size(); i++) {
            VaccineRepo.Vaccine vaccine = vaccines.get(i);
            if (i == vaccines.size() - 1) {
                mainCondition.append(" ").append(VaccinateActionUtils.addHyphen(vaccine.display())).append(" = ").append(NORMAL).append(" ");
            } else {
                mainCondition.append(" ").append(VaccinateActionUtils.addHyphen(vaccine.display())).append(" = ").append(NORMAL).append(OR);
            }
        }

        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.opv0.display())).append(" = ").append(NORMAL).append(" ) ");
        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.opv0.display())).append(" != ").append(COMPLETE).append(" ) ");

        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.mr1.display())).append(" != ").append(COMPLETE).append(" ) ");
        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.mr1.display())).append(" = ").append(NORMAL).append(" ) ");

        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.mr2.display())).append(" != ").append(COMPLETE).append(" ) ");
        mainCondition.append(OR + " ( ").append(VaccinateActionUtils.addHyphen(VaccineRepo.Vaccine.mr2.display())).append(" = ").append(NORMAL).append(" ) ");

        return mainCondition + " ) ";
    }

    public static String getSortQuery() {
        return KipConstants.KEY.LAST_INTERACTED_WITH + " DESC ";
    }
}
