package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.entity.GenericValue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductStoreBuilder {

    private String payToPartyId;
    private String facilityId;
    private String viewCartOnAdd;

    ProductStoreBuilder withPayToPartyId(String payToPartyId) {
        this.payToPartyId = payToPartyId;
        return this;
    }

    GenericValue build() {
        GenericValue productStore = mock(GenericValue.class);
        when(productStore.getString("payToPartyId")).thenReturn(this.payToPartyId);
        when(productStore.getString("inventoryFacilityId")).thenReturn(this.facilityId);
        when(productStore.getString("viewCartOnAdd")).thenReturn(this.viewCartOnAdd);

        return productStore;
    }

    ProductStoreBuilder withFacilityId(String facilityId) {
        this.facilityId = facilityId;
        return this;
    }

    public ProductStoreBuilder withViewCartOnAdd(String viewCartOnAdd) {
        this.viewCartOnAdd = viewCartOnAdd;
        return this;
    }
}
