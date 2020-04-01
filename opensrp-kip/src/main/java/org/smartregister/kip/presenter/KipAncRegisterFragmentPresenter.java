package org.smartregister.kip.presenter;

import org.smartregister.anc.library.contract.RegisterFragmentContract;
import org.smartregister.anc.library.presenter.RegisterFragmentPresenter;
import org.smartregister.kip.model.AncRegisterFragmentModel;

public class KipAncRegisterFragmentPresenter extends RegisterFragmentPresenter {
    public KipAncRegisterFragmentPresenter(RegisterFragmentContract.View view, String viewConfigurationIdentifier) {
        super(view, viewConfigurationIdentifier);
        setModel(new AncRegisterFragmentModel());
    }

    @Override
    public void setModel(RegisterFragmentContract.Model model) {
        super.setModel(model);
    }
}
