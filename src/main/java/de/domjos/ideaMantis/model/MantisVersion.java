package de.domjos.ideaMantis.model;

public class MantisVersion {
    private int id;
    private String name, date, description;
    private boolean released, obsolete;

    public MantisVersion() {
        this.id = 0;
        this.name = "";
        this.date = "";
        this.description = "";
        this.released = false;
        this.obsolete = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
