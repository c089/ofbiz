package org.apache.ofbiz.order.shoppingcart;

import org.junit.Test;

import java.math.BigDecimal;

import static org.apache.ofbiz.order.shoppingcart.ShoppingCartBuilder.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ShoppingCartMinimumOrderQuantityTest {
    @Test
    public void getMinimumOrderQuantity_should_return_zero_without_itemBasePrice_and_minimumOrderPrice_() throws Exception {
        ShoppingCart cart = cart()
                .withMinimumOrderPriceListRepository(minimumOrderPriceRepository()
                        .withMinimumOrderPriceForAnyProduct(null)
                        .build())
                .build();

        BigDecimal result = cart.getMinimumOrderQuantity(null, null);

        assertThat(result, is(BigDecimal.valueOf(0)));
    }

    @Test
    public void getMinimumOrderQuantity_should_return_zero_when_MinimumOrderPrice_for_product_is_zero() throws Exception {
        ShoppingCart cart = cart()
                .withMinimumOrderPriceListRepository(
                        minimumOrderPriceRepository()
                                .withMinimumOrderPriceForAnyProduct(BigDecimal.valueOf(0))
                                .build())
                .build();

        BigDecimal result = cart.getMinimumOrderQuantity(null, null);

        assertThat(result, is(BigDecimal.valueOf(0)));
    }

    private ShoppingCartTest.MinimumOrderPriceRepositoryBuilder minimumOrderPriceRepository() {
        return new ShoppingCartTest.MinimumOrderPriceRepositoryBuilder();
    }

    @Test
    public void getMinimumOrderQuantity_should_return_MinimumOrderPrice_divided_by_given_itemBasePrice() throws Exception {
        BigDecimal minimumOrderPrice = BigDecimal.valueOf(20);
        BigDecimal itemBasePrice = BigDecimal.valueOf(10);
        ShoppingCart cart = cart()
                .withMinimumOrderPriceListRepository(minimumOrderPriceRepository()
                        .withMinimumOrderPriceForAnyProduct(minimumOrderPrice)
                        .build())
                .build();

        BigDecimal result = cart.getMinimumOrderQuantity(itemBasePrice, "foo");
        assertThat(result, is(BigDecimal.valueOf(2)));
    }

    @Test
    public void getMinimumOrderQuantity_should_round_quantity_up() throws Exception {
        BigDecimal minimumOrderPrice = BigDecimal.valueOf(20);
        BigDecimal itemBasePrice = BigDecimal.valueOf(15);
        ShoppingCart cart = cart()
                .withMinimumOrderPriceListRepository(minimumOrderPriceRepository()
                        .withMinimumOrderPriceForAnyProduct(minimumOrderPrice)
                        .build())
                .build();

        BigDecimal result = cart.getMinimumOrderQuantity(itemBasePrice, "foo");
        assertThat(result, is(BigDecimal.valueOf(2)));
    }

    @Test
    public void getMinimumOrderQuantity_uses_SPECIAL_PROMO_PRICE_if_no_itemBasePrice_given() throws Exception {
        final BigDecimal minimumOrderPrice = BigDecimal.valueOf(20);
        final BigDecimal itemBasePrice = null;
        final BigDecimal specialPromoPrice = BigDecimal.valueOf(5);
        final BigDecimal expectedOrderQuantity = BigDecimal.valueOf(4);

        ShoppingCart.MinimumOrderPriceListRepository orderPriceListRepository = minimumOrderPriceRepository()
                .withMinimumOrderPriceForAnyProduct(minimumOrderPrice)
                .withSpecialPromoPriceForAnyProduct(specialPromoPrice)
                .build();
        ShoppingCart cart = cart()
                .withMinimumOrderPriceListRepository(orderPriceListRepository)
                .build();

        BigDecimal result = cart.getMinimumOrderQuantity(itemBasePrice, "foo");
        assertThat(result, is(expectedOrderQuantity));
    }
}
