/* * Copyright (c) 2024 DOMINIC JOAS * * This program is free software: you can redistribute it and/or modify * it under the terms of the GNU General Public License as published by * the Free Software Foundation, either version 3 of the License, or * (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program.  If not, see <http://www.gnu.org/licenses/>. */package de.domjos.ideaMantis.model;import java.util.AbstractMap;import java.util.LinkedList;import java.util.List;import java.util.Map;@SuppressWarnings("unused")public class MantisProject {    private int id;    private String name;    private String description;    private String view_state;    private boolean enabled;    private final List<MantisProject> subProjects;    private Map.Entry<Integer, String> access;    public MantisProject(String name) {        this.id = 0;        this.name = name;        this.description = "";        this.enabled = false;        this.subProjects = new LinkedList<>();        this.view_state = "";        this.access = null;    }    public int getId() {        return id;    }    public void setId(int id) {        this.id = id;    }    public String getName() {        return name;    }    public void setName(String name) {        this.name = name;    }    public String getDescription() {        return description;    }    public void setDescription(String description) {        this.description = description;    }    public boolean isEnabled() {        return enabled;    }    public void setEnabled(boolean enabled) {        this.enabled = enabled;    }    public List<MantisProject> getSubProjects() {        return subProjects;    }    public void addSubProject(MantisProject project) {        this.subProjects.add(project);    }    public String getView_state() {        return view_state;    }    public void setView_state(String view_state) {        this.view_state = view_state;    }    public Map.Entry<Integer, String> getAccess() {        return this.access;    }    public void setAccess(int id, String type) {        this.access = new AbstractMap.SimpleEntry<>(id, type);    }}