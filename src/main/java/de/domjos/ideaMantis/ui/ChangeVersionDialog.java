package de.domjos.ideaMantis.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.PanelCreator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class ChangeVersionDialog extends DialogWrapper {
    public static final String VERSION = "Change Version";
    public static final String TARGET_VERSION = "Change Target-Version";
    public static final String FIXED_IN_VERSION = "Change Fixed In Version";
    public static final String TAGS = "Add Tags";

    private final MantisSoapAPI api;
    private final List<MantisVersion> versions;
    private final List<MantisIssue> issues;
    private final String type;

    private ComboBox<String> cmbVersion;
    private JBTextField txtTags;

    protected ChangeVersionDialog(@Nullable Project project, int projectId, int[] issueIds, String type) {
        super(project);
        this.type = type;

        this.api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
        if(this.type.equals(ChangeVersionDialog.FIXED_IN_VERSION)) {
            this.versions = this.api.getVersions(projectId, "mc_project_get_unreleased_versions");
        } else {
            this.versions = this.api.getVersions(projectId);
        }
        this.issues = new LinkedList<>();
        for(int id : issueIds) {
            issues.add(this.api.getIssue(id));
        }

        this.init();
        this.setTitle(this.type);

        if(this.getButton(this.getOKAction())!=null) {
            JButton button = this.getButton(this.getOKAction());
            if (button != null) {
                button.addActionListener((event) -> {
                    for(MantisIssue issue : this.issues) {
                        if(this.cmbVersion.getSelectedItem() != null) {
                            String version = this.cmbVersion.getSelectedItem().toString().trim();
                            switch (this.type) {
                                case ChangeVersionDialog.VERSION:
                                    issue.setVersion(this.getVersion(version));
                                    break;
                                case ChangeVersionDialog.TARGET_VERSION:
                                    issue.setTarget_version(this.getVersion(version));
                                    break;
                                case ChangeVersionDialog.FIXED_IN_VERSION:
                                    issue.setFixed_in_version(this.getVersion(version));
                                    break;
                            }
                        }
                        if(this.type.equals(ChangeVersionDialog.TAGS)) {
                            issue.setTags(issue.getTags() + ", " + this.txtTags.getText().trim());
                        }
                        this.api.addIssue(issue);
                    }
                });
            }
        }
    }

    private MantisVersion getVersion(String name) {
        for(MantisVersion version : this.versions) {
            if(version.getName().equals(name)) {
                return version;
            }
        }
        return null;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints labelConstraint = PanelCreator.getLabelConstraint();
        GridBagConstraints txtConstraint = PanelCreator.getTxtConstraint();

        this.cmbVersion = new ComboBox<>();
        this.cmbVersion.setName("cmbVersion");
        for(MantisVersion version : this.versions) {
            this.cmbVersion.addItem(version.getName());
        }

        this.txtTags = new JBTextField();
        this.txtTags.setName("txtTags");

        if(this.type.equals(ChangeVersionDialog.TAGS)) {
            Label label = new Label("Tags");
            root.add(label, labelConstraint);
            root.add(this.txtTags, txtConstraint);
        } else {
            Label label = new Label(this.type);
            root.add(label, labelConstraint);
            root.add(this.cmbVersion, txtConstraint);
        }

        return root;
    }
}
