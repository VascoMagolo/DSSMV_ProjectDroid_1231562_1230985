package rttc.dssmv_projectdroid_1231562_1230985.model;

import java.util.Locale;

public class TtsRequest {
    private final String text;
    private final Locale locale;

    public TtsRequest(String text, String langCode) {
        this.text = text;
        this.locale = new Locale(langCode);
    }

    public String getText() {
        return text;
    }

    public Locale getLocale() {
        return locale;
    }
}