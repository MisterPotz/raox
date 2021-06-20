package ru.bmstu.rk9.rao.lib.simulator;

import java.util.HashMap;

public class SimulatorPreinitializationArguments {
    private final SimulatorPreinitializationInfo preinitializationInfo;
    private final HashMap<String, Double> varConstArgs;

    private SimulatorPreinitializationArguments(SimulatorPreinitializationInfo info, HashMap<String, Double> args) {
        preinitializationInfo = info;
        varConstArgs = args;
    }

    public SimulatorPreinitializationInfo getPreinitializationInfo() {
        return preinitializationInfo;
    }

    public HashMap<String, Double> getVarConstArgs() {
        return varConstArgs;
    }

    public static SimulatorPreinitializationArguments.Builder preinitializationArgs() {
        return new Builder();
    }

    public static class Builder {
        private SimulatorPreinitializationInfo preinitializationInfo;
        private HashMap<String, Double> varConstArgs = null;

        private Builder() {

        }

        public Builder setPreinitializationInfo(SimulatorPreinitializationInfo preinitializationInfo) {
            this.preinitializationInfo = preinitializationInfo;
            return this;
        }

        public Builder setVarConstArgs(HashMap<String, Double> varConstArgs) {
            this.varConstArgs = varConstArgs;
            return this;
        }

        public SimulatorPreinitializationArguments build() {
            return new SimulatorPreinitializationArguments(preinitializationInfo, varConstArgs);
        }
    }
}
