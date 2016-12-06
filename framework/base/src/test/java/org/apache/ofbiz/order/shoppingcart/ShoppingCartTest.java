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

    private MinimumOrderPriceRepositoryBuilder minimumOrderPriceRepository() {
        return new MinimumOrderPriceRepositoryBuilder();
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

    class MinimumOrderPriceRepositoryBuilder {
        private BigDecimal minimumOrderPrice = BigDecimal.ZERO;
        private List<GenericValue> productPrices = Collections.emptyList();

        MinimumOrderPriceRepositoryBuilder withMinimumOrderPriceForAnyProduct(BigDecimal price) {
            this.minimumOrderPrice = price;
            return this;
        }

        ShoppingCart.MinimumOrderPriceListRepository build() {
            BigDecimal minimumOrderPriceForAllProducts1 = this.minimumOrderPrice;
            List<GenericValue> productPrices1 = this.productPrices;
            return new ShoppingCart.MinimumOrderPriceListRepository() {
                private final BigDecimal minimumOrderPriceForAllProducts = minimumOrderPriceForAllProducts1;
                private List<GenericValue> productPrices = productPrices1;

                @Override
                public BigDecimal getMinimumOrderPriceFor(String itemProductId) throws GenericEntityException {
                    return minimumOrderPriceForAllProducts;
                }

                @Override
                public List<GenericValue> getPricesForProduct(String itemProductId) throws GenericEntityException {
                    return productPrices;
                }
            };
        }

        public MinimumOrderPriceRepositoryBuilder withSpecialPromoPriceForAnyProduct(BigDecimal specialPromoPrice) {
            GenericValue specialPromoPriceValue = mock(GenericValue.class);
            when(specialPromoPriceValue.getString("productPriceTypeId")).thenReturn("SPECIAL_PROMO_PRICE");
            when(specialPromoPriceValue.getBigDecimal("price")).thenReturn(specialPromoPrice);
            this.productPrices = Arrays.asList(specialPromoPriceValue);
            return this;
        }
    }


    private ProductStoreBuilder productStore() {
        return new ProductStoreBuilder();
    }

    private ShoppingCartBuilder cart() {
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


    private class ShoppingCartBuilder {
        private Delegator delegator = mock(Delegator.class);
        private GenericValue productStore = mock(GenericValue.class);
        private Locale defaultLocale = Locale.CANADA;
        private Locale locale = Locale.US;
        private String productStoreId = "my_store";
        private String currency = "EUR";
        private String defaultCurrency = "USD";
        private String billFromVendorPartyId = null;
        private boolean readOnly = false;
        private ShoppingCart.Logger logger = mock(ShoppingCart.Logger.class);
        private ShoppingCart.ProductStoreRepository productStoreRepository;
        private ShoppingCart.MinimumOrderPriceListRepository minimumOrderPriceRepository;

        ShoppingCartBuilder withProductStore(GenericValue store) {
            this.productStore = store;
            return this;
        }

        ShoppingCartBuilder withDefaultLocale(Locale locale) {
            this.defaultLocale = locale;
            return this;
        }

        ShoppingCartBuilder withLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        ShoppingCartBuilder withProductStoreId(String id) {
            this.productStoreId = id;
            return this;
        }

        ShoppingCartBuilder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        ShoppingCartBuilder withDefaultCurrency(String currency) {
            this.defaultCurrency = currency;
            return this;
        }

        ShoppingCartBuilder withBillFromVendorPartyId(String partyId) {
            this.billFromVendorPartyId = partyId;
            return this;
        }

        public ShoppingCartBuilder readOnly() {
            this.readOnly = true;
            return this;
        }

        public ShoppingCartBuilder withLogger(ShoppingCart.Logger logger) {
            this.logger = logger;
            return this;
        }

        public ShoppingCartBuilder withProductStoreRepository(ShoppingCart.ProductStoreRepository repository) {
            this.productStoreRepository = repository;
            return this;
        }

        ShoppingCart build() {
            ShoppingCart cart = new ShoppingCart(this.delegator, this.productStoreId, "websiteid", this.locale, this.currency, null, this.billFromVendorPartyId) {
                @Override
                protected Locale getDefaultLocale() {
                    return defaultLocale;
                }

                @Override
                protected GenericValue loadProductStore(Delegator delegator, String productStoreId) {
                    return productStore;
                }

                @Override
                protected String getDefaultCurrency(Delegator delegator) {
                    return defaultCurrency;
                }

                @Override
                protected Logger getLogger() {
                    return logger;
                }

                @Override
                protected ProductStoreRepository getProductStoreRepository() {
                    return productStoreRepository;
                }

                @Override
                protected MinimumOrderPriceListRepository getMinimumOrderPriceListRepository() {
                    return minimumOrderPriceRepository;
                }
            };
            cart.setReadOnlyCart(this.readOnly);
            return cart;
        }

        public ShoppingCartBuilder withMinimumOrderPriceListRepository(ShoppingCart.MinimumOrderPriceListRepository testMinimumOrderPriceListRepository) {
            this.minimumOrderPriceRepository = testMinimumOrderPriceListRepository;
            return this;
        }
    }

}