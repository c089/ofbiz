package org.apache.ofbiz.order.shoppingcart;

import org.apache.ofbiz.entity.GenericValue;

import java.math.BigDecimal;

interface SuppliersRepository {
    GenericValue getFirstSupplierProduct(String productId, BigDecimal quantity, String partyId, String currency);
}
