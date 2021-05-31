package org.smartregister.kip.repository;

import org.smartregister.kip.dao.Covid19AdverseReactionFormDao;
import org.smartregister.kip.pojo.Covid19AdverseReactionForm;
import org.smartregister.repository.BaseRepository;

import java.util.List;

public class Covid19AdverseReactionFormRepository extends BaseRepository implements Covid19AdverseReactionFormDao {

    @Override
    public boolean saveOrUpdate(Covid19AdverseReactionForm covid19AdverseReactionForm) {
        return false;
    }

    @Override
    public boolean save(Covid19AdverseReactionForm covid19AdverseReactionForm) {
        return false;
    }

    @Override
    public Covid19AdverseReactionForm findOne(Covid19AdverseReactionForm covid19AdverseReactionForm) {
        return null;
    }

    @Override
    public boolean delete(Covid19AdverseReactionForm covid19AdverseReactionForm) {
        return false;
    }

    @Override
    public List<Covid19AdverseReactionForm> findAll() {
        return null;
    }
}
