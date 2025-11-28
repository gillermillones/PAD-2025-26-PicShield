package es.ucm.fdi.pad.picshield;

public class ActivityItem {
    private String id;
    private String title;

    public ActivityItem(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
}
