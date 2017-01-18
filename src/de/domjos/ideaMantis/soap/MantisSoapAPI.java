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
import java.text.SimpleDateFormat;
import java.util.*;

import static org.ksoap2.serialization.MarshalHashtable.NAMESPACE;

public class MantisSoapAPI {
    private ConnectionSettings settings;
    private SoapSerializationEnvelope structEnvelope;
    private HttpTransportSE structTransport;
    private MantisUser user;

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
            SoapObject userData = (SoapObject) obj.getProperty(0);
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
            this.user = user;
            System.out.println(user.getAccessLevel().getValue());
            return user;
        } catch (Exception ex) {
            return null;
        }
    }

    public MantisUser testConnection(String hostName, String userName, String password) {
        SoapObject structRequest = new SoapObject(hostName + "/api/soap/mantisconnect.php", "mc_login");
        try {
            structRequest.addProperty("username", userName);
            structRequest.addProperty("password", password);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            obj = (SoapObject) obj.getProperty(0);
            SoapObject userData = (SoapObject) obj.getProperty(0);
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
            this.user = user;
            System.out.println(user.getAccessLevel().getValue());
            return user;
        } catch (Exception ex) {
            return null;
        }
    }

    public Map<Integer, String> getAccessLevels() {
        Map<Integer, String> accessLevelMap = new LinkedHashMap<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_enum_access_levels");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject soapObject = (SoapObject) structEnvelope.bodyIn;
            Vector access = (Vector) soapObject.getProperty("return");
            for(Object obj : access) {
                SoapObject level = ((SoapObject)obj);
                accessLevelMap.put(Integer.parseInt(level.getProperty("id").toString()), level.getProperty("name").toString());
            }
        } catch (Exception ex) {
            return accessLevelMap;
        }
        return  accessLevelMap;
    }

    public boolean addProject(MantisProject project) {
        SoapObject structRequest;
        try {
            if(project.getId()==0) {
                 structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_add");
            } else {
                structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_update");
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
            return false;
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
                projectList.add(getProject(soapObject));
            }
        } catch (Exception ex) {
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
            for(Object sub : ((Vector) object.getProperty(8))) {
                project.addSubProject(getProject((SoapObject) sub));
            }
        } catch (Exception ex) {
            return null;
        }
        return project;
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
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean checkInIssue(int sid, String comment, String state) {
        try {
            IssueNote note = new IssueNote();
            note.setReporter(user);
            note.setText(comment);
            note.setView_state(state);
            note.setDate(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()));
            return this.addNote(sid, note);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean addIssue(MantisIssue issue) throws Exception {
        SoapObject structRequest;
        if(issue.getId()!=0) {
            structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_update");
        } else {
            structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_add");
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
        if(issue.getFixed_in_version()!=null)
            issueObject.addProperty("fixed_in_version", issue.getFixed_in_version().getName());
        if(issue.getTarget_version()!=null)
            issueObject.addProperty("target_version", issue.getTarget_version().getName());
        issueObject.addProperty("description", issue.getDescription());
        issueObject.addProperty("steps_to_reproduce", issue.getSteps_to_reproduce());
        issueObject.addProperty("additional_information", issue.getAdditional_information());

        /* ************************************************************************** */
        /* * Can't add attachments and notes direct because of serialisation error  * */
        /* ************************************************************************** */

        structRequest.addProperty("issue", issueObject);
        structEnvelope.setOutputSoapObject(structRequest);
        System.out.println(structEnvelope.bodyOut);
        structTransport.call("SOAPAction", structEnvelope);
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
        if(note.getReporter()!=null)
            issueNoteData = this.buildAccountData(note.getReporter(), issueNoteData, "reporter");
        issueNoteData.addProperty("text", note.getText());
        if(!note.getView_state().equals("")) {
            SoapObject objectRef = new SoapObject(NAMESPACE, "ObjectRef");
            ObjectRef ref = getEnum("view_states", note.getView_state());
            assert ref != null;
            objectRef.addProperty("id", ref.getId());
            objectRef.addProperty("name", ref.getName());
            issueNoteData.addProperty("view_state", objectRef);
        }
        return issueNoteData;
    }

    private SoapObject buildObjectRef(String strEnum, String value) {
        SoapObject objectRef = new SoapObject(NAMESPACE, "ObjectRef");
        ObjectRef ref = getEnum(strEnum, value);
        assert ref != null;
        objectRef.addProperty("id", ref.getId());
        objectRef.addProperty("name", ref.getName());
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

    public boolean removeAttachment(int aid) {
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_attachment_delete");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("issue_attachment_id", aid);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
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

    private ObjectRef getEnum(String type, String value) {
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

    public List<MantisUser> getUsers(int pid) throws Exception {
        List<Integer> accessLevels = this.getIntEnum("access_levels");
        List<MantisUser> userList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_get_users");
        structRequest.addProperty("username", this.settings.getUserName());
        structRequest.addProperty("password", this.settings.getPassword());
        structRequest.addProperty("project_id", pid);
        assert accessLevels != null;
        for(int level : accessLevels) {
            structRequest.addProperty("access", level);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            for(Object object : ((Vector) obj.getProperty(0))) {
                SoapObject soapObject = (SoapObject) object;
                MantisUser user = new MantisUser(checkAndGetProperty("name", soapObject));
                user.setName(checkAndGetProperty("real_name", soapObject));
                user.setId(Integer.parseInt(checkAndGetProperty("id", soapObject)));
                user.setEmail(checkAndGetProperty("email", soapObject));
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
        return userList;
    }

    public boolean addVersion(MantisVersion version, int pid) {
        SoapObject structRequest;
        try {
            if(version.getId()!=0) {
                structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_version_update");
                structRequest.addProperty("version_id", version.getId());
            } else {
                structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_version_add");
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
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean deleteVersion(int id) {
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_project_version_delete");
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("version_id", id);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    public List<MantisVersion> getVersions(int pid) {
        return getVersions(pid, "mc_project_get_versions");
    }

    private List<MantisVersion> getVersions(int pid, String function) {
        List<MantisVersion> enumList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", function);
        try {
            structRequest.addProperty("username", this.settings.getUserName());
            structRequest.addProperty("password", this.settings.getPassword());
            structRequest.addProperty("project_id", pid);
            structEnvelope.setOutputSoapObject(structRequest);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
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
        } catch (Exception ex) {
            return null;
        }
        return enumList;
    }

    public boolean addTagToIssue(int issue_id, String tags) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            String[] strTags = tags.split(", ");
            SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_issue_set_tags");
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

                tagObject.addProperty("id", tag.getId());
                if(tag.getReporter()!=null) {
                    SoapObject userObject = new SoapObject(NAMESPACE, "AccountData");
                    userObject.addProperty("name", tag.getReporter().getUserName());
                    userObject.addProperty("id", tag.getReporter().getId());
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
                tagObjectArray.add(tagObject);
            }
            structRequest.addProperty("tags", tagObjectArray);
            structEnvelope.setOutputSoapObject(structRequest);
            System.out.println(structEnvelope.bodyOut);
            structTransport.call("SOAPAction", structEnvelope);
            SoapObject obj = (SoapObject) structEnvelope.bodyIn;
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean addTag(MantisTag tag) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_tag_add");
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
            return checkProperty(obj);
        } catch (Exception ex) {
            return false;
        }
    }

    public List<MantisTag> getTags() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        List<MantisTag> tagList = new LinkedList<>();
        SoapObject structRequest = new SoapObject(this.settings.getHostName() + "/api/soap/mantisconnect.php", "mc_tag_get_all");
        try {
            int counter = 0, pageNumber = 0;
            do {
                counter--;
                pageNumber++;
                structRequest.addProperty("username", this.settings.getUserName());
                structRequest.addProperty("password", this.settings.getPassword());
                structRequest.addProperty("page_number", pageNumber);
                structRequest.addProperty("per_page", 100);
                structEnvelope.setOutputSoapObject(structRequest);
                structTransport.call("SOAPAction", structEnvelope);
                SoapObject obj = (SoapObject) structEnvelope.bodyIn;
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
            } while (counter!=0);
        } catch (Exception ex) {
            return tagList;
        }
        return tagList;
    }

    public MantisTag getTag(String name) {
        for(MantisTag tag : this.getTags()) {
            if(tag.getName().equals(name))
                return tag;
        }
        return null;
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
        List<MantisVersion> versions = this.getVersions(settings.getProjectID());
        for(MantisVersion version :  versions) {
            if(version.getName().equals(checkAndGetProperty("version", soapObjIssue))) {
                issue.setFixed_in_version(version);
                break;
            }
        }
        for(MantisVersion version :  versions) {
            if(version.getName().equals(checkAndGetProperty("target_version", soapObjIssue))) {
                issue.setTarget_version(version);
                break;
            }
        }
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
                    note.setReporter(sReporter);
                    note.setView_state(Helper.getParam(soapObject, "view_state", true, 1));
                    issue.addNote(note);
                }
            }
        } catch (RuntimeException ignored) {} catch (Exception ex) {
            Helper.printNotification("Problem", ex.toString(), NotificationType.ERROR);
        }
        try {
            if(soapObjIssue.getProperty("tags")!=null) {
                for(Object object : (Vector) soapObjIssue.getProperty("tags")) {
                    SoapObject soapObject = (SoapObject) object;
                    if(issue.getTags().equals("")) {
                        issue.setTags(checkAndGetProperty("name", soapObject));
                    } else {
                        issue.setTags(issue.getTags() + ", " + checkAndGetProperty("name", soapObject));
                    }
                }
            }
        } catch (RuntimeException ignored) {} catch (Exception ex) {
            Helper.printNotification("Problem", ex.toString(), NotificationType.ERROR);
        }
        return issue;
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
                issue = this.getIssue(issue.getId());
                if(issue.getTarget_version()!=null) {
                    if(issue.getTarget_version().equals(version)) {
                        changeLogMap.put(issue, version);
                    }
                }
            }
        }
        return changeLogMap;
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
}
