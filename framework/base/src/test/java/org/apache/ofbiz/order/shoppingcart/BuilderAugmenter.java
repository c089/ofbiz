package org.apache.ofbiz.order.shoppingcart;

@FunctionalInterface
interface BuilderAugmenter<T> {
    T augment(T builder);
}
