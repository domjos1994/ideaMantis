package de.domjos.ideaMantis.model;

import java.util.LinkedList;
import java.util.List;

public class MantisProject {
    private int id;
    private String name;
    private String description;
    private boolean enabled;
    private List<MantisProject> subProjects;

    public MantisProject(String name) {
        this.id = 0;
        this.name = name;
        this.description = "";
        this.enabled = false;
        this.subProjects = new LinkedList<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<MantisProject> getSubProjects() {
        return subProjects;
    }

    public void addSubProject(MantisProject project) {
        this.subProjects.add(project);
    }

    public void setSubProjects(List<MantisProject> subProjects) {
        this.subProjects = subProjects;
    }
}
