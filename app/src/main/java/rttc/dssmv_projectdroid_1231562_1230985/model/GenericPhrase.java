package rttc.dssmv_projectdroid_1231562_1230985.model;

public class GenericPhrase {
    private String id;
    private String text;
    private String language;
    private String category;

    public GenericPhrase(){}

    public GenericPhrase(String text, String language, String category) {
        this.text = text;
        this.language = language;
        this.category = category;
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public String getText() {return text;}
    public void setText(String text) {this.text = text;}

    public String getLanguage() {return language;}
    public void setLanguage(String language) {this.language = language;}

    public String getCategory() {return category;}
    public void setCategory(String category) {this.category = category;}
}
