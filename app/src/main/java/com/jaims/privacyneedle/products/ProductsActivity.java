package com.jaims.privacyneedle.products;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.button.MaterialButton;
import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.ads.AdMobManager;
import com.jaims.privacyneedle.ads.AdPlacementPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Product discovery with original practical learning notes.
 * Paid native ads have distinct labels and are inserted after three and six
 * genuine product guides, never ahead of the content or as a fake product.
 */
public class ProductsActivity extends AppCompatActivity {
    private final List<FrameLayout> nativeSlots = new ArrayList<>();
    private AdMobManager adsManager;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_products);
        findViewById(R.id.productsBack).setOnClickListener(v -> finish());
        LinearLayout list = findViewById(R.id.productList);
        LayoutInflater inflater = LayoutInflater.from(this);

        List<ProductCatalog.Product> products = ProductCatalog.all();
        int desiredAds = AdPlacementPolicy.desiredProductNativeAds(products.size());

        for (int index = 0; index < products.size(); index++) {
            ProductCatalog.Product product = products.get(index);
            View card = inflater.inflate(R.layout.item_product, list, false);
            ((TextView) card.findViewById(R.id.productCategory)).setText(product.category);
            ((TextView) card.findViewById(R.id.productTitle)).setText(product.title);
            ((TextView) card.findViewById(R.id.productDescription)).setText(product.description);
            ((TextView) card.findViewById(R.id.productLearningNote))
                    .setText("PRIVACY NEEDLE TIP\n" + product.learningNote);
            ((TextView) card.findViewById(R.id.productDomain))
                    .setText(Uri.parse(product.url).getHost());
            MaterialButton open = card.findViewById(R.id.productOpen);
            open.setContentDescription("Open " + product.title + " in browser");
            open.setOnClickListener(v -> openProduct(product.url));
            list.addView(card);

            if ((index + 1) % 3 == 0 && nativeSlots.size() < desiredAds) {
                FrameLayout slot = new FrameLayout(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
                slot.setLayoutParams(params);
                slot.setVisibility(View.GONE);
                list.addView(slot);
                nativeSlots.add(slot);
            }
        }

        if (desiredAds > 0) {
            adsManager = new AdMobManager(this);
            adsManager.requestConsentAndInitialize(() -> {
                if (!isFinishing() && !isDestroyed() && adsManager.canLoadAds()) {
                    adsManager.loadProductNativeAds(products.size(), this::renderNativeAds);
                }
            });
        }
    }

    private void renderNativeAds() {
        if (adsManager == null || isFinishing() || isDestroyed()) return;
        List<NativeAd> loaded = adsManager.getLoadedNativeAds();
        int count = Math.min(nativeSlots.size(), loaded.size());
        for (int i = 0; i < count; i++) {
            FrameLayout slot = nativeSlots.get(i);
            if (slot.getChildCount() > 0) continue;
            NativeAdView view = createNativeView(loaded.get(i), slot);
            slot.addView(view);
            slot.setVisibility(View.VISIBLE);
        }
    }

    private NativeAdView createNativeView(NativeAd ad, FrameLayout slot) {
        NativeAdView view = (NativeAdView) LayoutInflater.from(this)
                .inflate(R.layout.item_native_ad, slot, false);
        TextView headline = view.findViewById(R.id.nativeHeadline);
        TextView body = view.findViewById(R.id.nativeBody);
        TextView advertiser = view.findViewById(R.id.nativeAdvertiser);
        ImageView icon = view.findViewById(R.id.nativeIcon);
        MediaView media = view.findViewById(R.id.nativeMedia);
        Button cta = view.findViewById(R.id.nativeCta);

        headline.setText(ad.getHeadline());
        view.setHeadlineView(headline);

        if (ad.getBody() != null) body.setText(ad.getBody());
        else body.setVisibility(View.GONE);
        view.setBodyView(body);

        if (ad.getAdvertiser() != null) advertiser.setText(ad.getAdvertiser());
        else advertiser.setVisibility(View.GONE);
        view.setAdvertiserView(advertiser);

        if (ad.getIcon() != null) icon.setImageDrawable(ad.getIcon().getDrawable());
        else icon.setVisibility(View.GONE);
        view.setIconView(icon);

        if (ad.getMediaContent() != null) media.setMediaContent(ad.getMediaContent());
        else media.setVisibility(View.GONE);
        view.setMediaView(media);

        if (ad.getCallToAction() != null) cta.setText(ad.getCallToAction());
        else cta.setVisibility(View.GONE);
        view.setCallToActionView(cta);

        // SDK handles native clicks and impressions. The layout itself labels
        // these cards "ADVERTISEMENT" separate from our own product links.
        view.setNativeAd(ad);
        return view;
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

    @Override
    protected void onDestroy() {
        if (adsManager != null) adsManager.destroy();
        for (FrameLayout slot : nativeSlots) slot.removeAllViews();
        super.onDestroy();
    }
}
