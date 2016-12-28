package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;

import java.math.BigDecimal;

interface SuppliersRepository {
    GenericValue getFirstSupplierProduct(String productId, BigDecimal quantity, LocalDispatcher dispatcher, String partyId, String currency);
}
