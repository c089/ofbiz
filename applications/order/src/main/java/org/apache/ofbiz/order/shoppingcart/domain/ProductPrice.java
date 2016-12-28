package org.apache.ofbiz.order.shoppingcart.domain;

import java.math.BigDecimal;

public class ProductPrice {
    final String productPriceTypeId;
    final BigDecimal price;

    public ProductPrice(String productPriceTypeId, BigDecimal price) {
        this.productPriceTypeId = productPriceTypeId;
        this.price = price;
    }

    public String getProductPriceTypeId() {
        return productPriceTypeId;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
