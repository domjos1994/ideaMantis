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

import java.util.AbstractMap;
import java.util.Map;

@SuppressWarnings("unused")
public class MantisUser {
    private int id;
    private String userName, name, email, password;
    private Map.Entry<Integer, String> accessLevel;

    public MantisUser(String userName) {
        this.id = 0;
        this.userName = userName;
        this.name = "";
        this.email = "";
        this.password = "";
        this.accessLevel = new AbstractMap.SimpleEntry<>(0, "");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map.Entry<Integer, String> getAccessLevel() {return this.accessLevel;}

    public void setAccessLevel(int id, String name) {this.accessLevel = new AbstractMap.SimpleEntry<>(id, name);}
}
