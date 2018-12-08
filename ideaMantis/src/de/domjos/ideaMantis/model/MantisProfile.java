package de.domjos.ideaMantis.model;

public class MantisProfile {
    private int id;
    private String platform;
    private String os;
    private String osBuild;

    public MantisProfile() {
        this.id = 0;
        this.platform = "";
        this.os = "";
        this.osBuild = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsBuild() {
        return osBuild;
    }

    public void setOsBuild(String osBuild) {
        this.osBuild = osBuild;
    }
}
