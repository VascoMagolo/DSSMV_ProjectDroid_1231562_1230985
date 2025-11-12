package rttc.dssmv_projectdroid_1231562_1230985.model;

import java.util.Date;

public class ImageHistory {
    private String id;
    private String imageUrl;
    private String userId;
    private String extractedText;
    private String translatedText;
    private String targetLanguage;
    private Date timestamp;

    public ImageHistory() {}

    public ImageHistory(String imageUrl, String extractedText, String translatedText, String targetLanguage) {
        this.imageUrl = imageUrl;
        this.extractedText = extractedText;
        this.translatedText = translatedText;
        this.targetLanguage = targetLanguage;
        this.timestamp = new Date();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}