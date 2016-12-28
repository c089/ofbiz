package org.apache.ofbiz.order.shoppingcart.model;

import org.apache.ofbiz.entity.GenericValue;

public class ProductStore {
    private GenericValue productStore;
    private boolean viewCartOnAdd;
    private String payToPartyId;
    private String inventoryFacilityId;

    public ProductStore(GenericValue productStore) {
        String storeViewCartOnAdd = productStore.getString("viewCartOnAdd");
        viewCartOnAdd = storeViewCartOnAdd != null && "Y".equalsIgnoreCase(storeViewCartOnAdd);

        payToPartyId = productStore.getString("payToPartyId");
        inventoryFacilityId = productStore.getString("inventoryFacilityId");
    }

    public boolean viewCartOnAdd() {
        return viewCartOnAdd;
    }

    public String payToPartId() {
        return payToPartyId;
    }

    public String inventoryFacilityId() {
        return inventoryFacilityId;
    }
}
