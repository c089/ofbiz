package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.base.util.UtilProperties;

import java.util.Locale;

public class DefaultMessageProvider implements MessageProvider {
    @Override
    public String getMessage(String resource, String name, Locale locale) {
        return UtilProperties.getMessage(resource, name, locale);
    }
}
