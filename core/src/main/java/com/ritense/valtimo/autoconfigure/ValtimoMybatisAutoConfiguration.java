/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.autoconfigure;

import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ValtimoMybatisAutoConfiguration {

    @Value("${valtimo.database:mysql}")
    private String valtimoDatabaseType;

    protected static final Map<String, String> databaseSpecificTruncDatepart1 = new HashMap<>();
    protected static final Map<String, String> databaseSpecificTruncDatepart2 = new HashMap<>();

    static {
        // init database specific trunc date functions
        databaseSpecificTruncDatepart1.put(DbSqlSessionFactory.H2, "PARSEDATETIME(FORMATDATETIME(");
        databaseSpecificTruncDatepart2.put(DbSqlSessionFactory.H2, ",'dd-MM-yyyy'),'dd-MM-yyyy')");
        databaseSpecificTruncDatepart1.put(DbSqlSessionFactory.ORACLE, "TRUNC(");
        databaseSpecificTruncDatepart2.put(DbSqlSessionFactory.ORACLE, ",')");
        for (String mysqlLikeDatabase : Arrays.asList(
            DbSqlSessionFactory.MYSQL,
            DbSqlSessionFactory.MARIADB,
            DbSqlSessionFactory.POSTGRES
        )) {
            databaseSpecificTruncDatepart1.put(mysqlLikeDatabase, "date(");
            databaseSpecificTruncDatepart2.put(mysqlLikeDatabase, ")");
        }
    }

    @Bean
    @ConditionalOnMissingBean(SpringManagedTransactionFactory.class)
    public SpringManagedTransactionFactory springManagedTransactionFactory() {
        return new SpringManagedTransactionFactory();
    }

    @Bean
    @ConditionalOnMissingBean(SqlSessionFactory.class)
    public SqlSessionFactory sqlSessionFactory(
        final SpringProcessEngineConfiguration springProcessEngineConfiguration,
        final SpringManagedTransactionFactory springManagedTransactionFactory
    ) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setTransactionFactory(springManagedTransactionFactory);
        sqlSessionFactoryBean.setDataSource(springProcessEngineConfiguration.getDataSource());
        sqlSessionFactoryBean.setMapperLocations(
            new ClassPathResource("common.xml"),
            new ClassPathResource("camunda-queries.xml"),
            new ClassPathResource("camunda-process-instance-v2.xml")
        );
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setLazyLoadingEnabled(false);
        configuration.setLogImpl(Slf4jImpl.class);
        configuration.setVariables(getProps(springProcessEngineConfiguration));
        sqlSessionFactoryBean.setConfiguration(configuration);
        return sqlSessionFactoryBean.getObject();
    }

    private Properties getProps(ProcessEngineConfigurationImpl conf) {
        Properties properties = new Properties();
        properties.put("prefix", conf.getDatabaseTablePrefix());

        String dbmsToUse;
        if (conf.getDatabaseType() == null || conf.getDatabaseType().isEmpty())
            dbmsToUse = valtimoDatabaseType;
        else{
            dbmsToUse = conf.getDatabaseType();
        }

        ProcessEngineConfigurationImpl.initSqlSessionFactoryProperties(properties, conf.getDatabaseTablePrefix(), dbmsToUse);
        // Add database specific trunc date function
        properties.put("truncDatepart1", databaseSpecificTruncDatepart1.get(conf.getDatabaseType()));
        properties.put("truncDatepart2", databaseSpecificTruncDatepart2.get(conf.getDatabaseType()));
        return properties;
    }

}
