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
                        .withPrices(new PriceBuilder()
                                .withPrice("SPECIAL_PROMO_PRICE", BigDecimal.valueOf(5)))
                        .thenExpect(BigDecimal.valueOf(4)),
                // should use PROMO
                test()
                        .withMinimumOrderPrice(BigDecimal.valueOf(20))
                        .withPrices(new PriceBuilder()
                                .withPrice("SPECIAL_PROMO_PRICE", null)
                                .withPrice("PROMO_PRICE", BigDecimal.valueOf(5))
                        )
                        .thenExpect(BigDecimal.valueOf(4)),
                // should prefer SPECIAL_PROMO
                test()
                        .withMinimumOrderPrice(BigDecimal.valueOf(20))
                        .withPrices(new PriceBuilder()
                                .withPrice("SPECIAL_PROMO_PRICE", BigDecimal.valueOf(15))
                                .withPrice("PROMO_PRICE", BigDecimal.valueOf(5))
                        )
                        .thenExpect(BigDecimal.valueOf(2)),
                // should prefer PROMO_PRICE over DEFAULT_PRICE
                test()
                        .withMinimumOrderPrice(BigDecimal.valueOf(10))
                        .withPrices(new PriceBuilder()
                                .withPrice("DEFAULT_PRICE", BigDecimal.valueOf(15))
                                .withPrice("PROMO_PRICE", BigDecimal.valueOf(5))
                        )
                        .thenExpect(BigDecimal.valueOf(2)),
                // should prefer DEFAULT_PRICE over LIST_PRICE
                test()
                        .withMinimumOrderPrice(BigDecimal.valueOf(10))
                        .withPrices(new PriceBuilder()
                                .withPrice("LIST_PRICE", BigDecimal.valueOf(15))
                                .withPrice("DEFAULT_PRICE", BigDecimal.valueOf(5))
                        )
                        .thenExpect(BigDecimal.valueOf(2)),
                // should use LIST_PRICE if no other price was given
                test()
                        .withMinimumOrderPrice(BigDecimal.valueOf(10))
                        .withItemBasePrice(null)
                        .withPrices(new PriceBuilder()
                                .withPrice("LIST_PRICE", BigDecimal.valueOf(5))
                        )
                        .thenExpect(BigDecimal.valueOf(2))
        });
    }

    private static TestDataBuilder test() {
        return new TestDataBuilder();
    }

    private final BigDecimal minimumOrderPrice;
    private final BigDecimal itemBasePrice;
    private final BigDecimal expectedOrderQuantity;
    private PriceBuilder priceBuilder = new PriceBuilder();


    public ShoppingCartMinimumOrderQuantityTest(
            BigDecimal minimumOrderPrice,
            BigDecimal itemBasePrice,
            PriceBuilder priceBuilder,
            BigDecimal expectedOrderQuantity) {
        this.minimumOrderPrice = minimumOrderPrice;
        this.itemBasePrice = itemBasePrice;
        this.expectedOrderQuantity = expectedOrderQuantity;
        this.priceBuilder = priceBuilder;
    }

    @Test
    public void test_getMinimumQuantity() throws Exception {
        ShoppingCart cart = cart()
                .withMinimumOrderPriceListRepository(b -> b
                        .withPricesForAnyProduct(this.priceBuilder)
                        .withMinimumOrderPriceForAnyProduct(this.minimumOrderPrice)
                )
                .build();

        BigDecimal result = cart.getMinimumOrderQuantity(this.itemBasePrice, null);

        assertThat(result, is(this.expectedOrderQuantity));
    }
    static class TestDataBuilder {
        private BigDecimal basePrice;
        private BigDecimal minimumOrderPrice;

        private BigDecimal expectedOrderQuantity;
        private PriceBuilder priceBuilder = new PriceBuilder();

        public TestDataBuilder() {
        }

        public TestDataBuilder withPrices(PriceBuilder p) {
            priceBuilder = p;
            return this;
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

        public Object[] build() {
            return new Object[] {
                    this.minimumOrderPrice,
                    this.basePrice,
                    this.priceBuilder,
                    this.expectedOrderQuantity
            };
        }

    }

    private MinimumOrderPriceRepositoryBuilder minimumOrderPriceRepository() {
        return new MinimumOrderPriceRepositoryBuilder();
    }
}
