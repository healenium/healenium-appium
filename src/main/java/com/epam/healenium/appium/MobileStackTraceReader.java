package com.epam.healenium.appium;

import com.epam.healenium.utils.StackTraceReader;

import java.util.Arrays;
import java.util.List;

public class MobileStackTraceReader extends StackTraceReader {

    @Override
    public List<String> getProxyHandlerNames() {
        return Arrays.asList(ProxyMethodHandler.class.getName());
    }
}
