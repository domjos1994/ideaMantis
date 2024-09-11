/*
 * Copyright (c) 2024 DOMINIC JOAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.ideaMantis.model;

import com.intellij.openapi.components.BaseState;
import de.domjos.ideaMantis.lang.Lang;

public class Connection extends BaseState {
    private int itemsPerPage;
    private long projectId;
    private boolean fastTrack;
    private boolean reload;
    private int reloadTime;
    private String colorNew;
    private String colorFeedback;
    private String colorAcknowledged;
    private String colorConfirmed;
    private String colorAssigned;
    private String colorResolved;
    private String colorClosed;

    public Connection() {
        this.itemsPerPage = -1;
        this.projectId = -1;
        this.fastTrack = false;
        this.reload = false;
        this.reloadTime = 300;
        this.colorNew = Lang.SETTINGS_COLORS_NEW_DEFAULT;
        this.colorFeedback = Lang.SETTINGS_COLORS_FEEDBACK_DEFAULT;
        this.colorAcknowledged = Lang.SETTINGS_COLORS_ACKNOWLEDGED_DEFAULT;
        this.colorConfirmed = Lang.SETTINGS_COLORS_CONFIRMED_DEFAULT;
        this.colorAssigned = Lang.SETTINGS_COLORS_ASSIGNED_DEFAULT;
        this.colorResolved = Lang.SETTINGS_COLORS_RESOLVED_DEFAULT;
        this.colorClosed = Lang.SETTINGS_COLORS_CLOSED_DEFAULT;
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

    public String getColorNew() {
        return this.colorNew;
    }

    public void setColorNew(String colorNew) {
        this.colorNew = colorNew;
    }

    public String getColorFeedback() {
        return this.colorFeedback;
    }

    public void setColorFeedback(String colorFeedback) {
        this.colorFeedback = colorFeedback;
    }

    public String getColorAcknowledged() {
        return this.colorAcknowledged;
    }

    public void setColorAcknowledged(String colorAcknowledged) {
        this.colorAcknowledged = colorAcknowledged;
    }

    public String getColorConfirmed() {
        return this.colorConfirmed;
    }

    public void setColorConfirmed(String colorConfirmed) {
        this.colorConfirmed = colorConfirmed;
    }

    public String getColorAssigned() {
        return this.colorAssigned;
    }

    public void setColorAssigned(String colorAssigned) {
        this.colorAssigned = colorAssigned;
    }

    public String getColorResolved() {
        return this.colorResolved;
    }

    public void setColorResolved(String colorResolved) {
        this.colorResolved = colorResolved;
    }

    public String getColorClosed() {
        return this.colorClosed;
    }

    public void setColorClosed(String colorClosed) {
        this.colorClosed = colorClosed;
    }
}
