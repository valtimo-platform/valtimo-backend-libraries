/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimo.config.testcontainer;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Ulimit;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class BootstrapMySqlContainer<SELF extends BootstrapMySqlContainer<SELF>> extends JdbcDatabaseContainer<SELF> {

    public static final String NAME = "mysql";
    public static final String IMAGE = "mysql";

    private static final String MY_CNF_CONFIG_OVERRIDE_PARAM_NAME = "TC_MY_CNF";
    private static final Integer MYSQL_PORT = 3306;
    private static final Integer HOST_PORT = 6161;
    private String databaseName; //is set via JDBC URL
    private String username; //default is test;
    private String password; //default is test;
    private static final String MYSQL_ROOT_USER = "root";
    private static final String NAMED_VOLUME_NAME = "valtimo-dev-fixtures";
    private static final String NAMED_VOLUME_MOUNT_PATH = "/var/lib/mysql";
    private static final List<Ulimit> U_LIMITS = List.of(new Ulimit("nofile", 90000, 90000));

    public BootstrapMySqlContainer(String dockerImageName) {
        super(dockerImageName);
    }

    @NotNull
    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return new HashSet<>(getMappedPort(MYSQL_PORT));
    }

    @Override
    public void configure() {
        optionallyMapResourceParameterAsVolume(
            MY_CNF_CONFIG_OVERRIDE_PARAM_NAME,
            "/etc/mysql/conf.d",
            "config/mysql/CustomConfig.cnf"
        );
        addExposedPort(MYSQL_PORT);
        addFixedExposedPort(HOST_PORT, MYSQL_PORT);
        addEnv("MYSQL_DATABASE", databaseName);
        addEnv("MYSQL_USER", username);

        withCreateContainerCmdModifier(
            createContainerCmd -> createContainerCmd
                .withUlimits(U_LIMITS)
                .withBinds(Bind.parse(NAMED_VOLUME_NAME + ":" + NAMED_VOLUME_MOUNT_PATH))
        );

        if (password != null && !password.isEmpty()) {
            addEnv("MYSQL_PASSWORD", password);
            addEnv("MYSQL_ROOT_PASSWORD", password);
        } else if (MYSQL_ROOT_USER.equalsIgnoreCase(username)) {
            addEnv("MYSQL_ALLOW_EMPTY_PASSWORD", "yes");
        } else {
            throw new ContainerLaunchException("Empty password can be used only with the root user");
        }
        setStartupAttempts(3);
        setLogConsumers(Collections.singletonList(new Slf4jLogConsumer(logger)));
    }

    @Override
    public String getDriverClassName() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:mysql://" + getContainerIpAddress() + ":" + getMappedPort(MYSQL_PORT) + "/" + databaseName;
    }

    @Override
    protected String constructUrlForConnection(String queryString) {
        String url = super.constructUrlForConnection(queryString);
        if (!url.contains("useSSL=")) {
            String separator = url.contains("?") ? "&" : "?";
            return url + separator + "useSSL=false";
        } else {
            return url;
        }
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getTestQueryString() {
        return "SELECT 1";
    }

    public SELF withConfigurationOverride(String s) {
        parameters.put(MY_CNF_CONFIG_OVERRIDE_PARAM_NAME, s);
        return self();
    }

    @Override
    public SELF withDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
        return self();
    }

    @Override
    public SELF withUsername(final String username) {
        this.username = username;
        return self();
    }

    @Override
    public SELF withPassword(final String password) {
        this.password = password;
        return self();
    }

}