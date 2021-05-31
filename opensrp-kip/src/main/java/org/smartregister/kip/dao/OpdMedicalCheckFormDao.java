package org.smartregister.kip.dao;

import org.smartregister.kip.pojo.OpdMedicalCheckForm;
import org.smartregister.opd.dao.OpdGenericDao;

import java.util.List;

public interface OpdMedicalCheckFormDao extends OpdGenericDao<OpdMedicalCheckForm> {
    List<OpdMedicalCheckForm> findAll(OpdMedicalCheckForm opdMedicalCheckForm);
}
