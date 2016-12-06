package org.apache.ofbiz.order.shoppingcart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.apache.ofbiz.order.shoppingcart.ShoppingCartBuilder.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
public class ShoppingCartMinimumOrderQuantityTest {
    @Parameterized.Parameters
    public static List data() {
        return Arrays.asList(new Object[][] {
                // should be 0 when MinimumOrderPrice and baseItemPrice are not given
                test()
                        .withItemBasePrice(null)
                        .withMinimumOrderPrice(null)
                        .thenExpect(BigDecimal.ZERO),
                // should be 0 when minimum order price is 0
                test()
                        .withMinimumOrderPrice(BigDecimal.ZERO)
                        .withItemBasePrice(null)
                        .thenExpect(BigDecimal.ZERO),
                // should use MinimumOrderPrice divided by given itemBasePrice
                test()
                        .withMinimumOrderPrice(BigDecimal.valueOf(20))
                        .withItemBasePrice(BigDecimal.valueOf(10))
                        .thenExpect(BigDecimal.valueOf(2)),
                // should round quantity up
                test()
                        .withMinimumOrderPrice(BigDecimal.valueOf(20))
                        .withItemBasePrice(BigDecimal.valueOf(15))
                        .thenExpect(BigDecimal.valueOf(2)),
                // should use SPECIAL_PROMO price if no itemBasePrice is given
                test()
                        .withMinimumOrderPrice(BigDecimal.valueOf(20))
                        .withSpecialPromoPrice(BigDecimal.valueOf(5))
                        .thenExpect(BigDecimal.valueOf(4))
        });
    }

    private static TestDataBuilder test() {
        return new TestDataBuilder();
    }

    private final BigDecimal minimumOrderPrice;
    private final BigDecimal itemBasePrice;
    private final BigDecimal specialPromoPrice;

    private final BigDecimal expectedOrderQuantity;

    public ShoppingCartMinimumOrderQuantityTest(
            BigDecimal minimumOrderPrice,
            BigDecimal itemBasePrice,
            BigDecimal specialPromoPrice,
            BigDecimal expectedOrderQuantity) {
        this.minimumOrderPrice = minimumOrderPrice;
        this.itemBasePrice = itemBasePrice;
        this.specialPromoPrice = specialPromoPrice;
        this.expectedOrderQuantity = expectedOrderQuantity;
    }

    @Test
    public void test_getMinimumQuantity() throws Exception {
        ShoppingCart.MinimumOrderPriceListRepository priceListRepository = minimumOrderPriceRepository()
                .withMinimumOrderPriceForAnyProduct(this.minimumOrderPrice)
                .withSpecialPromoPriceForAnyProduct(this.specialPromoPrice)
                .build();
        ShoppingCart cart = cart()
                .withMinimumOrderPriceListRepository(priceListRepository)
                .build();

        BigDecimal result = cart.getMinimumOrderQuantity(this.itemBasePrice, null);

        assertThat(result, is(this.expectedOrderQuantity));
    }
    static class TestDataBuilder {
        private BigDecimal basePrice;
        private BigDecimal minimumOrderPrice;
        private BigDecimal specialPromoPrice;

        private BigDecimal expectedOrderQuantity;

        public TestDataBuilder() {
        }

        public TestDataBuilder withItemBasePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public TestDataBuilder withMinimumOrderPrice(BigDecimal o) {
            this.minimumOrderPrice = o;
            return this;
        }

        public Object[] thenExpect(BigDecimal expected) {
            this.expectedOrderQuantity = expected;
            return this.build();
        }

        public TestDataBuilder withSpecialPromoPrice(BigDecimal specialPromoPrice) {
            this.specialPromoPrice = specialPromoPrice;
            return this;
        }
        public Object[] build() {
            return new Object[] {
                    this.minimumOrderPrice,
                    this.basePrice,
                    this.specialPromoPrice,
                    this.expectedOrderQuantity
            };
        }

    }

    private ShoppingCartTest.MinimumOrderPriceRepositoryBuilder minimumOrderPriceRepository() {
        return new ShoppingCartTest.MinimumOrderPriceRepositoryBuilder();
    }
}
