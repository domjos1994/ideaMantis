package de.domjos.ideaMantis.soap;

import de.domjos.ideaMantis.model.*;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.utils.Helper;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class MantisSoapAPI {
    private ConnectionSettings settings;
    private SoapSerializationEnvelope structEnvelope;
    private HttpTransportSE structTransport;

    public MantisSoapAPI(ConnectionSettings settings) {
        this.settings = settings;
        this.structEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        this.structTransport = new HttpTransportSE(this.settings.getHostName() + "/api/soap/mantisconnect.php");
        this.structTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
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
                issueList.add(this.getIssueFromSoap(soapObjIssue));
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
            return Boolean.parseBoolean(obj.getProperty(0).toString());
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
        MantisUser sUser = new MantisUser(user.getProperty(1).toString());
        sUser.setName(user.getProperty(2).toString());
        sUser.setId(Integer.parseInt(user.getProperty(0).toString()));
        sUser.setEmail(user.getProperty(3).toString());
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
                    attachment.setFilename(soapObject.getProperty(1).toString());
                    attachment.setSize(Integer.parseInt(soapObject.getProperty(2).toString()));
                    attachment.setDownload_url(soapObject.getProperty(5).toString());
                    issue.addAttachment(attachment);
                }
            }
        } catch (Exception ignored) {}
        try {
            if(soapObjIssue.getProperty("notes")!=null) {
                for(Object object : (Vector) soapObjIssue.getProperty("notes")) {
                    SoapObject soapObject = (SoapObject) object;
                    IssueNote note = new IssueNote();
                    note.setText(soapObject.getProperty(2).toString());
                    note.setDate(soapObject.getProperty(5).toString());
                    SoapObject reporter = (SoapObject) soapObjIssue.getProperty(1);
                    MantisUser sReporter = new MantisUser(reporter.getProperty(1).toString());
                    sReporter.setName(reporter.getProperty(2).toString());
                    sReporter.setId(Integer.parseInt(reporter.getProperty(0).toString()));
                    sReporter.setEmail(reporter.getProperty(3).toString());
                    note.setReporter(sUser);
                    note.setView_state(soapObjIssue.getProperty(3).toString());
                    issue.addNote(note);
                }
            }
        } catch (Exception ignored) {}
        return issue;
    }
}
