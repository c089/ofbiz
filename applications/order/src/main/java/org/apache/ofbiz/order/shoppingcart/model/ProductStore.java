package org.apache.ofbiz.order.shoppingcart.model;

public class ProductStore {
    private boolean viewCartOnAdd;
    private String payToPartyId;

    public ProductStore(boolean viewCartOnAdd, String payToPartyId, String inventoryFacilityId) {
        this.viewCartOnAdd = viewCartOnAdd;
        this.payToPartyId = payToPartyId;
        this.inventoryFacilityId = inventoryFacilityId;
    }

    private String inventoryFacilityId;

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
