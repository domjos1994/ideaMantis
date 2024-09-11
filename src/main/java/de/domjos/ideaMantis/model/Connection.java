package de.domjos.ideaMantis.model;

import com.intellij.openapi.components.BaseState;

public class Connection extends BaseState {
    private int itemsPerPage;
    private long projectId;
    private boolean fastTrack;
    private boolean reload;
    private int reloadTime;

    public Connection() {
        this.itemsPerPage = -1;
        this.projectId = -1;
        this.fastTrack = false;
        this.reload = false;
        this.reloadTime = 300;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public boolean isFastTrack() {
        return this.fastTrack;
    }

    public void setFastTrack(boolean fastTrack) {
        this.fastTrack = fastTrack;
    }

    public boolean isReload() {
        return this.reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }

    public int getReloadTime() {
        return this.reloadTime;
    }

    public void setReloadTime(int reloadTime) {
        this.reloadTime = reloadTime;
    }
}
