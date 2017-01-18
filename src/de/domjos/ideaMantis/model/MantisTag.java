package de.domjos.ideaMantis.model;

import java.util.Date;

public class MantisTag {
    private int id;
    private MantisUser reporter;
    private String name, description;
    private Date creationDate, updatedDate;

    public MantisTag() {
        this.id = 0;
        this.reporter = null;
        this.name = "";
        this.description = "";
        this.creationDate = new Date();
        this.updatedDate = new Date();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MantisUser getReporter() {
        return this.reporter;
    }

    public void setReporter(MantisUser reporter) {
        this.reporter = reporter;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdatedDate() {
        return this.updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
