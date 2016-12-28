package org.apache.ofbiz.order.shoppingcart;

import com.google.protobuf.Message;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.product.config.ProductConfigWrapper;
import org.apache.ofbiz.service.LocalDispatcher;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.*;

class ShoppingCartBuilder {
    private Delegator delegator = mock(Delegator.class);
    private GenericValue productStore = mock(GenericValue.class);
    private Locale defaultLocale = Locale.CANADA;
    private Locale locale = Locale.US;
    private String productStoreId = "my_store";
    private String currency = "EUR";
    private String defaultCurrency = "USD";
    private String billFromVendorPartyId = null;
    private String orderType;
    private boolean readOnly = false;
    private ShoppingCart.Logger logger = mock(ShoppingCart.Logger.class);
    private ShoppingCart.ProductStoreRepository productStoreRepository;
    private ShoppingCart.MinimumOrderPriceListRepository minimumOrderPriceRepository;
    private ShoppingCartItem purchaseOrderItem;
    private String partyId;

    public static ShoppingCartBuilder cart() {
        return new ShoppingCartBuilder();
    }

    ShoppingCartBuilder withoutProductStore() {
        this.productStore = null;
        return this;
    }

    ShoppingCartBuilder withProductStore(BuilderAugmenter<ProductStoreBuilder> a) {
        this.productStore = a.augment(new ProductStoreBuilder()).build();
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

    public ShoppingCartBuilder createsPurchaseOrderItem(ShoppingCartItem item) {
        this.purchaseOrderItem = item;
        return this;
    }

    ShoppingCart build() {
        ShoppingCart cart = new ShoppingCart(this.delegator, this.productStoreId, "websiteid", this.locale, this.currency, null, this.billFromVendorPartyId) {
            @Override
            protected Locale getDefaultLocale() {
                return defaultLocale;
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
                if(productStoreRepository == null) {
                    productStoreRepository = mock(ProductStoreRepository.class);
                    when(productStoreRepository.getProductStore(any())).thenReturn(productStore);
                }
                return productStoreRepository;
            }

            @Override
            protected MinimumOrderPriceListRepository getMinimumOrderPriceListRepository() {
                return minimumOrderPriceRepository;
            }

            @Override
            protected ShoppingCartItem makePurchaseOrderItem(String productId, BigDecimal selectedAmount, BigDecimal quantity, Timestamp shipBeforeDate, Timestamp shipAfterDate, Map<String, GenericValue> features, Map<String, Object> attributes, String prodCatalogId, ProductConfigWrapper configWrapper, String itemType, LocalDispatcher dispatcher, ShoppingCartItemGroup itemGroup, GenericValue supplierProduct) throws CartItemModifyException, ItemNotFoundException {
                return purchaseOrderItem;
            }

            @Override
            protected MessageProvider getMessageProvider() {
                return (resource, name) -> String.format("Message for resource=[%s], name=[%s]", resource, name);
            }
        };
        cart.setReadOnlyCart(this.readOnly);
        cart.setOrderType(this.orderType);
        cart.setOrderPartyId(this.partyId);
        return cart;
    }

    public ShoppingCartBuilder withMinimumOrderPriceListRepository(BuilderAugmenter<MinimumOrderPriceRepositoryBuilder> a) {
        this.minimumOrderPriceRepository = a.augment(new MinimumOrderPriceRepositoryBuilder()).build();
        return this;
    }

    public ShoppingCartBuilder asPurchaseOrderCart() {
        this.orderType = "PURCHASE_ORDER";
        return this;
    }

    public ShoppingCartBuilder withPartyId(String partyId) {
        this.partyId = partyId;
        return this;
    }
}
