package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.base.util.UtilProperties;

import java.util.Locale;

public class LocalizingMessageProvider implements MessageProvider {
    private final Locale locale;

    public LocalizingMessageProvider(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String getMessage(String resource, String name) {
        return UtilProperties.getMessage(resource, name, locale);
    }
}
