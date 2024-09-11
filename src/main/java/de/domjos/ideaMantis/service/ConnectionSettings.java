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

package de.domjos.ideaMantis.service;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import de.domjos.ideaMantis.model.Connection;
import de.domjos.ideaMantis.model.MantisUser;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

@Service(Service.Level.PROJECT)
@State(
    name = "ideaMantisSettings",
    storages = {
        @Storage("$PROJECT_FILE$"),
        @Storage("$PROJECT_CONFIG_DIR$/ideaMantis.xml")
    }
)
public final class ConnectionSettings implements PersistentStateComponent<Connection> {
    private int reloadTime = 300;
    private boolean fastTrack = false, reload = false;
    private int itemsPerPage = 0;
    private long projectID = 0L;

    public static ConnectionSettings getInstance(Project project) {
        return project.getService(ConnectionSettings.class);
    }

    @Override
    public @NotNull Connection getState() {
        Connection connection = new Connection();
        connection.setReload(this.reload);
        connection.setFastTrack(this.fastTrack);
        connection.setProjectId(this.projectID);
        connection.setItemsPerPage(this.itemsPerPage);
        connection.setReloadTime(this.reloadTime);
        return connection;
    }

    @Override
    public void loadState(@NotNull Connection connection) {
        this.itemsPerPage = connection.getItemsPerPage();
        this.projectID = connection.getProjectId();
        this.fastTrack = connection.isFastTrack();
        this.reload = connection.isReload();
        this.reloadTime = connection.getReloadTime();
    }

    @Override
    public void noStateLoaded() {
        Connection connection = new Connection();
        this.reloadTime = connection.getReloadTime();
        this.fastTrack = connection.isFastTrack();
        this.reload = connection.isReload();
        this.itemsPerPage = connection.getItemsPerPage();
        this.projectID = connection.getProjectId();
    }

    @Override
    public void initializeComponent() {
        PersistentStateComponent.super.initializeComponent();
    }

    public String getHostName() {
        return this.getSensitiveData("host");
    }

    public void setHostName(String hostName) {
        this.setSensitiveData("host", hostName);
    }

    public String getUserName() {
        return this.getSensitiveData("user");
    }

    public void setUserName(String userName) {
        this.setSensitiveData("user", userName);
    }

    public String getPassword() {
        return this.getSensitiveData("password");
    }

    public void setPassword(String password) {
        this.setSensitiveData("password", password);
    }

    public long getProjectID() {
        return projectID;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public int getItemsPerPage() {
        return this.itemsPerPage;
    }

    public void setProjectID(long projectID) {
        this.projectID = projectID;
    }

    public boolean isFastTrack() {
        return this.fastTrack;
    }

    public void setFastTrack(boolean fastTrack) {
        this.fastTrack = fastTrack;
    }

    public boolean isReload() {
        return this.reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }

    public int getReloadTime() {
        return this.reloadTime;
    }

    public void setReloadTime(int reloadTime) {
        this.reloadTime = reloadTime;
    }

    public boolean validateSettings() {
        MantisSoapAPI api = new MantisSoapAPI(this);
        MantisUser user = api.testConnection();
        return user != null && projectID != 0;
    }

    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(
                CredentialAttributesKt.generateServiceName("ideaMantis", key)
        );
    }

    private String getSensitiveData(String key) {
        try {
            AtomicReference<String> result = new AtomicReference<>("");
            ApplicationManager.getApplication().invokeAndWait(() -> {
                CredentialAttributes attributes = createCredentialAttributes(key);
                PasswordSafe passwordSafe = PasswordSafe.getInstance();
                Credentials credentials = passwordSafe.get(attributes);
                if(credentials != null) {
                    result.set(credentials.getPasswordAsString());
                }
            });
            return result.get();
        } catch (Exception ignored) {}
        return "";
    }

    private void setSensitiveData(String key, String value) {
        try {
            CredentialAttributes attributes = createCredentialAttributes(key);
            Credentials credentials = new Credentials(key, value);
            PasswordSafe.getInstance().set(attributes, credentials);
        } catch (Exception ignored) {}
    }
}

