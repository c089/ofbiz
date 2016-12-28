package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.order.shoppingcart.domain.ProductPrice;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;

class PriceBuilder {
    private List<ProductPrice> productPrices;

    PriceBuilder() {
        productPrices = new LinkedList<>();
    }

    List<ProductPrice> build() {
        return this.productPrices;
    }

    PriceBuilder withPrice(String id, BigDecimal price) {
        ProductPrice value = new ProductPrice(id, price);
        productPrices.add(value);
        return this;
    }
}
