package com.jaims.privacyneedle.scam;

/** Original, fictional social-engineering examples. No real message data is collected. */
public final class ScamScenario {
    public final String id;
    public final String category;
    public final String sender;
    public final String message;
    public final boolean scam;
    public final String explanation;
    public final String safetyTip;

    ScamScenario(String id, String category, String sender, String message,
                 boolean scam, String explanation, String safetyTip) {
        this.id = id;
        this.category = category;
        this.sender = sender;
        this.message = message;
        this.scam = scam;
        this.explanation = explanation;
        this.safetyTip = safetyTip;
    }
}
