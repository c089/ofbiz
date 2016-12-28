package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

class DefaultSuppliersRepository implements SuppliersRepository {
    final ShoppingCart.Logger logger;
    private MessageProvider messageProvider;

    DefaultSuppliersRepository(ShoppingCart.Logger logger, MessageProvider messageProvider) {
        this.logger = logger;
        this.messageProvider = messageProvider;
    }

    @Override
    public GenericValue getFirstSupplierProduct(String productId, BigDecimal quantity, LocalDispatcher dispatcher, String partyId, String currency) {
        GenericValue supplierProduct = null;
        Map<String, Object> params = UtilMisc.<String, Object>toMap("productId", productId,
                "partyId", partyId,
                "currencyUomId", currency,
                "quantity", quantity);
        try {
            Map<String, Object> result = dispatcher.runSync("getSuppliersForProduct", params);
            List<GenericValue> productSuppliers = UtilGenerics.checkList(result.get("supplierProducts"));
            if ((productSuppliers != null) && (productSuppliers.size() > 0)) {
                supplierProduct = productSuppliers.get(0);
            }
        } catch (Exception e) {
            getLogger().logWarning(this.messageProvider.getMessage(ShoppingCart.resource_error, "OrderRunServiceGetSuppliersForProductError") + e.getMessage(), ShoppingCart.module);
        }
        return supplierProduct;
    }

    public ShoppingCart.Logger getLogger() {
        return logger;
    }
}
