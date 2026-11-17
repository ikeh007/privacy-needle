package com.jaims.privacyneedle.products;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Official Privacy Needle product directory. */
public final class ProductCatalog {
    private ProductCatalog() {}

    public static final class Product {
        public final String title;
        public final String category;
        public final String description;
        public final String url;

        Product(String title, String category, String description, String url) {
            this.title = title;
            this.category = category;
            this.description = description;
            this.url = url;
        }
    }

    private static final List<Product> PRODUCTS = Collections.unmodifiableList(Arrays.asList(
        new Product("Privacy Needle Play", "FREE LEARNING",
            "Ten interactive games for recognising scams, improving password habits and exploring privacy.",
            "https://play.privacyneedle.com/"),
        new Product("Spot the Phishing", "FEATURED GAME",
            "Test your judgement with 30 fictional suspicious emails and practise spotting warning signs.",
            "https://play.privacyneedle.com/spot-the-phishing/"),
        new Product("Privacy Needle Sentinel", "CYBERSECURITY",
            "Check website security headers and practise safe responses to simulated cyber incidents.",
            "https://sentinel.privacyneedle.com/"),
        new Product("Privacy Needle Check", "DUE DILIGENCE",
            "Run initial searches against public OFAC and UN sanctions lists.",
            "https://check.privacyneedle.com/"),
        new Product("Privacy Needle Calc", "COST PLANNING",
            "Estimate and compare cloud infrastructure, AI, database and SaaS costs.",
            "https://calc.privacyneedle.com/"),
        new Product("Privacy Needle AI", "COMPLIANCE",
            "Access AI-assisted data protection and organisational compliance workflows.",
            "https://app.privacyneedle.com/"),
        new Product("Christmas Greetings", "SEASONAL",
            "Create and share personalised Christmas wishes and greeting cards.",
            "https://christmas.privacyneedle.com/"),
        new Product("Privacy Needle Magazine", "FREE MAGAZINE",
            "Read and download practical reporting on cybersecurity and data protection.",
            "https://privacyneedle.com/magazine/")
    ));

    public static List<Product> all() {
        return PRODUCTS;
    }

    public static boolean isTrustedProductUrl(String raw) {
        try {
            URI uri = new URI(raw);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null
                    || uri.getUserInfo() != null) return false;
            host = host.toLowerCase(Locale.ROOT);
            return host.equals("privacyneedle.com") || host.endsWith(".privacyneedle.com");
        } catch (Exception ex) {
            return false;
        }
    }
}
