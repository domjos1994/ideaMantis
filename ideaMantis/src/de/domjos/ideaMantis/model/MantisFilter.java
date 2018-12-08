package de.domjos.ideaMantis.model;

/**
 * Created by Dominic Joas on 19.05.2017.
 */
public class MantisFilter {
    private int id;
    private String name;
    private String filterString;
    private String url;
    private MantisUser owner;
    private boolean filterPublic;

    public MantisFilter() {
        this.id = 0;
        this.name = "";
        this.filterString = "";
        this.url = "";
        this.owner = null;
        this.filterPublic = false;
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

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MantisUser getOwner() {
        return owner;
    }

    public void setOwner(MantisUser owner) {
        this.owner = owner;
    }

    public boolean isFilterPublic() {
        return filterPublic;
    }

    public void setFilterPublic(boolean filterPublic) {
        this.filterPublic = filterPublic;
    }
}
