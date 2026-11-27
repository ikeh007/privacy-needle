package com.jaims.privacyneedle.scam;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Original 1080×1350 result card, with a scannable QR code that retains the
 * exact dated challenge. Social platforms do not always retain image captions,
 * so the QR avoids depending on EXTRA_TEXT alone.
 */
public final class ScamShareCard {
    private static final int INK = 0xFF171717;
    private static final int GOLD = 0xFFFFB300;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFD7D2C9;
    private ScamShareCard() {}

    public static Intent createShareIntent(Context context, String link, String day,
                                           int score, boolean correct) throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888);
        try {
            drawCard(bitmap, day, score, correct, link);
            File folder = new File(context.getCacheDir(), "quiz_share");
            if (!folder.exists() && !folder.mkdirs()) throw new IOException("Share folder unavailable");
            File card = File.createTempFile("scam_challenge_", ".png", folder);
            try (FileOutputStream stream = new FileOutputStream(card)) {
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    throw new IOException("Failed to save score image");
                }
            }
            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".shareprovider", card);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT,
                    "I scored " + score + " points in Privacy Needle's 30-second "
                    + "Scam of the Day. Can you beat me? Open the SAME challenge:\n"
                    + link + "\n"
                    + "Get the app: https://play.google.com/store/apps/details?id="
                    + context.getPackageName());
            intent.setClipData(ClipData.newUri(context.getContentResolver(),
                    "Privacy Needle challenge score", uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return intent;
        } finally {
            bitmap.recycle();
        }
    }

    private static void drawCard(Bitmap bitmap, String day, int score,
                                 boolean correct, String link) throws Exception {
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawColor(INK);

        p.setColor(GOLD);
        canvas.drawRoundRect(68, 75, 300, 89, 7, 7, p);
        text(canvas, p, "PRIVACY NEEDLE", 86, 176, 49, WHITE, true, false);
        text(canvas, p, "30-SECOND DIGITAL SAFETY CHALLENGE", 86, 230, 28, GOLD, true, false);
        text(canvas, p, "SCAM OF THE DAY", 86, 388, 58, WHITE, true, false);
        text(canvas, p, "MY SCORE", 86, 475, 30, MUTED, false, false);

        p.setColor(0xFF28241C);
        canvas.drawRoundRect(75, 518, 1005, 821, 36, 36, p);
        text(canvas, p, String.valueOf(score), 540, 722, 185, GOLD, true, true);
        text(canvas, p, "POINTS", 540, 782, 28, WHITE, true, true);

        text(canvas, p, correct ? "I SPOTTED THE CLUES." : "I LEARNED THE CLUES.",
                86, 901, 37, WHITE, true, false);
        text(canvas, p, "Could you beat my result?", 86, 956, 33, MUTED, false, false);

        p.setColor(WHITE);
        canvas.drawRoundRect(772, 980, 995, 1203, 18, 18, p);
        BitMatrix qr = new QRCodeWriter().encode(link, BarcodeFormat.QR_CODE, 184, 184);
        p.setColor(INK);
        for (int y = 0; y < qr.getHeight(); y++) {
            for (int x = 0; x < qr.getWidth(); x++) {
                if (qr.get(x, y)) canvas.drawRect(792 + x, 1000 + y,
                        793 + x, 1001 + y, p);
            }
        }

        text(canvas, p, "SCAN TO PLAY THE", 86, 1070, 27, GOLD, true, false);
        text(canvas, p, "SAME CHALLENGE", 86, 1110, 27, GOLD, true, false);
        text(canvas, p, "play.privacyneedle.com/challenge", 86, 1188, 25, MUTED, false, false);

        p.setColor(GOLD);
        canvas.drawRoundRect(68, 1250, 1012, 1260, 5, 5, p);
        text(canvas, p, "KNOW MORE. PROTECT MORE.  •  " + day, 86, 1313,
                24, MUTED, true, false);
    }

    private static void text(Canvas canvas, Paint p, String value, float x,
                             float y, int size, int color,
                             boolean bold, boolean center) {
        p.setColor(color);
        p.setTextSize(size);
        p.setTypeface(Typeface.create("sans-serif",
                bold ? Typeface.BOLD : Typeface.NORMAL));
        p.setTextAlign(center ? Paint.Align.CENTER : Paint.Align.LEFT);
        canvas.drawText(value, x, y, p);
    }
}
