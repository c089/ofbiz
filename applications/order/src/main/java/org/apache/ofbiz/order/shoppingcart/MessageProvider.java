package org.apache.ofbiz.order.shoppingcart;

import java.util.Locale;

public interface MessageProvider {
    String getMessage(String resource, String name, Locale locale);
}
