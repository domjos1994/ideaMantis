package de.domjos.ideaMantis.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MantisIssue {
    private int id;
    private String category;
    private String priority;
    private String severity;
    private String status;
    private String summary;
    private String build;
    private String reproducibility;
    private String tags;
    private String description, steps_to_reproduce, additional_information;
    private MantisVersion version;
    private MantisVersion fixed_in_version;
    private MantisVersion target_version;
    private MantisUser reporter;
    private String date_submitted;
    private final List<IssueAttachment> issueAttachmentList;
    private final List<IssueNote> issueNoteList;
    private Map<CustomField, String> customFields;
    private MantisProfile profile;

    public MantisIssue() {
        this.reporter = null;
        this.date_submitted = null;
        this.issueAttachmentList = new LinkedList<>();
        this.issueNoteList = new LinkedList<>();
        this.tags = "";
        this.setVersion(null);
        this.fixed_in_version = null;
        this.target_version = null;
        this.setCustomFields(new LinkedHashMap<>());
        this.profile = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getReproducibility() {
        return reproducibility;
    }

    public void setReproducibility(String reproducibility) {
        this.reproducibility = reproducibility;
    }

    public MantisVersion getVersion() {
        return version;
    }

    public void setVersion(MantisVersion version) {
        this.version = version;
    }

    public MantisVersion getFixed_in_version() {
        return fixed_in_version;
    }

    public void setFixed_in_version(MantisVersion fixed_in_version) {
        this.fixed_in_version = fixed_in_version;
    }

    public MantisVersion getTarget_version() {
        return target_version;
    }

    public void setTarget_version(MantisVersion target_version) {
        this.target_version = target_version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSteps_to_reproduce() {
        return steps_to_reproduce;
    }

    public void setSteps_to_reproduce(String steps_to_reproduce) {
        this.steps_to_reproduce = steps_to_reproduce;
    }

    public String getAdditional_information() {
        return additional_information;
    }

    public void setAdditional_information(String additional_information) {
        this.additional_information = additional_information;
    }

    public MantisUser getReporter() {
        return reporter;
    }

    public void setReporter(MantisUser reporter) {
        this.reporter = reporter;
    }

    public String getDate_submitted() {
        return date_submitted;
    }

    public void setDate_submitted(String date_submitted) {
        this.date_submitted = date_submitted;
    }

    public List<IssueAttachment> getIssueAttachmentList() {
        return issueAttachmentList;
    }

    public void addAttachment(IssueAttachment issueAttachment) {
        this.issueAttachmentList.add(issueAttachment);
    }

    public List<IssueNote> getIssueNoteList() {
        return issueNoteList;
    }

    public void addNote(IssueNote issueNote) {
        this.issueNoteList.add(issueNote);
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Map<CustomField, String> getCustomFields() {
        return customFields;
    }

    public void addCustomField(CustomField field, String value) {
        this.customFields.put(field, value);
    }

    public void setCustomFields(Map<CustomField, String> customFields) {
        this.customFields = customFields;
    }

    public MantisProfile getProfile() {
        return profile;
    }

    public void setProfile(MantisProfile profile) {
        this.profile = profile;
    }
}
