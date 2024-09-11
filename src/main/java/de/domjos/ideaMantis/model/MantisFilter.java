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

/**
 * Created by Dominic Joas on 19.05.2017.
 */
@SuppressWarnings("unused")
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
