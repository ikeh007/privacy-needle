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
        public final String learningNote;

        Product(String title, String category, String description, String url,
                String learningNote) {
            this.title = title;
            this.category = category;
            this.description = description;
            this.url = url;
            this.learningNote = learningNote;
        }
    }

    private static final List<Product> PRODUCTS = Collections.unmodifiableList(Arrays.asList(
        new Product("Privacy Needle Play", "FREE LEARNING",
            "Ten interactive games for recognising scams, improving password habits and exploring privacy.",
            "https://play.privacyneedle.com/",
            "Choose a short practice game and look for the warning signs you missed. Repeating scenarios can improve how quickly you notice suspicious requests."),
        new Product("Spot the Phishing", "FEATURED GAME",
            "Test your judgement with 30 fictional suspicious emails and practise spotting warning signs.",
            "https://play.privacyneedle.com/spot-the-phishing/",
            "Before trusting any email, inspect the sender, context and requested action. Avoid treating a familiar logo as proof of authenticity."),
        new Product("Privacy Needle Sentinel", "CYBERSECURITY",
            "Check website security headers and practise safe responses to simulated cyber incidents.",
            "https://sentinel.privacyneedle.com/",
            "A security-header check is one technical signal, not a complete security audit. Combine it with software updates and secure account settings."),
        new Product("Privacy Needle Check", "DUE DILIGENCE",
            "Run initial searches against public OFAC and UN sanctions lists.",
            "https://check.privacyneedle.com/",
            "Treat name matches as leads for manual verification. Similar names alone do not prove that someone is a sanctioned person or organisation."),
        new Product("Privacy Needle Calc", "COST PLANNING",
            "Estimate and compare cloud infrastructure, AI, database and SaaS costs.",
            "https://calc.privacyneedle.com/",
            "Compare recurring costs as well as one-time prices. Cloud and AI usage can change sharply as traffic, storage and requests increase."),
        new Product("Privacy Needle AI", "COMPLIANCE",
            "Access AI-assisted data protection and organisational compliance workflows.",
            "https://app.privacyneedle.com/",
            "AI-assisted compliance answers should be checked against the actual law, your organisation's policies and advice from a qualified professional."),
        new Product("Christmas Greetings", "SEASONAL",
            "Create and share personalised Christmas wishes and greeting cards.",
            "https://christmas.privacyneedle.com/",
            "When sharing a greeting, avoid including private addresses, account numbers or other details that you would not post publicly."),
        new Product("Privacy Needle Magazine", "FREE MAGAZINE",
            "Read and download practical reporting on cybersecurity and data protection.",
            "https://privacyneedle.com/magazine/",
            "Use magazine explainers as a starting point, then refer to official regulatory guidance for decisions involving legal obligations.")
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
