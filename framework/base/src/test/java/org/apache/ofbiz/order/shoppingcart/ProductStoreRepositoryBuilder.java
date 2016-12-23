package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.entity.GenericValue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductStoreRepositoryBuilder implements Builder<ShoppingCart.ProductStoreRepository> {
    private String requirePinCode = "N";

    public ProductStoreRepositoryBuilder requirePinCode() {
        this.requirePinCode = "Y";
        return this;
    }

    public Builder<ShoppingCart.ProductStoreRepository> doNotRequirePinCode() {
        this.requirePinCode = "N";
        return this;
    }

    @Override
    public ShoppingCart.ProductStoreRepository build() {
        return productStoreId -> {
            GenericValue value = mock(GenericValue.class);
            when(value.getString("requirePinCode")).thenReturn(this.requirePinCode);
            return value;
        };
    }
}
