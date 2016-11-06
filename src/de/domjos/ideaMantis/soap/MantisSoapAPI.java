package de.domjos.ideaMantis.soap;

import com.intellij.notification.NotificationType;
import de.domjos.ideaMantis.model.*;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.utils.Helper;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static org.ksoap2.serialization.MarshalHashtable.NAMESPACE;

public class MantisSoapAPI {
    private ConnectionSettings settings;
    private SoapSerializationEnvelope structEnvelope;
    private HttpTransportSE structTransport;

    public MantisSoapAPI(ConnectionSettings settings) {
        this.settings = settings;
        this.structEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        this.structTransport = new HttpTransportSE(this.settings.getHostName() + "/api/soap/mantisconnect.php");
        this.structTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        new MarshalBase64().register(structEnvelope);   //serialization
        structEnvelope.encodingStyle = SoapEnvelope.ENC;
    }

    public MantisUser testConnection() {
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_login");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            obj = (SoapObject) obj.getProperty(0);
            obj = (SoapObject) obj.getProperty(0);
            MantisUser user = new MantisUser(obj.getProperty(1).toString());
            user.setId(Integer.parseInt(obj.getProperty(0).toString()));
            user.setName(obj.getProperty(2).toString());
            user.setEmail(obj.getProperty(3).toString());
            user.setPassword(this.settings.getPassword());
            return user;
        } catch (Exception ex) {
            return null;
        }
    }

    public List<MantisProject> getProjects() {
        List<MantisProject> projectList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_projects_get_user_accessible");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            Vector objects = (Vector) obj.getProperty(0);
            for(Object object : objects) {
                SoapObject soapObject = (SoapObject) object;
                MantisProject mantisProject = new MantisProject(soapObject.getProperty(1).toString());
                mantisProject.setId(Integer.parseInt(soapObject.getProperty(0).toString()));
                mantisProject.setEnabled(Boolean.parseBoolean(soapObject.getProperty(3).toString()));
                mantisProject.setDescription(soapObject.getProperty(7).toString());
                for(Object sub : ((Vector) soapObject.getProperty(8))) {
                    SoapObject subObj = (SoapObject) sub;
                    MantisProject subProject = new MantisProject(subObj.getProperty(1).toString());
                    subProject.setId(Integer.parseInt(subObj.getProperty(0).toString()));
                    subProject.setEnabled(Boolean.parseBoolean(subObj.getProperty(3).toString()));
                    subProject.setDescription(subObj.getProperty(7).toString());
                    mantisProject.addSubProject(subProject);
                }
                projectList.add(mantisProject);
            }
        } catch (Exception ex) {
            return null;
        }
        return projectList;
    }

    public MantisIssue getIssue(int sid) {
        MantisIssue issue;
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_get");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("issue_id", sid);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            SoapObject soapObjIssue = (SoapObject) obj.getProperty(0);
            issue = this.getIssueFromSoap(soapObjIssue);
        } catch (Exception ex) {
            return null;
        }
        return issue;
    }

    public List<MantisIssue> getIssues(int pid) {
        List<MantisIssue> issueList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_get_issues");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("project_id", pid);
            structRequest.addProperty("page_number", 1);
            structRequest.addProperty("per_page", -1);
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
            return null;
        }
        return issueList;
    }

    public boolean removeIssue(int sid) {
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_delete");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("issue_id", sid);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            if(obj.getProperty(0)==null) {
                return false;
            } else {
                return Boolean.parseBoolean(obj.getProperty(0).toString());
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean addIssue(MantisIssue issue) {
        SoapObject structRequest;
        if(issue.getId()!=0) {
            structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_update");
        } else {
            structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_add");
        }
        try {
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
            issueObject.addProperty("priority", buildObjectRef("priorities", issue.getPriority()));
            issueObject.addProperty("severity", buildObjectRef("severities", issue.getSeverity()));
            issueObject.addProperty("status", buildObjectRef("status", issue.getStatus()));
            issueObject = buildAccountData(issue.getReporter(), issueObject);
            issueObject.addProperty("summary", issue.getSummary());
            issueObject.addProperty("fixed_in_version", issue.getFixed_in_version());
            issueObject.addProperty("target_version", issue.getTarget_version());
            issueObject.addProperty("description", issue.getDescription());
            issueObject.addProperty("steps_to_reproduce", issue.getSteps_to_reproduce());
            issueObject.addProperty("additional_information", issue.getAdditional_information());

            SoapObject[] tmp = buildAttachmentRequests(issue.getIssueAttachmentList());
            if(tmp.length!=0) {
                if(tmp[0]!=null) {
                    issueObject.addProperty("attachments", tmp);
                }
            }

            tmp = buildNoteRequests(issue.getIssueNoteList());
            if(tmp.length!=0) {
                if(tmp[0]!=null) {
                    issueObject.addProperty("notes", tmp);
                }
            }

            structRequest.addProperty("issue", issueObject);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean removeNote(int nid) {
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_note_delete");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("issue_note_id", nid);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            return Boolean.parseBoolean(obj.getProperty(0).toString());
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
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_note_add");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("issue_id", sid);
            structRequest.addProperty("note", buildNoteRequest(note));
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    private SoapObject buildNoteRequest(IssueNote note) {
        SoapObject issueNoteData = new SoapObject(NAMESPACE, "IssueNoteData");
        issueNoteData.addProperty("id", 0);
        issueNoteData = this.buildAccountData(note.getReporter(), issueNoteData);
        issueNoteData.addProperty("text", note.getText());
        if(!note.getView_state().equals("")) {
            SoapObject objectRef = new SoapObject(NAMESPACE, "ObjectRef");
            ObjectRef ref = getEnum("view_states", note.getView_state());
            objectRef.addProperty("id", ref.getId());
            objectRef.addProperty("name", ref.getName());
            issueNoteData.addProperty("view_state", objectRef);
        }
        return issueNoteData;
    }

    private SoapObject[] buildAttachmentRequests(List<IssueAttachment> attachments) {
        SoapObject[] objects = new SoapObject[attachments.size()];
        try {
            for(int i = 0; i<=attachments.size()-1; i++) {
                objects[i] = new SoapObject(NAMESPACE, "AttachmentData");
                objects[i].addProperty("id", attachments.get(i).getId());
                objects[i].addProperty("name", new File(attachments.get(i).getFilename()).getName());
                objects[i].addProperty("file_type", new File(attachments.get(i).getFilename()).getName());
                objects[i].addProperty("content", Files.readAllBytes(Paths.get(new File(attachments.get(i).getFilename()).toURI())));
            }
        } catch (Exception ignored) {}
        return objects;
    }

    private SoapObject[] buildNoteRequests(List<IssueNote> notes) {
        SoapObject[] objects = new SoapObject[notes.size()];
        try {
            for(int i = 0; i<=notes.size()-1; i++) {
                objects[i] = buildNoteRequest(notes.get(i));
            }
        } catch (Exception ignored) {}
        return objects;
    }

    private SoapObject buildObjectRef(String strEnum, String value) {
        SoapObject objectRef = new SoapObject(NAMESPACE, "ObjectRef");
        ObjectRef ref = getEnum(strEnum, value);
        objectRef.addProperty("id", ref.getId());
        objectRef.addProperty("name", ref.getName());
        return objectRef;
    }

    private SoapObject buildAccountData(MantisUser reporter, SoapObject parent) {
        if(reporter!=null) {
            SoapObject accountData = new SoapObject(NAMESPACE, "AccountData");
            accountData.addProperty("id", reporter.getId());
            accountData.addProperty("name", reporter.getUserName());
            accountData.addProperty("real_name", reporter.getName());
            accountData.addProperty("email", reporter.getEmail());
            parent.addProperty("reporter", accountData);
        }
        return parent;
    }

    public boolean removeAttachment(int aid) {
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_attachment_delete");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("issue_attachment_id", aid);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            return Boolean.parseBoolean(obj.getProperty(0).toString());
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
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_attachment_add");
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
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_get_categories");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("project_id", pid);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            for(Object object : ((Vector) obj.getProperty(0))) {
                categoryList.add(object.toString());
            }
        } catch (Exception ex) {
            return null;
        }
        return categoryList;
    }

    public List<String> getEnum(String type) {
        List<String> enumList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_enum_" + type);
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            for(Object object : ((Vector) obj.getProperty(0))) {
                enumList.add(((SoapObject)object).getProperty("name").toString());
            }
        } catch (Exception ex) {
            return null;
        }
        return enumList;
    }

    public ObjectRef getEnum(String type, String value) {
        ObjectRef ref = new ObjectRef("", "");
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_enum_" + type);
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            for(Object object : ((Vector) obj.getProperty(0))) {
                if(((SoapObject)object).getProperty("name").toString().equals(value)) {
                    ref.setName(((SoapObject)object).getProperty("name").toString());
                    ref.setId(Integer.parseInt(((SoapObject)object).getProperty("id").toString()));
                    break;
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return ref;
    }

    private List<Integer> getIntEnum(String type) {
        List<Integer> enumList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_enum_" + type);
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            for(Object object : ((Vector) obj.getProperty(0))) {
                enumList.add(Integer.parseInt(((SoapObject)object).getProperty("id").toString()));
            }
        } catch (Exception ex) {
            return null;
        }
        return enumList;
    }

    public List<MantisUser> getUsers(int pid) {
        List<Integer> accessLevels = this.getIntEnum("access_levels");
        List<MantisUser> userList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_get_users");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("project_id", pid);
            for(int level : accessLevels) {
                structRequest.addProperty("access", level);
                structEnvelope.setOutputSoapObject(structRequest);
                structTransport.call("SOAPAction", structEnvelope);
                SoapObject obj = (SoapObject) structEnvelope.bodyIn;
                for(Object object : ((Vector) obj.getProperty(0))) {
                    SoapObject soapObject = (SoapObject) object;
                    MantisUser user = new MantisUser(soapObject.getProperty(1).toString());
                    user.setName(soapObject.getProperty(2).toString());
                    user.setId(Integer.parseInt(soapObject.getProperty(0).toString()));
                    user.setEmail(soapObject.getProperty(3).toString());
                    boolean state = true;
                    for(MantisUser tmp : userList) {
                        if(tmp.getName().equals(user.getName())) {
                            state = false;
                        }
                    }
                    if(state) {
                        userList.add(user);
                    }
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return userList;
    }

    public List<String> getVersions(int pid) {
        List<String> enumList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_get_versions");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("project_id", pid);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            for(Object object : ((Vector) obj.getProperty(0))) {
                enumList.add(((SoapObject)object).getProperty("name").toString());
            }
        } catch (Exception ex) {
            return null;
        }
        return enumList;
    }

    private MantisIssue getIssueFromSoap(SoapObject soapObjIssue) {
        MantisIssue issue = new MantisIssue();
        issue.setId(Integer.parseInt(soapObjIssue.getProperty("id").toString()));
        issue.setCategory(Helper.getParam(soapObjIssue, "category", false, 0));
        issue.setPriority(Helper.getParam(soapObjIssue, "priority", true, 1));
        issue.setSeverity(Helper.getParam(soapObjIssue, "severity", true, 1));
        issue.setStatus(Helper.getParam(soapObjIssue, "status", true, 1));
        SoapObject user = (SoapObject) soapObjIssue.getProperty(8);
        MantisUser sUser = new MantisUser(Helper.getParam(user, "name", false, 0));
        sUser.setName(Helper.getParam(user, "real_name", false, 0));
        sUser.setId(Integer.parseInt(Helper.getParam(user, "id", false, 0)));
        sUser.setEmail(Helper.getParam(user, "email", false, 0));
        issue.setReporter(sUser);
        issue.setSummary(Helper.getParam(soapObjIssue, "summary", false, 0));
        issue.setReproducibility(Helper.getParam(soapObjIssue, "reproducibility", true, 1));
        issue.setDate_submitted(Helper.getParam(soapObjIssue, "date_submitted", false, 0));
        issue.setFixed_in_version(Helper.getParam(soapObjIssue, "fixed_in_version", false, 0));
        issue.setTarget_version(Helper.getParam(soapObjIssue, "target_version", false, 0));
        issue.setDescription(Helper.getParam(soapObjIssue, "description", false, 0));
        issue.setSteps_to_reproduce(Helper.getParam(soapObjIssue, "steps_to_reproduce", false, 0));
        issue.setAdditional_information(Helper.getParam(soapObjIssue, "additional_information", false, 0));
        try {
            if(soapObjIssue.getProperty("attachments")!=null) {
                for(Object object : (Vector) soapObjIssue.getProperty("attachments")) {
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
            Helper.printNotification("Problem", ex.getMessage(), NotificationType.ERROR);
        }
        try {
            if(soapObjIssue.getProperty("notes")!=null) {
                for(Object object : (Vector) soapObjIssue.getProperty("notes")) {
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
                    note.setReporter(sUser);
                    note.setView_state(Helper.getParam(soapObject, "view_state", true, 1));
                    issue.addNote(note);
                }
            }
        } catch (RuntimeException ignored) {} catch (Exception ex) {
            Helper.printNotification("Problem", ex.toString(), NotificationType.ERROR);
        }
        return issue;
    }

    private boolean checkProperty(SoapObject result) {
        if(result.getProperty(0)!=null) {
            if(result.getProperty(0) instanceof Integer) {
                return true;
            } else if(result.getProperty(0) instanceof Boolean) {
                return Boolean.parseBoolean(result.getProperty(0).toString());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
