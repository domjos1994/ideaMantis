package de.domjos.ideaMantis.model;

public class IssueNote {
    private int id;
    private MantisUser reporter;
    private String text;
    private String view_state;
    private String date;

    public IssueNote() {
        this.setId(0);
        this.reporter = null;
        this.text = "";
        this.view_state = "";
        this.date = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MantisUser getReporter() {
        return reporter;
    }

    public void setReporter(MantisUser reporter) {
        this.reporter = reporter;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getView_state() {
        return view_state;
    }

    public void setView_state(String view_state) {
        this.view_state = view_state;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
