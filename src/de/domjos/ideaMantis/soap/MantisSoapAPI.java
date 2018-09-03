/*
 * Copyright (C) 2017 Domjos1994
 *
 * This file is part of ideaMantis.
 * ideaMantis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.domjos.ideaMantis.soap;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import de.domjos.ideaMantis.model.*;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.utils.Helper;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.ksoap2.serialization.MarshalHashtable.NAMESPACE;

/**
 * Class which executes the calls to the mantis-soap-api
 * @author domjos1994
 * @version 1.0
 */
public class MantisSoapAPI {
    private ConnectionSettings settings;
    private SoapSerializationEnvelope structEnvelope;
    private HttpTransportSE structTransport;
    private MantisUser user;
    private int issueID;

    /**
     * Constructor with the connection-settings as param
     * @see de.domjos.ideaMantis.service.ConnectionSettings
     * @param settings the connection-settings
     */
    public MantisSoapAPI(ConnectionSettings settings) {

        // initialize settings
        this.settings = settings;

        // initialize the serialization of soap
        this.structEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        // initialize the transport-object
        this.structTransport = new HttpTransportSE(this.returnURL());
        this.structTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

        // initialize MarshalBase for sending files
        new MarshalBase64().register(structEnvelope);

        // set the right encoding
        structEnvelope.encodingStyle = SoapEnvelope.ENC;

        this.issueID = 0;

    }

    /**
     * Function to test the connection to the MantisBT
     * and get the current user of the api
     * @return current user
     */
    public MantisUser testConnection() {

        try {
            // try to send a request to the api
            SoapObject obj = this.executeQueryAndGetSoapObject("mc_login", null);

            if(obj!=null) {
                // get the result from the api
                obj = (SoapObject) obj.getProperty(0);
                SoapObject userData = (SoapObject) obj.getProperty(0);

                // get user-data, the access-level of the user
                MantisUser user = new MantisUser(checkAndGetProperty("name", userData));
                user.setId(Integer.parseInt(checkAndGetProperty("id", userData)));
                user.setName(checkAndGetProperty("real_name", userData));
                user.setEmail(checkAndGetProperty("email", userData));
                user.setPassword(this.settings.getPassword());
                int access = Integer.parseInt(obj.getProperty(1).toString());
                for(Map.Entry<Integer, String> entry : this.getAccessLevels().entrySet()) {
                    if(access==entry.getKey()) {
                        user.setAccessLevel(entry.getKey(), entry.getValue());
                    }
                }

                // set it to the class-param and return it
                this.user = user;
                return user;
            }

        } catch (Exception ex) {
            Helper.printException(ex);
        }
        return null;
    }

    private Map<Integer, String> getAccessLevels() {
        Map<Integer, String> accessLevelMap = new LinkedHashMap<>();
        try {
            SoapObject soapObject = this.executeQueryAndGetSoapObject("mc_enum_access_levels", null);
            if(soapObject!=null) {
                Vector access = (Vector) soapObject.getProperty("return");
                for(Object obj : access) {
                    SoapObject level = ((SoapObject)obj);
                    accessLevelMap.put(Integer.parseInt(level.getProperty("id").toString()), level.getProperty("name").toString());
                }
            }
        } catch (Exception ex) {
            Helper.printException(ex);
        }
        return  accessLevelMap;
    }

    public Map.Entry<Integer, String> getRightsFromProject(int pid) {
        Map.Entry<Integer, String> right = null;
        MantisUser current = this.testConnection();
        for(Map.Entry<Integer, String> entry : this.getAccessLevels().entrySet()) {
            try {
                SoapObject obj =
                    this.executeQueryAndGetSoapObject(
                        "mc_project_get_users",
                        new Object[][]{{"project_id", pid}, {"access", entry.getKey()}}
                    );
                if(obj!=null) {
                    Vector objects = (Vector) obj.getProperty(0);
                    for(Object object : objects) {
                        SoapObject userData = (SoapObject) object;
                        if(checkAndGetProperty("name", userData).equals(current.getUserName())) {
                            right = new AbstractMap.SimpleEntry<>(entry);
                        }
                    }
                }
            } catch (Exception ex) {
                Helper.printException(ex);
                return null;
            }
        }
        return right;
    }

    public List<CustomField> getCustomFields(int pid) {
        List<CustomField> customFields = new LinkedList<>();
        List<ObjectRef> custom_types = getEnum("custom_field_types");
        List<ObjectRef> objectRefList = new LinkedList<>();
        if(custom_types!=null) {
            objectRefList.addAll(custom_types);
        }

        try {
            SoapObject obj =
                this.executeQueryAndGetSoapObject(
                    "mc_project_get_custom_fields",
                    new Object[][]{{"project_id", pid}}
                );
            if(obj!=null) {
                Vector objects = (Vector) obj.getProperty(0);
                for(Object object : objects) {
                    SoapObject customFieldObject = (SoapObject) object;
                    SoapObject field = (SoapObject) customFieldObject.getProperty("field");
                    int typeID = Integer.parseInt(customFieldObject.getProperty("type").toString());
                    CustomField customField = new CustomField();
                    customField.setId(Integer.parseInt(field.getProperty("id").toString()));
                    customField.setName(field.getProperty("name").toString());
                    for(ObjectRef ref : objectRefList) {
                        if(ref!=null) {
                            if(ref.getId()==typeID) {
                                customField.setTypeId(ref.getId());
                                customField.setTypeName(ref.getName());
                                break;
                            }
                        }
                    }
                    customField.setPossibleValues(checkAndGetProperty("possible_values", customFieldObject));
                    customField.setDefaultValue(checkAndGetProperty("default_value", customFieldObject));
                    customField.setMin(checkAndGetIntProperty("length_min", customFieldObject));
                    customField.setMax(checkAndGetIntProperty("length_max", customFieldObject));
                    Map<Integer, String> accessLevels = getAccessLevels();
                    customField.setWriteAccessID(checkAndGetIntProperty("access_level_rw", customFieldObject));
                    customField.setReadAccessID(checkAndGetIntProperty("access_level_r", customFieldObject));
                    customField.setWriteAccessName(accessLevels.getOrDefault(customField.getWriteAccessID(), ""));
                    customField.setReadAccessName(accessLevels.getOrDefault(customField.getReadAccessID(), ""));
                    customField.setDisplayUpdate(checkAndGetBooleanProperty("display_update", customFieldObject));
                    customField.setDisplayReport(checkAndGetBooleanProperty("display_report", customFieldObject));
                    customField.setDisplayResolved(checkAndGetBooleanProperty("display_resolved", customFieldObject));
                    customField.setDisplayClosed(checkAndGetBooleanProperty("display_closed", customFieldObject));
                    customField.setRequireUpdate(checkAndGetBooleanProperty("require_update", customFieldObject));
                    customField.setRequireReport(checkAndGetBooleanProperty("require_report", customFieldObject));
                    customField.setRequireResolved(checkAndGetBooleanProperty("require_resolved", customFieldObject));
                    customField.setRequireClosed(checkAndGetBooleanProperty("require_closed", customFieldObject));
                    customFields.add(customField);
                }
            }
        } catch (Exception ex) {
            Helper.printException(ex);
        }
        return customFields;
    }

    public boolean addProject(MantisProject project) {
        SoapObject structRequest;
        try {
            if(project.getId()==0) {
                structRequest = new SoapObject(this.returnURL(), "mc_project_add");
            } else {
                structRequest = new SoapObject(this.returnURL(), "mc_project_update");
                structRequest.addProperty("project_id", project.getId());
            }
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            SoapObject projectObject = new SoapObject(NAMESPACE, "ProjectData");
            projectObject.addProperty("id", project.getId());
            projectObject.addProperty("name", project.getName());
            projectObject.addProperty("enabled", project.isEnabled());
            projectObject.addProperty("view_state", buildObjectRef("view_states", project.getView_state()));
            projectObject.addProperty("description", project.getDescription());
            if(!project.getSubProjects().isEmpty()) {
                for(int i = 0; i<= project.getSubProjects().size()-1; i++) {
                    SoapObject subObject = new SoapObject(NAMESPACE, "ProjectDataArray");
                    if(project.getSubProjects().get(i).getId()==0) {
                        if(this.addProject(project.getSubProjects().get(i))) {
                            for(MantisProject mantisProject : this.getProjects()) {
                                if(mantisProject.getName().equals(project.getSubProjects().get(i).getName())) {
                                    subObject.addProperty("id", mantisProject.getId());
                                    subObject.addProperty("name", project.getSubProjects().get(i).getName());
                                    subObject.addProperty("enabled", project.getSubProjects().get(i).isEnabled());
                                    subObject.addProperty("view_state", buildObjectRef("view_states", project.getSubProjects().get(i).getView_state()));
                                    subObject.addProperty("description", project.getSubProjects().get(i).getDescription());
                                    projectObject.addProperty("subprojects", subObject);
                                }
                            }
                        }
                    }
                }

            }
            structRequest.addProperty("project", projectObject);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            return checkProperty(obj);
        } catch (Exception ex) {
            Helper.printException(ex);
            return false;
        }
    }

    public List<MantisProject> getProjects() {
        List<MantisProject> projectList = new LinkedList<>();
        try {
            SoapObject obj = this.executeQueryAndGetSoapObject("mc_projects_get_user_accessible", null);
            if(obj!=null) {
                Vector objects = (Vector) obj.getProperty(0);
                for(Object object : objects) {
                    SoapObject soapObject = (SoapObject) object;
                    projectList.add(getProject(soapObject));
                }
            }
        } catch (Exception ex) {
            Helper.printException(ex);
            return null;
        }
        return projectList;
    }

    private MantisProject getProject(SoapObject object) {
        MantisProject project = new MantisProject(object.getProperty(1).toString());
        try {
            project.setId(Integer.parseInt(object.getProperty(0).toString()));
            project.setEnabled(Boolean.parseBoolean(object.getProperty(3).toString()));
            project.setDescription(object.getProperty(7).toString());
            SoapObject access = (SoapObject) object.getProperty("access_min");
            project.setAccess(Integer.parseInt(access.getProperty("id").toString()), access.getProperty("name").toString());
            for(Object sub : ((Vector) object.getProperty(8))) {
                project.addSubProject(getProject((SoapObject) sub));
            }
        } catch (Exception ex) {
            Helper.printException(ex);
            return null;
        }
        return project;
    }

    public List<MantisFilter> getFilters(int pid) {
        List<MantisFilter> filters = new LinkedList<>();
        try {
            SoapObject obj =
                this.executeQueryAndGetSoapObject(
                    "mc_filter_get",
                    new Object[][]{{"project_id", pid}}
                );
            if(obj!=null) {
                for(Object object : (Vector) obj.getProperty(0)) {
                    MantisFilter filter = new MantisFilter();
                    SoapObject soapObject = (SoapObject) object;
                    filter.setId(checkAndGetIntProperty("id", soapObject));
                    filter.setName(checkAndGetProperty("name", soapObject));
                    filter.setUrl(checkAndGetProperty("url", soapObject));
                    filter.setFilterPublic(checkAndGetBooleanProperty("is_public", soapObject));
                    filter.setFilterString(checkAndGetProperty("filter_string", soapObject));
                    SoapObject owner = (SoapObject) soapObject.getProperty("owner");
                    MantisUser user = new MantisUser(checkAndGetProperty("name", owner));
                    user.setName(checkAndGetProperty("real_name", owner));
                    user.setId(checkAndGetIntProperty("id", owner));
                    user.setEmail(checkAndGetProperty("email", owner));
                    filter.setOwner(user);
                    filters.add(filter);
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return filters;
    }

    public MantisIssue getIssue(int sid) {
        return this.getIssue(sid,false);
    }

    private MantisIssue getIssue(int sid,boolean small) {
        MantisIssue issue = null;
        try {
            SoapObject obj =
                this.executeQueryAndGetSoapObject(
                    "mc_issue_get",
                    new Object[][]{{"issue_id", sid}}
                );
            if(obj!=null) {
                SoapObject soapObjIssue = (SoapObject) obj.getProperty(0);
                issue = this.getIssueFromSoap(soapObjIssue, small);
            }
        } catch (Exception ex) {
            Helper.printException(ex);
            return null;
        }
        return issue;
    }

    public List<MantisIssue> getIssues(int pid) {
        return this.getIssues(pid, -1, "");
    }

    public List<MantisIssue> getIssues(int pid, int page, String filter) {
        List<MantisIssue> issueList = new LinkedList<>();
        SoapObject structRequest;
        if(filter.equals("")) {
            structRequest = new SoapObject(this.returnURL(), "mc_project_get_issues");
        } else {
            structRequest = new SoapObject(this.returnURL(), "mc_filter_get_issues");
            structRequest.addProperty("filter_id", Integer.parseInt(filter));
        }
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("project_id", pid);
            structRequest.addProperty("page_number", page);
            if(page==-1) {
                structRequest.addProperty("per_page", -1);
            } else {
                structRequest.addProperty("per_page", settings.getItemsPerPage());
            }
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            for(Object objIssue : (Vector)obj.getProperty(0)) {
                SoapObject soapObjIssue = (SoapObject) objIssue;
                MantisIssue issue = new MantisIssue();
                issue.setId(Integer.parseInt(soapObjIssue.getProperty("id").toString()));
                issue.setStatus(Helper.getParam(soapObjIssue, "status", true, 1));
                issue.setSummary(Helper.getParam(soapObjIssue, "summary", false, 0));
                issueList.add(issue);
            }
        } catch (Exception ex) {
            Helper.printException(ex);
            return null;
        }
        return issueList;
    }

    public boolean removeIssue(int sid) {
        try {
            SoapObject obj =
                this.executeQueryAndGetSoapObject(
                    "mc_issue_delete",
                    new Object[][]{{"issue_id", sid}}
                );
            return checkProperty(obj);
        } catch (Exception ex) {
            Logger.getInstance(this.getClass()).error(ex.toString());
            return false;
        }
    }

    public void checkInIssue(int sid, String comment, String state) {
        try {
            IssueNote note = new IssueNote();
            note.setReporter(user);
            note.setText(comment);
            note.setView_state(state);
            note.setDate(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()));
            this.addNote(sid, note);
        } catch (Exception ex) {
            Helper.printException(ex);
        }
    }

    public boolean addIssue(MantisIssue issue) throws Exception {
        SoapObject structRequest;
        if(issue.getId()!=0) {
            structRequest = new SoapObject(this.returnURL(), "mc_issue_update");
        } else {
            structRequest = new SoapObject(this.returnURL(), "mc_issue_add");
        }
        structRequest.addProperty("username", this.settings.getUserName());
        structRequest.addProperty("password", this.settings.getPassword());
        structRequest.addProperty("issueId", issue.getId());
        SoapObject issueObject = new SoapObject(NAMESPACE, "IssueData");
        issueObject.addProperty("last_updated", issue.getDate_submitted());
        SoapObject project = new SoapObject(NAMESPACE, "ObjectRef");
        project.addProperty("id", this.settings.getProjectID());
        for(MantisProject projectM : this.getProjects()) {
            if(projectM.getId()==this.settings.getProjectID()) {
                project.addProperty("name", project.getName());
                break;
            }
        }
        issueObject.addProperty("project", project);
        issueObject.addProperty("category", issue.getCategory());
        if(issue.getProfile()!=null) {
            issueObject.addProperty("platform", issue.getProfile().getPlatform());
            issueObject.addProperty("os", issue.getProfile().getOs());
            issueObject.addProperty("os_build", issue.getProfile().getOsBuild());
        } else {
            issueObject.addProperty("platform", "");
            issueObject.addProperty("os", "");
            issueObject.addProperty("os_build", "");
        }
        issueObject.addProperty("priority", buildObjectRef("priorities", issue.getPriority()));
        issueObject.addProperty("severity", buildObjectRef("severities", issue.getSeverity()));
        issueObject.addProperty("status", buildObjectRef("status", issue.getStatus()));
        if(this.user!=null) {
            issueObject = buildAccountData(this.user, issueObject, "reporter");
        } else {
            this.user = this.testConnection();
            if(this.user!=null) {
                issueObject = buildAccountData(this.user, issueObject, "reporter");
            }
        }
        if(issue.getReporter()!=null)
            issueObject = buildAccountData(issue.getReporter(), issueObject, "handler");
        issueObject.addProperty("summary", issue.getSummary());
        if(issue.getVersion()!=null) {
            issueObject.addProperty("version", issue.getVersion().getName());
        } else {
            issueObject.addProperty("version", null);
        }
        if(issue.getFixed_in_version()!=null) {
            issueObject.addProperty("fixed_in_version", issue.getFixed_in_version().getName());
        } else {
            issueObject.addProperty("fixed_in_version", null);
        }
        if(issue.getTarget_version()!=null) {
            issueObject.addProperty("target_version", issue.getTarget_version().getName());
        } else {
            issueObject.addProperty("target_version", null);
        }
        issueObject.addProperty("description", issue.getDescription());
        issueObject.addProperty("steps_to_reproduce", issue.getSteps_to_reproduce());
        issueObject.addProperty("additional_information", issue.getAdditional_information());
        if(!issue.getCustomFields().isEmpty()) {
            issueObject = buildCustomFieldData(issue.getCustomFields(), issueObject);
        }
        /* ************************************************************************** */
        /* * Can't add attachments and notes direct because of serialisation error  * */
        /* ************************************************************************** */

        structRequest.addProperty("issue", issueObject);
        structEnvelope.setOutputSoapObject(structRequest);
        try {
            structTransport.call("SOAPAction", structEnvelope);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        if(structEnvelope.bodyIn instanceof SoapObject) {
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;

            int id;
            if(issue.getId()!=0) {
                if(!checkProperty(obj)) {
                    return false;
                } else {
                    id = issue.getId();
                }
            } else {
                id = checkIntProperty(obj);
            }

            this.issueID = id;

            if(id!=0) {
                for(IssueAttachment attachment : issue.getIssueAttachmentList()) {
                    boolean state = addAttachment(id, attachment);
                    if(!state) {
                        Helper.printNotification("Problem", "Can't add Attachment!", NotificationType.ERROR);
                    }
                }
                for(IssueNote note : issue.getIssueNoteList()) {
                    boolean state = addNote(id, note);
                    if(!state) {
                        Helper.printNotification("Problem", "Can't add Note!", NotificationType.ERROR);
                    }
                }
                return true;
            } else {
                return false;
            }
        } else if(structEnvelope.bodyIn instanceof SoapFault) {
            SoapFault fault = (SoapFault) structEnvelope.bodyIn;
            Helper.printNotification(String.valueOf(fault.faultcode), fault.faultstring, NotificationType.ERROR);
            return false;
        } else {
            return false;
        }
    }

    public int getIssueID() {
        return this.issueID;
    }

    public boolean removeNote(int nid) {
        try {
            SoapObject obj =
                this.executeQueryAndGetSoapObject(
                    "mc_issue_note_delete",
                    new Object[][]{{"issue_note_id", nid}}
                );
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean addNote(int sid, IssueNote note) {
        if(note.getId()!=0) {
            if(!removeNote(note.getId())) {
                return false;
            }
        }
        try {
            SoapObject obj =
                this.executeQueryAndGetSoapObject(
                    "mc_issue_note_add",
                    new Object[][]{{"issue_id", sid},{"note", buildNoteRequest(note)}}
                );
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    private SoapObject buildNoteRequest(IssueNote note) {
        SoapObject issueNoteData = new SoapObject(NAMESPACE, "IssueNoteData");
        issueNoteData.addProperty("id", 0);
        if(note.getReporter()!=null)
            issueNoteData = this.buildAccountData(note.getReporter(), issueNoteData, "reporter");
        issueNoteData.addProperty("text", note.getText());
        if(!note.getView_state().equals("")) {
            SoapObject objectRef = new SoapObject(NAMESPACE, "ObjectRef");
            List<ObjectRef> objectRefs = getEnum("view_states");
            ObjectRef ref = null;
            for(ObjectRef objRef : objectRefs) {
                if(objRef.getName().equals(note.getView_state())) {
                    ref = objRef;
                    break;
                }
            }
            if(ref!=null) {
                objectRef.addProperty("id", ref.getId());
                objectRef.addProperty("name", ref.getName());
                issueNoteData.addProperty("view_state", objectRef);
            }
        }
        return issueNoteData;
    }

    private SoapObject buildObjectRef(String strEnum, String value) {
        SoapObject objectRef = new SoapObject(NAMESPACE, "ObjectRef");
        List<ObjectRef> objectRefs = getEnum(strEnum);
        ObjectRef ref = null;
        for(ObjectRef objRef : objectRefs) {
            if(objRef.getName().equals(value)) {
                ref = objRef;
                break;
            }
        }
        if(ref!=null) {
            objectRef.addProperty("id", ref.getId());
            objectRef.addProperty("name", ref.getName());
        }
        return objectRef;
    }

    private SoapObject buildAccountData(MantisUser reporter, SoapObject parent, String item) {
        if(reporter!=null) {
            SoapObject accountData = new SoapObject(NAMESPACE, "AccountData");
            accountData.addProperty("id", reporter.getId());
            accountData.addProperty("name", reporter.getUserName());
            accountData.addProperty("real_name", reporter.getName());
            accountData.addProperty("email", reporter.getEmail());
            parent.addProperty(item, accountData);
        }
        return parent;
    }

    private SoapObject buildCustomFieldData(Map<CustomField, String> customFieldResult, SoapObject parent) {
        if(customFieldResult!=null) {
            SoapObject object = new SoapObject(NAMESPACE, "CustomFieldValueForIssueDataArray");
            for(Map.Entry<CustomField, String> entry : customFieldResult.entrySet()) {
                SoapObject customFieldData = new SoapObject(NAMESPACE, "CustomFieldValueForIssueData");
                SoapObject customField = new SoapObject(NAMESPACE, "ObjectRef");
                customField.addProperty("id", entry.getKey().getId());
                customField.addProperty("name", entry.getKey().getName());
                customFieldData.addProperty("field", customField);
                customFieldData.addProperty("value", entry.getValue());
                object.addProperty("custom_fields", customFieldData);
            }
            parent.addProperty("custom_fields", object);
        }
        return parent;
    }

    public boolean removeAttachment(int aid) {
        try {
            SoapObject obj = this.executeQueryAndGetSoapObject("mc_issue_attachment_delete", new Object[][]{{"issue_attachment_id", aid}});
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean addAttachment(int sid, IssueAttachment attachment) {
        if(attachment.getId()!=0) {
            if(!removeAttachment(attachment.getId())) {
                return false;
            }
        }
        SoapObject structRequest = new SoapObject(this.returnURL(), "mc_issue_attachment_add");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("issue_id", sid);
            structRequest.addProperty("name", new File(attachment.getFilename()).getName());
            structRequest.addProperty("file_type", new File(attachment.getFilename()).getName());
            structRequest.addProperty("content", Files.readAllBytes(Paths.get(new File(attachment.getFilename()).toURI())));
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    public List<String> getCategories(int pid) {
        List<String> categoryList = new LinkedList<>();
        try {
            SoapObject obj =
                this.executeQueryAndGetSoapObject(
                    "mc_project_get_categories",
                    new Object[][]{{"project_id", pid}}
                );
            if(obj!=null) {
                for(Object object : ((Vector) obj.getProperty(0))) {
                    categoryList.add(object.toString());
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return categoryList;
    }

    public List<ObjectRef> getEnum(String type) {
        List<ObjectRef> enumList = new LinkedList<>();
        try {
            SoapObject obj = this.executeQueryAndGetSoapObject("mc_enum_" + type, null);
            if(obj!=null) {
                for(Object object : ((Vector) obj.getProperty(0))) {
                    SoapObject soapObject = (SoapObject) object;
                    ObjectRef objectRef = new ObjectRef("", "");
                    objectRef.setId(this.checkAndGetIntProperty("id", soapObject));
                    objectRef.setName(this.checkAndGetProperty("name", soapObject));
                    enumList.add(objectRef);
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return enumList;
    }

    public List<MantisUser> getUsers(int pid) throws Exception {
        List<ObjectRef> access_levels = this.getEnum("access_levels");
        List<MantisUser> userList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.returnURL(), "mc_project_get_users");
        structRequest.addProperty("username", this.settings.getUserName());
        structRequest.addProperty("password", this.settings.getPassword());
        structRequest.addProperty("project_id", pid);
        for(ObjectRef objectRef : access_levels) {
            if(objectRef!=null) {
                if(objectRef.getId()!=10 && objectRef.getId()!=25 && objectRef.getId()!=40) {
                    structRequest.addProperty("access", objectRef.getId());
                    structEnvelope.setOutputSoapObject(structRequest);
                    structTransport.call("SOAPAction", structEnvelope);
                    SoapObject obj = (SoapObject) structEnvelope.bodyIn;
                    for (Object object : ((Vector) obj.getProperty(0))) {
                        SoapObject soapObject = (SoapObject) object;
                        MantisUser user = new MantisUser(checkAndGetProperty("name", soapObject));
                        user.setName(checkAndGetProperty("real_name", soapObject));
                        user.setId(Integer.parseInt(checkAndGetProperty("id", soapObject)));
                        user.setEmail(checkAndGetProperty("email", soapObject));
                        boolean state = true;
                        for (MantisUser tmp : userList) {
                            if (tmp.getName().equals(user.getName())) {
                                state = false;
                            }
                        }
                        if (state) {
                            userList.add(user);
                        }
                    }
                }
            }
        }
        return userList;
    }

    public List<HistoryItem> getHistory(int issueID) {
        List<HistoryItem> historyItems = new LinkedList<>();
        try {
            SoapObject structRequest = this.executeQueryAndGetSoapObject("mc_issue_get_history", new Object[][]{{"issue_id", issueID}});
            if(structRequest!=null) {
                Vector vector = (Vector) structRequest.getProperty("return");
                for(Object object : vector) {
                    HistoryItem historyItem = new HistoryItem();
                    SoapObject soapObject = (SoapObject) object;

                    MantisUser user = new MantisUser(this.checkAndGetProperty("username", soapObject));
                    user.setId(this.checkAndGetIntProperty("userid", soapObject));
                    historyItem.setUser(user);

                    historyItem.setChangedAt(this.checkAndGetIntProperty("date", soapObject));
                    historyItem.setType(this.checkAndGetIntProperty("type", soapObject));
                    historyItem.setField(this.checkAndGetProperty("field", soapObject));
                    historyItem.setOldValue(this.checkAndGetProperty("old_value", soapObject));
                    historyItem.setNewValue(this.checkAndGetProperty("new_value", soapObject));

                    historyItems.add(historyItem);
                }
            }
        } catch (Exception ex) {
            Helper.printException(ex);
        }
        return historyItems;
    }

    public List<MantisProfile> getProfiles() {
        List<MantisProfile> mantisProfiles = new LinkedList<>();
        try {
            SoapObject structRequest = this.executeQueryAndGetSoapObject("mc_user_profiles_get_all", new Object[][]{{"page_number", 0}, {"per_page", -1}});
            if(structRequest!=null) {
                SoapObject obj = (SoapObject) structRequest.getProperty("return");
                Vector vector = (Vector) obj.getProperty("results");
                for(Object object : vector) {
                    MantisProfile profile = new MantisProfile();
                    SoapObject soapObject = (SoapObject) object;
                    profile.setId(this.checkAndGetIntProperty("id", soapObject));
                    profile.setPlatform(this.checkAndGetProperty("platform", soapObject));
                    profile.setOs(this.checkAndGetProperty("os", soapObject));
                    profile.setOsBuild(this.checkAndGetProperty("os_build", soapObject));
                    mantisProfiles.add(profile);
                }
            }
        } catch (Exception ex) {
            Helper.printException(ex);
        }
        return mantisProfiles;
    }

    public void addVersion(MantisVersion version, int pid) {
        SoapObject structRequest;
        try {
            if(version.getId()!=0) {
                structRequest = new SoapObject(this.returnURL(), "mc_project_version_update");
                structRequest.addProperty("version_id", version.getId());
            } else {
                structRequest = new SoapObject(this.returnURL(), "mc_project_version_add");
            }
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            SoapObject versionObject = new SoapObject(NAMESPACE, "ProjectVersionData");
            versionObject.addProperty("name", version.getName());
            versionObject.addProperty("project_id", pid);
            versionObject.addProperty("date_order", version.getDate());
            versionObject.addProperty("description ", version.getDescription());
            versionObject.addProperty("obsolete", version.isObsolete());
            versionObject.addProperty("released", version.isReleased());
            structRequest.addProperty("version", versionObject);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            checkProperty(obj);
        } catch (Exception ex) {
            Helper.printException(ex);
        }
    }

    public void deleteVersion(int id) {
        try {
            SoapObject obj = this.executeQueryAndGetSoapObject("mc_project_version_delete", new Object[][]{{"version_id", id}});
            checkProperty(obj);
        } catch (Exception ex) {
            Helper.printException(ex);
        }
    }

    public List<MantisVersion> getVersions(int pid) {
        return getVersions(pid, "mc_project_get_versions");
    }

    public void addTagToIssue(int issue_id, String tags) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            String[] strTags = tags.split(", ");
            SoapObject structRequest = new SoapObject(this.returnURL(), "mc_issue_set_tags");
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("issue_id", issue_id);
            Vector<SoapObject> tagObjectArray = new Vector<>();
            for(String strTag : strTags) {
                SoapObject tagObject = new SoapObject(NAMESPACE, "TagDataArray");
                MantisTag tag = getTag(strTag);
                if(tag==null) {
                    MantisTag mantisTag = new MantisTag();
                    mantisTag.setName(strTag);
                    if(this.user!=null)
                        mantisTag.setReporter(user);
                    this.addTag(mantisTag);
                    tag = getTag(strTag);
                }

                if (tag != null) {
                    tagObject.addProperty("id", tag.getId());
                    SoapObject userObject = new SoapObject(NAMESPACE, "AccountData");
                    userObject.addProperty("name", tag.getReporter().getUserName());
                    userObject.addProperty("id", tag.getReporter().getId());
                    userObject.addProperty("real_name", tag.getReporter().getName());
                    userObject.addProperty("email", tag.getReporter().getEmail());
                    tagObject.addProperty("user_id", userObject);
                    tagObject.addProperty("name", tag.getName());
                    tagObject.addProperty("description", tag.getDescription());
                    if (tag.getCreationDate() != null)
                        tagObject.addProperty("date_created", format.format(tag.getCreationDate()));
                    if (tag.getUpdatedDate() != null)
                        tagObject.addProperty("date_updated", format.format(tag.getUpdatedDate()));
                    tagObjectArray.add(tagObject);
                }
            }
            structRequest.addProperty("tags", tagObjectArray);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            checkProperty(obj);
        } catch (Exception ex) {
            Helper.printException(ex);
        }
    }

    public List<MantisTag> getTags() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        List<MantisTag> tagList = new LinkedList<>();
        try {
            int counter = 0, pageNumber = 0;
            do {
                counter--;
                pageNumber++;
                SoapObject obj =
                    this.executeQueryAndGetSoapObject(
                        "mc_tag_get_all",
                        new Object[][]{{"page_number", pageNumber}, {"per_page", 100}}
                    );
                if(obj!=null) {
                    SoapObject returnObject = (SoapObject) obj.getProperty("return");
                    if(counter==-1) {
                        counter = Integer.parseInt(returnObject.getProperty("total_results").toString()) / 100;
                    }
                    for (Object object : ((Vector) returnObject.getProperty("results"))) {
                        MantisTag tag = new MantisTag();
                        tag.setId(Integer.parseInt(checkAndGetProperty("id", ((SoapObject) object))));
                        if (!checkAndGetProperty("user_id", ((SoapObject) object)).isEmpty()) {
                            SoapObject user = (SoapObject) ((SoapObject) object).getProperty("user_id");
                            MantisUser sUser = new MantisUser(Helper.getParam(user, "name", false, 0));
                            sUser.setName(Helper.getParam(user, "real_name", false, 0));
                            sUser.setId(Integer.parseInt(Helper.getParam(user, "id", false, 0)));
                            sUser.setEmail(Helper.getParam(user, "email", false, 0));
                            tag.setReporter(sUser);
                        }
                        tag.setName(checkAndGetProperty("name", ((SoapObject) object)));
                        tag.setDescription(checkAndGetProperty("description", ((SoapObject) object)));
                        try {
                            tag.setCreationDate(format.parse(checkAndGetProperty("date_created", ((SoapObject) object))));
                            tag.setUpdatedDate(format.parse(checkAndGetProperty("date_updated", ((SoapObject) object))));
                        } catch (Exception ex) {
                            tag.setCreationDate(null);
                            tag.setUpdatedDate(null);
                        }
                        tagList.add(tag);
                    }
                }
            } while (counter!=0);
        } catch (Exception ex) {
            Helper.printException(ex);
            return tagList;
        }
        return tagList;
    }

    public Map<MantisIssue, MantisVersion> createChangeLog(MantisVersion mantisVersion) {
        Map<MantisIssue, MantisVersion> changeLogMap = new LinkedHashMap<>();
        List<MantisVersion> versions = new LinkedList<>();
        if(mantisVersion==null) {
            List<MantisVersion> tmp = getVersions(settings.getProjectID(), "mc_project_get_unreleased_versions");
            if(tmp==null)
                return changeLogMap;
            versions = tmp;
        } else {
            versions.add(mantisVersion);
        }

        List<MantisIssue> issues = this.getIssues(settings.getProjectID());
        for(MantisVersion version : versions) {
            for(MantisIssue issue : issues) {
                issue = this.getIssue(issue.getId(), true);
                if (issue != null && issue.getTarget_version() != null) {
                    if (issue.getTarget_version().getName().equals(version.getName())) {
                        changeLogMap.put(issue, version);
                    }
                }
            }
        }
        return changeLogMap;
    }

    private List<MantisVersion> getVersions(int pid, String function) {
        List<MantisVersion> enumList = new LinkedList<>();
        try {
            SoapObject obj = this.executeQueryAndGetSoapObject(function, new Object[][]{{"project_id", pid}});
            if(obj!=null) {
                for(Object object : ((Vector) obj.getProperty(0))) {
                    MantisVersion version = new MantisVersion();
                    version.setId(Integer.parseInt(checkAndGetProperty("id", ((SoapObject)object))));
                    version.setName(checkAndGetProperty("name", ((SoapObject)object)));
                    version.setDate(checkAndGetProperty("date_order", ((SoapObject)object)));
                    version.setDescription(checkAndGetProperty("description", ((SoapObject)object)));
                    version.setReleased(Boolean.parseBoolean(checkAndGetProperty("released", ((SoapObject)object))));
                    version.setObsolete(Boolean.parseBoolean(checkAndGetProperty("obsolete", ((SoapObject)object))));
                    enumList.add(version);
                }
            }
        } catch (Exception ex) {
            Helper.printException(ex);
            return null;
        }
        return enumList;
    }

    private void addTag(MantisTag tag) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        SoapObject structRequest = new SoapObject(this.returnURL(), "mc_tag_add");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            SoapObject tagObject = new SoapObject(NAMESPACE, "TagData");
            tagObject.addProperty("id", tag.getId());
            if(tag.getReporter()!=null) {
                SoapObject userObject = new SoapObject(NAMESPACE, "AccountData");
                userObject.addProperty("id", tag.getReporter().getId());
                userObject.addProperty("name", tag.getReporter().getUserName());
                userObject.addProperty("real_name", tag.getReporter().getName());
                userObject.addProperty("email", tag.getReporter().getEmail());
                tagObject.addProperty("user_id", userObject);
            }
            tagObject.addProperty("name", tag.getName());
            tagObject.addProperty("description", tag.getDescription());
            if(tag.getCreationDate()!=null)
                tagObject.addProperty("date_created", format.format(tag.getCreationDate()));
            if(tag.getUpdatedDate()!=null)
                tagObject.addProperty("date_updated", format.format(tag.getUpdatedDate()));
            structRequest.addProperty("tag", tagObject);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            checkProperty(obj);
        } catch (Exception ex) {
            Helper.printException(ex);
        }
    }

    private MantisTag getTag(String name) {
        for(MantisTag tag : this.getTags()) {
            if(tag.getName().equals(name))
                return tag;
        }
        return null;
    }

    private MantisIssue getIssueFromSoap(SoapObject soapObjIssue, boolean small) {
        MantisIssue issue = new MantisIssue();
        issue.setId(Integer.parseInt(soapObjIssue.getProperty("id").toString()));
        if(!small) {
            issue.setCategory(Helper.getParam(soapObjIssue, "category", false, 0));
            issue.setPriority(Helper.getParam(soapObjIssue, "priority", true, 1));
            issue.setSeverity(Helper.getParam(soapObjIssue, "severity", true, 1));
        }
        issue.setStatus(Helper.getParam(soapObjIssue, "status", true, 1));

        MantisProfile profile = new MantisProfile();
        profile.setPlatform(this.checkAndGetProperty("platform", soapObjIssue));
        profile.setOs(this.checkAndGetProperty("os", soapObjIssue));
        profile.setOsBuild(this.checkAndGetProperty("os_build", soapObjIssue));
        issue.setProfile(profile);

        if(!small) {
            SoapObject user = (SoapObject) soapObjIssue.getProperty(8);
            MantisUser sUser = new MantisUser(Helper.getParam(user, "name", false, 0));
            sUser.setName(Helper.getParam(user, "real_name", false, 0));
            sUser.setId(Integer.parseInt(Helper.getParam(user, "id", false, 0)));
            sUser.setEmail(Helper.getParam(user, "email", false, 0));
            issue.setReporter(sUser);
        }
        issue.setSummary(Helper.getParam(soapObjIssue, "summary", false, 0));
        if(!small) {
            issue.setReproducibility(Helper.getParam(soapObjIssue, "reproducibility", true, 1));
            issue.setDate_submitted(Helper.getParam(soapObjIssue, "date_submitted", false, 0));
        }
        List<MantisVersion> versions = this.getVersions(settings.getProjectID());
        if(!small) {
            for (MantisVersion version : versions) {
                if (version.getName().equals(checkAndGetProperty("version", soapObjIssue))) {
                    issue.setVersion(version);
                    break;
                }
            }
        }
        for(MantisVersion version :  versions) {
            if(version.getName().equals(checkAndGetProperty("target_version", soapObjIssue))) {
                issue.setTarget_version(version);
                break;
            }
        }
        for(MantisVersion version :  versions) {
            if(version.getName().equals(checkAndGetProperty("fixed_in_version", soapObjIssue))) {
                issue.setFixed_in_version(version);
                break;
            }
        }
        if(!small) {
            issue.setDescription(Helper.getParam(soapObjIssue, "description", false, 0));
            issue.setSteps_to_reproduce(Helper.getParam(soapObjIssue, "steps_to_reproduce", false, 0));
            issue.setAdditional_information(Helper.getParam(soapObjIssue, "additional_information", false, 0));
            try {
                if (soapObjIssue.getProperty("attachments") != null) {
                    for (Object object : (Vector) soapObjIssue.getProperty("attachments")) {
                        SoapObject soapObject = (SoapObject) object;
                        IssueAttachment attachment = new IssueAttachment();
                        attachment.setId(Integer.parseInt(Helper.getParam(soapObject, "id", false, 0)));
                        attachment.setFilename(Helper.getParam(soapObject, "filename", false, 0));
                        attachment.setSize(Integer.parseInt(Helper.getParam(soapObject, "size", false, 0)));
                        attachment.setDownload_url(Helper.getParam(soapObject, "download_url", false, 0));
                        issue.addAttachment(attachment);
                    }
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            }
            try {
                if (soapObjIssue.getProperty("notes") != null) {
                    for (Object object : (Vector) soapObjIssue.getProperty("notes")) {
                        SoapObject soapObject = (SoapObject) object;
                        IssueNote note = new IssueNote();
                        note.setId(Integer.parseInt(Helper.getParam(soapObject, "id", false, 0)));
                        note.setText(Helper.getParam(soapObject, "text", false, 0));
                        note.setDate(Helper.getParam(soapObject, "date_submitted", false, 0));
                        SoapObject reporter = (SoapObject) soapObjIssue.getProperty("reporter");
                        MantisUser sReporter = new MantisUser(Helper.getParam(reporter, "real_name", false, 0));
                        sReporter.setName(Helper.getParam(reporter, "name", false, 0));
                        sReporter.setId(Integer.parseInt(Helper.getParam(reporter, "id", false, 0)));
                        sReporter.setEmail(Helper.getParam(reporter, "email", false, 0));
                        note.setReporter(sReporter);
                        note.setView_state(Helper.getParam(soapObject, "view_state", true, 1));
                        issue.addNote(note);
                    }
                }
            } catch (RuntimeException ignored) {
            } catch (Exception ex) {
                Helper.printException(ex);
            }
            try {
                if (soapObjIssue.getProperty("tags") != null) {
                    for (Object object : (Vector) soapObjIssue.getProperty("tags")) {
                        SoapObject soapObject = (SoapObject) object;
                        if (issue.getTags().equals("")) {
                            issue.setTags(checkAndGetProperty("name", soapObject));
                        } else {
                            issue.setTags(issue.getTags() + ", " + checkAndGetProperty("name", soapObject));
                        }
                    }
                }
            } catch (RuntimeException ignored) {
            } catch (Exception ex) {
                Helper.printException(ex);
            }
            try {
                if(soapObjIssue.getProperty("custom_fields") != null) {
                    List<CustomField> fields = this.getCustomFields(settings.getProjectID());
                    for (Object object : (Vector) soapObjIssue.getProperty("custom_fields")) {
                        SoapObject soapObject = (SoapObject) object;
                        String value = checkAndGetProperty("value", soapObject);
                        SoapObject field = (SoapObject) soapObject.getProperty("field");
                        int id = checkAndGetIntProperty("id", field);
                        String name = checkAndGetProperty("name", field);
                        for(CustomField customField : fields) {
                            if(id==customField.getId() && name.equals(customField.getName())) {
                                issue.addCustomField(customField, value);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                //Helper.printException(ex);
            }
        }
        return issue;
    }

    private boolean checkProperty(SoapObject result) {
        try {
            try {
                if (checkIntProperty(result) != 0) {
                    return true;
                }
            } catch (Exception ignored) {
            }
            return result.getProperty(0) == null || Boolean.parseBoolean(result.getProperty(0).toString());
        } catch (Exception ex) {
            return false;
        }
    }

    private int checkIntProperty(SoapObject result) {
        try {
            return Integer.parseInt(result.getProperty(0).toString());
        } catch (Exception ex) {
            return 0;
        }
    }

    private String checkAndGetProperty(String name, SoapObject object) {
        try {
            if(object.getProperty(name)!=null) {
                return object.getProperty(name).toString();
            }
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    private int checkAndGetIntProperty(String name, SoapObject object) {
        try {
            if(object.getProperty(name)!=null) {
                return Integer.parseInt(object.getProperty(name).toString());
            }
            return 0;
        } catch (Exception ex) {
            return 0;
        }
    }

    private boolean checkAndGetBooleanProperty(String name, SoapObject object) {
        try {
            return object.getProperty(name) != null && Boolean.parseBoolean(object.getProperty(name).toString());
        } catch (Exception ex) {
            return false;
        }
    }

    private SoapObject executeQueryAndGetSoapObject(String action, Object[][] furtherParams) throws Exception {
        SoapObject structRequest = new SoapObject(this.returnURL(), action);
        structRequest.addProperty("username", this.settings.getUserName());
        structRequest.addProperty("password", this.settings.getPassword());
        if(furtherParams!=null) {
            for(Object[] param : furtherParams) {
                structRequest.addProperty(param[0].toString(), param[1]);
            }
        }
        structEnvelope.setOutputSoapObject(structRequest);
        try {
            structTransport.call("SOAPAction", structEnvelope);
            return (SoapObject) structEnvelope.bodyIn;
        } catch (Exception ex) {
            return null;
        }
    }

    private String returnURL() {
        StringBuilder builder = new StringBuilder("");
        builder.append(this.settings.getHostName());
        if(this.settings.getHostName().endsWith("/")) {
            builder.append("api/soap/mantisconnect.php");
        } else {
            builder.append("/api/soap/mantisconnect.php");
        }
        return builder.toString();
    }
}
