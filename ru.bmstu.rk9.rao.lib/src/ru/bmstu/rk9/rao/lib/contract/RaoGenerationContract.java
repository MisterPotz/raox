package ru.bmstu.rk9.rao.lib.contract;

import java.util.HashMap;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public class RaoGenerationContract {
    public static final String INITIALIZATION_SCOPE_CLASS = "InitializationScope";
    public static final String INITIALIZATION_SCOPE_FIELD = "initializationScope";
    public static final Class<?> SIMULATOR_ID_CLASS = SimulatorId.class;
    public static final Class<?> VARCONST_VALUES_CLASS = HashMap.class;
    public static final Class<?> VARCONST_VALUES_KEY = String.class;
    public static final Class<?> VARCONST_VALUES_VALUE = Double.class;
    public static final String VARCONST_VALUES_NAME = "varConstValues";

    private RaoGenerationContract() {}
}
