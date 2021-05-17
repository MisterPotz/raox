package ru.bmstu.rk9.rao.jvmmodel;

import java.util.List;

interface ProxyBuilderHelpersStorage {
    void addNewProxyBuilder(ProxyBuilderHelper newBuilder);
    List<ProxyBuilderHelper> getCollectedProxyBuilders();
}