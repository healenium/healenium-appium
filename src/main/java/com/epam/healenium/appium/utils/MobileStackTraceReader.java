package com.epam.healenium.appium.utils;

import com.epam.healenium.appium.handlers.proxy.MobileSelfHealingProxyInvocationHandler;
import com.epam.healenium.handlers.proxy.BaseHandler;
import com.epam.healenium.utils.StackTraceReader;

import java.util.Arrays;
import java.util.List;

public class MobileStackTraceReader extends StackTraceReader {

    @Override
    public List<String> getProxyHandlerNames() {
        return Arrays.asList(MobileSelfHealingProxyInvocationHandler.class.getName(), BaseHandler.class.getName());
    }
}
