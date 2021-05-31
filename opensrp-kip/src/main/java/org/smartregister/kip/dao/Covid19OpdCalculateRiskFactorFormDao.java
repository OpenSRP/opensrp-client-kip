package org.smartregister.kip.dao;

import org.smartregister.kip.pojo.OpdCovid19CalculateRiskFactorForm;
import org.smartregister.opd.dao.OpdGenericDao;

import java.util.List;

public interface Covid19OpdCalculateRiskFactorFormDao extends OpdGenericDao<OpdCovid19CalculateRiskFactorForm> {
    List<OpdCovid19CalculateRiskFactorForm> findAll(OpdCovid19CalculateRiskFactorForm opdCovid19CalculateRiskFactorForm);
}
