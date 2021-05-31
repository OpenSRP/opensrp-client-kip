package org.smartregister.kip.fragment;

import org.smartregister.kip.configuration.AllClientsRegisterQueryProvider;
import org.smartregister.opd.utils.ConfigurationInstancesHelper;


public class AllClientsRegisterFragmentKip extends KipOpdRegisterFragment {

    public AllClientsRegisterFragmentKip() {
        super();
        setOpdRegisterQueryProvider(ConfigurationInstancesHelper.newInstance(AllClientsRegisterQueryProvider.class));
    }
}