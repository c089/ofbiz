package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.entity.GenericValue;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PriceBuilder {
    private List<GenericValue> productPrices;

    PriceBuilder() {
        productPrices = new LinkedList<>();
    }

    List<GenericValue> build() {
        return this.productPrices;
    }

    PriceBuilder withPrice(String id, BigDecimal price) {
        GenericValue value = mock(GenericValue.class);
        when(value.getString("productPriceTypeId")).thenReturn(id);
        when(value.getBigDecimal("price")).thenReturn(price);
        productPrices.add(value);
        return this;
    }
}
