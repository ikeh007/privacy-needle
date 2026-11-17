package com.jaims.privacyneedle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.jaims.privacyneedle.products.ProductCatalog;
import org.junit.Test;

public class ProductCatalogTest {
    @Test public void allProductLinksAreOfficialHttps() {
        assertEquals(8, ProductCatalog.all().size());
        for (ProductCatalog.Product product : ProductCatalog.all()) {
            assertTrue(ProductCatalog.isTrustedProductUrl(product.url));
        }
    }

    @Test public void rejectSpoofedAndInsecureDomains() {
        assertFalse(ProductCatalog.isTrustedProductUrl("http://play.privacyneedle.com/"));
        assertFalse(ProductCatalog.isTrustedProductUrl("https://privacyneedle.com.evil.test/"));
        assertFalse(ProductCatalog.isTrustedProductUrl("https://privacyneedle.com@evil.test/"));
    }
}
