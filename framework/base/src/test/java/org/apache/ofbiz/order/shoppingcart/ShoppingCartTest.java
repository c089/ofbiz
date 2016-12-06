package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class ShoppingCartTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void initializesEmptyCart() throws Exception {
        ShoppingCart cart = cart().build();

        assertThat(cart.getItemTotal(), is(BigDecimal.valueOf(0)));
    }

    @Test
    public void refusesToInitializeForUnknownProductStoreId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unable to locate ProductStore by ID [fooStore]");

        cart()
                .withProductStoreId("fooStore")
                .withProductStore(null)
                .build();
    }

    @Test
    public void fallsBackToDefaultLocale() {
        ShoppingCart cart = cart()
                .withLocale(null)
                .withDefaultLocale(Locale.CANADA)
                .build();
        assertThat(cart.getLocale(), is(Locale.CANADA));
    }

    @Test
    public void usesGivenLocale() {
        ShoppingCart cart = cart()
                .withLocale(Locale.US)
                .withDefaultLocale(Locale.CANADA)
                .build();

        assertThat(cart.getLocale(), is(Locale.US));
    }

    @Test
    public void usesGivenCurrency() {
        ShoppingCart cart = cart()
                .withCurrency("EUR")
                .build();

        Map<String, Object> stringObjectMap = cart.makeCartMap(mock(LocalDispatcher.class), false);

        assertThat(stringObjectMap.get("currencyUom"), is("EUR"));
    }

    @Test
    public void fallsBackToCurrencyFromEntityUtilProperties() {
        ShoppingCart cart = cart()
                .withCurrency(null)
                .withDefaultCurrency("EUR")
                .build();

        Map<String, Object> cartMap = cart.makeCartMap(mock(LocalDispatcher.class), false);

        assertThat(cartMap.get("currencyUom"), is("EUR"));
    }

    @Test
    public void viewCartOnAdd_defaults_to_false() {
        ShoppingCart cart = cart().build();

        assertThat(cart.viewCartOnAdd(), is(false));
    }

    @Test
    public void viewCartOnAdd_is_set_to_true_when_Y_on_ProductStore() {
        GenericValue productStore = mock(GenericValue.class);
        when(productStore.getString("viewCartOnAdd")).thenReturn("Y");

        ShoppingCart cart = cart()
                .withProductStore(productStore)
                .build();

        assertThat(cart.viewCartOnAdd(), is(true));
    }

    @Test
    public void viewCartOnAdd_is_set_to_true_when_lowercase_y_on_ProductStore() {
        GenericValue productStore = mock(GenericValue.class);
        when(productStore.getString("viewCartOnAdd")).thenReturn("y");

        ShoppingCart cart = cart()
                .withProductStore(productStore)
                .build();

        assertThat(cart.viewCartOnAdd(), is(true));
    }

    @Test
    public void should_use_billFromVendorPartyId_from_ProductStore() throws Exception {
        GenericValue productStore = productStore()
                .withPayToPartyId("the party id")
                .build();

        ShoppingCart cart = cart()
                .withBillFromVendorPartyId(null)
                .withProductStore(productStore)
                .build();

        assertThat(cart.getBillFromVendorPartyId(), is("the party id"));
    }

    @Test
    public void should_prefer_given_billFromVendorPartyId_over_ProductStore() throws Exception {
        GenericValue productStore = productStore()
                .withPayToPartyId("product store party id")
                .build();

        ShoppingCart cart = cart()
                .withProductStore(productStore)
                .withBillFromVendorPartyId("other party id")
                .build();

        assertThat(cart.getBillFromVendorPartyId(), is("other party id"));

    }

    @Test
    public void should_use_facilityId_from_ProductStore() {
        GenericValue productStore = productStore()
                .withFacilityId("my facility")
                .build();

        ShoppingCart cart = cart()
                .withProductStore(productStore)
                .build();

        assertThat(cart.getFacilityId(), is("my facility"));
    }

    @Test
    public void should_refuse_to_add_item_to_readonly_cart() throws CartItemModifyException {
        thrown.expect(CartItemModifyException.class);
        thrown.expectMessage("Cart items cannot be changed");

        ShoppingCart cart = cart()
                .readOnly()
                .build();

        cart.addItem(0, new ShoppingCartItem());
    }

    @Test
    public void should_add_first_cart_item() throws Exception {
        ShoppingCart cart = cart()
                .build();

        int result = cart.addItem(0, new ShoppingCartItem());

        assertThat(result, is(0));
    }

    @Test
    public void should_not_add_the_same_item_twice() throws Exception {
        ShoppingCartItem item = new ShoppingCartItem();
        ShoppingCart cart = cart()
                .build();
        cart.addItem(0, item);

        int result2 = cart.addItem(1, item);

        assertThat(result2, is(0));
        assertThat(cart.items(), is(Arrays.asList(item)));
    }

    @Test
    public void getItemsProducts_should_return_list_of_products() throws Exception {
        GenericValue product1 = mock(GenericValue.class);
        ShoppingCartItem item1 = mock(ShoppingCartItem.class);
        when(item1.getProduct()).thenReturn(product1);

        GenericValue product2 = mock(GenericValue.class);
        ShoppingCartItem item2 = mock(ShoppingCartItem.class);
        when(item2.getProduct()).thenReturn(product2);

        ShoppingCartItem item3 = mock(ShoppingCartItem.class);
        when(item3.getProduct()).thenReturn(null);

        List<ShoppingCartItem> items = Arrays.asList(item1, item2);

        assertThat(ShoppingCart.getItemsProducts(items), is(Arrays.asList(product1, product2)));
    }

    ShoppingCart.ProductStoreRepository productStoreRepository = new ShoppingCart.ProductStoreRepository() {
        @Override
        public GenericValue giftCertSettings(String s) throws GenericEntityException {
            throw new GenericEntityException("my message");
        }
    };

    @Test
    public void isPinRequiredForGC_should_be_true_when_gift_cert_settings_cannot_be_loaded() throws Exception {
        ShoppingCart cart = cart()
                .withProductStoreRepository(productStoreRepository)
                .build();

        boolean pinRequiredForGC = cart.isPinRequiredForGC(mock(Delegator.class));
        assertThat(pinRequiredForGC, is(true));
    }

    @Test
    public void isPinRequiredForGC_should_log_error_when_gift_cert_settings_cannot_be_loaded() throws Exception {
        ShoppingCart.Logger logger = mock(ShoppingCart.Logger.class);

        ShoppingCart cart = cart()
                .withLogger(logger)
                .withProductStoreRepository(productStoreRepository)
                .build();


        cart.isPinRequiredForGC(mock(Delegator.class));

        verify(logger).logError("Error checking if store requires pin number for GC: my message", ShoppingCart.module);
    }

    @Test
    public void isPinRequiredForGC_should_be_true_when_GiftCertSettings_requirePinCode_is_set_to_Y() throws Exception {
        ShoppingCart.ProductStoreRepository repository = new ShoppingCart.ProductStoreRepository() {
            @Override
            public GenericValue giftCertSettings(String productStoreId) throws GenericEntityException {
                GenericValue value = mock(GenericValue.class);
                when(value.getString("requirePinCode")).thenReturn("Y");
                return value;
            }
        };

        ShoppingCart cart = cart()
                .withProductStoreRepository(repository)
                .build();

        assertThat(cart.isPinRequiredForGC(mock(Delegator.class)), is(true));
    }


    @Test
    public void isPinRequiredForGC_should_be_false_when_GiftCertSettings_requirePinCode_is_set_to_N() throws Exception {
        ShoppingCart.ProductStoreRepository repository = new ShoppingCart.ProductStoreRepository() {
            @Override
            public GenericValue giftCertSettings(String productStoreId) throws GenericEntityException {
                GenericValue value = mock(GenericValue.class);
                when(value.getString("requirePinCode")).thenReturn("N");
                return value;
            }
        };

        ShoppingCart cart = cart()
                .withProductStoreRepository(repository)
                .build();

        assertThat(cart.isPinRequiredForGC(mock(Delegator.class)), is(false));
    }

    @Test
    public void isPinRequiredForGC_should_warn_about_missing_giftCertSettings() throws Exception {
        ShoppingCart.Logger logger = mock(ShoppingCart.Logger.class);
        ShoppingCart.ProductStoreRepository repository = new ShoppingCart.ProductStoreRepository() {
            @Override
            public GenericValue giftCertSettings(String productStoreId) throws GenericEntityException {
                return null;
            }
        };

        ShoppingCart cart = cart()
                .withLogger(logger)
                .withProductStoreRepository(repository)
                .build();

        boolean pinRequiredForGC = cart.isPinRequiredForGC(cart.getDelegator());

        verify(logger).logWarning("No product store gift certificate settings found for store [my_store]", ShoppingCart.module);
        assertThat(pinRequiredForGC, is(true));
    }

    static class PriceBuilder {
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

    static class MinimumOrderPriceRepositoryBuilder {
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


    private ProductStoreBuilder productStore() {
        return new ProductStoreBuilder();
    }

    public ShoppingCartBuilder cart() {
        return new ShoppingCartBuilder();
    }

    private class ProductStoreBuilder {

        private String payToPartyId;
        private String facilityId;

        ProductStoreBuilder withPayToPartyId(String payToPartyId) {
            this.payToPartyId = payToPartyId;
            return this;
        }

        GenericValue build() {
            GenericValue productStore = mock(GenericValue.class);
            when(productStore.getString("payToPartyId")).thenReturn(this.payToPartyId);
            when(productStore.getString("inventoryFacilityId")).thenReturn(this.facilityId);
            return productStore;
        }

        public ProductStoreBuilder withFacilityId(String facilityId) {
            this.facilityId = facilityId;
            return this;
        }
    }


}

