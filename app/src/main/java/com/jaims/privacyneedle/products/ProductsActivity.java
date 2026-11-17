package com.jaims.privacyneedle.products;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.jaims.privacyneedle.R;

/**
 * First-party product promotion, clearly separate from paid AdMob units.
 * Descriptive links open official HTTPS properties through a browser.
 */
public class ProductsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_products);
        findViewById(R.id.productsBack).setOnClickListener(v -> finish());
        LinearLayout list = findViewById(R.id.productList);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ProductCatalog.Product product : ProductCatalog.all()) {
            View card = inflater.inflate(R.layout.item_product, list, false);
            ((TextView) card.findViewById(R.id.productCategory)).setText(product.category);
            ((TextView) card.findViewById(R.id.productTitle)).setText(product.title);
            ((TextView) card.findViewById(R.id.productDescription)).setText(product.description);
            ((TextView) card.findViewById(R.id.productDomain)).setText(
                    Uri.parse(product.url).getHost());
            MaterialButton open = card.findViewById(R.id.productOpen);
            open.setContentDescription("Open " + product.title + " in browser");
            open.setOnClickListener(v -> openProduct(product.url));
            list.addView(card);
        }
    }

    private void openProduct(String url) {
        if (!ProductCatalog.isTrustedProductUrl(url)) {
            Toast.makeText(this, "Product link is not verified", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(i);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "A browser is needed to open this product",
                    Toast.LENGTH_LONG).show();
        }
    }
}
