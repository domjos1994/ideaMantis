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

import java.util.Date;

public class HistoryItem {
    private Date changedAt;
    private MantisUser user;
    private String field;
    private int type;
    private String oldValue;
    private String newValue;

    public HistoryItem() {
        this.changedAt = null;
        this.user = null;
        this.field = "";
        this.type = 0;
        this.oldValue = "";
        this.newValue = "";
    }

    public Date getChangedAt() {
        return this.changedAt;
    }

    public void setChangedAt(int changedAt) {
        this.changedAt = new Date((long) changedAt*1000L);
    }

    public MantisUser getUser() {
        return this.user;
    }

    public void setUser(MantisUser user) {
        this.user = user;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getOldValue() {
        return this.oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}
