package com.jaims.privacyneedle.game;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Creates a shareable 1080x1350 portrait score card with original Privacy Needle
 * brand colours and a Play Store invitation. Image content always reflects the
 * user's locally recorded attempts; unknown legacy scores are never invented.
 */
public final class QuizScoreCard {
    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1350;
    private static final int GOLD = 0xFFFFB300;
    private static final int BLACK = 0xFF171717;
    private static final int CREAM = 0xFFF8F8F6;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAA9A3;

    private QuizScoreCard() {}

    public static Intent createShareIntent(Context context,
                                           int answered, int correct,
                                           boolean known, int streak)
            throws IOException {
        if (!QuizScoreRules.canShare(answered)) {
            throw new IllegalArgumentException("A full daily challenge is required");
        }
        Bitmap image = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(image);
            drawCard(canvas, answered, correct, known, Math.max(0, streak));
            File folder = new File(context.getCacheDir(), "quiz_share");
            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("Could not create score-card cache");
            }
            File file = File.createTempFile("privacy_needle_score_", ".png", folder);
            try (FileOutputStream stream = new FileOutputStream(file)) {
                if (!image.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    throw new IOException("Could not encode score card");
                }
            }
            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".shareprovider", file);
            String playStore = "https://play.google.com/store/apps/details?id="
                    + context.getPackageName();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, QuizScoreRules.socialCaption(
                    correct, answered, known, playStore));
            intent.setClipData(ClipData.newUri(
                    context.getContentResolver(), "Privacy Needle quiz score", uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return intent;
        } finally {
            image.recycle();
        }
    }

    private static void drawCard(Canvas c, int answered, int correct, boolean known, int streak) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(CREAM);
        c.drawColor(CREAM);

        p.setColor(BLACK);
        c.drawRoundRect(40, 40, 1040, 1310, 40, 40, p);
        p.setColor(GOLD);
        c.drawRoundRect(78, 86, 263, 99, 6, 6, p);

        draw(c, p, "PRIVACY NEEDLE", 95, 180, 44, WHITE, true, false);
        draw(c, p, "DAILY PRIVACY CHALLENGE", 95, 234, 26, GOLD, true, false);

        p.setColor(0xFF292929);
        c.drawRoundRect(86, 298, 994, 944, 32, 32, p);

        draw(c, p, "MY PRIVACY IQ", 540, 415, 40, WHITE, true, true);
        draw(c, p, QuizScoreRules.displayScore(correct, answered, known),
                540, 667, known ? 180 : 105, GOLD, true, true);
        draw(c, p, QuizScoreRules.scoreSubtitle(known),
                540, 737, 28, WHITE, true, true);

        p.setColor(GOLD);
        c.drawRoundRect(270, 788, 810, 862, 34, 34, p);
        draw(c, p, streak + " DAY STREAK", 540, 840, 30, BLACK, true, true);

        draw(c, p, "CAN YOU BEAT MY SCORE?", 540, 1031, 41, WHITE, true, true);
        draw(c, p, "Try the free daily privacy quiz.", 540, 1081, 30, GRAY, false, true);

        p.setColor(GOLD);
        c.drawRoundRect(88, 1142, 992, 1234, 24, 24, p);
        draw(c, p, "FIND PRIVACY NEEDLE ON GOOGLE PLAY", 540, 1202, 26, BLACK, true, true);

        draw(c, p, "Google Play  |  privacyneedle.com", 540, 1279, 22, GRAY, false, true);
    }

    private static void draw(Canvas canvas, Paint paint, String text,
                             float x, float y, float size,
                             int color, boolean bold, boolean centered) {
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTypeface(Typeface.create(
                bold ? "sans-serif" : "sans-serif-light",
                bold ? Typeface.BOLD : Typeface.NORMAL));
        paint.setTextAlign(centered ? Paint.Align.CENTER : Paint.Align.LEFT);
        canvas.drawText(text, x, y, paint);
    }
}
