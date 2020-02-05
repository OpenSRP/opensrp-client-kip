package org.smartregister.kip.interactor;

import net.sqlcipher.Cursor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.smartregister.Context;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.util.KipConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KipApplication.class)
public class NavigationInteractorTest {

    @Mock
    private KipApplication gizMalawiApplication;

    @Mock
    private Context context;

    @Mock
    private CommonRepository commonRepository;

    private NavigationInteractor navigationInteractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        navigationInteractor = new NavigationInteractor();
    }

    @Test
    public void getCountForChildRegisterCountIfResultIsOne() throws Exception {
        PowerMockito.mockStatic(KipApplication.class);
        PowerMockito.when(gizMalawiApplication.getContext()).thenReturn(context);
        PowerMockito.when(context.commonrepository(KipConstants.TABLE_NAME.CHILD)).thenReturn(commonRepository);
        Cursor cursor = Mockito.mock(Cursor.class);
        PowerMockito.when(cursor.moveToFirst()).thenReturn(true);
        PowerMockito.when(cursor.getInt(0)).thenReturn(1);
        PowerMockito.when(commonRepository.rawCustomQueryForAdapter("select count(*) from ec_child  where date_removed is null AND  ((( julianday('now') - julianday(dob))/365.25) <5);")).thenReturn(cursor);
        PowerMockito.when(KipApplication.getInstance()).thenReturn(gizMalawiApplication);
        int result = Whitebox.invokeMethod(navigationInteractor, "getCount", KipConstants.TABLE_NAME.CHILD);
        Assert.assertEquals(1, result);
    }

}