package org.apache.ofbiz.order.shoppingcart.model;

import org.apache.ofbiz.entity.GenericValue;

public class GiftCertSettings {
    final private boolean isPinRequired;

    public GiftCertSettings(GenericValue value) {
        this.isPinRequired = "Y".equals(value.getString("requirePinCode"));
    }

    public boolean isPinRequired() {
        return this.isPinRequired;
    }
}
