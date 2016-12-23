package org.apache.ofbiz.order.shoppingcart;

@FunctionalInterface
public interface Builder<T> {
    T build();
}
