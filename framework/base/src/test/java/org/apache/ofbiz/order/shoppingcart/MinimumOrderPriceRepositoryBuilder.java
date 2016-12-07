package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;

import java.math.BigDecimal;
import java.util.List;

class MinimumOrderPriceRepositoryBuilder {
    private BigDecimal minimumOrderPrice = BigDecimal.ZERO;
    private PriceBuilder priceBuilder;

    MinimumOrderPriceRepositoryBuilder withMinimumOrderPriceForAnyProduct(BigDecimal price) {
        this.minimumOrderPrice = price;
        return this;
    }

    public MinimumOrderPriceRepositoryBuilder withPricesForAnyProduct(PriceBuilder p) {
        priceBuilder = p;
        return this;
    }

    ShoppingCart.MinimumOrderPriceListRepository build() {
        return new ShoppingCart.MinimumOrderPriceListRepository() {
            private final BigDecimal minimumOrderPriceForAllProducts = minimumOrderPrice;

            @Override
            public BigDecimal getMinimumOrderPriceFor(String itemProductId) throws GenericEntityException {
                return minimumOrderPriceForAllProducts;
            }

            @Override
            public List<GenericValue> getPricesForProduct(String itemProductId) throws GenericEntityException {
                return priceBuilder.build();
            }
        };
    }
}
