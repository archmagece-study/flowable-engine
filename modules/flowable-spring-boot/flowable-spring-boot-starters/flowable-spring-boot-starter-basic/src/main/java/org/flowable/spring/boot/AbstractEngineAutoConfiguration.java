package org.flowable.spring.boot;

import org.flowable.engine.common.AbstractEngineConfiguration;
import org.flowable.spring.common.SpringEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base auto configuration for the different engines.
 *
 * @author Filip Hrisafov
 * @author Javier Casal
 */
public abstract class AbstractEngineAutoConfiguration {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final FlowableProperties flowableProperties;
    protected ResourcePatternResolver resourcePatternResolver;

    public AbstractEngineAutoConfiguration(FlowableProperties flowableProperties) {
        this.flowableProperties = flowableProperties;
    }

    @Autowired
    public void setResourcePatternResolver(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    protected void configureSpringEngine(SpringEngineConfiguration engineConfiguration, PlatformTransactionManager transactionManager) {
        engineConfiguration.setTransactionManager(transactionManager);
    }

    protected void configureEngine(AbstractEngineConfiguration engineConfiguration, DataSource dataSource) {

        engineConfiguration.setDataSource(dataSource);

        engineConfiguration.setDatabaseSchema(defaultText(flowableProperties.getDatabaseSchema(), engineConfiguration.getDatabaseSchema()));
        engineConfiguration.setDatabaseSchemaUpdate(defaultText(flowableProperties.getDatabaseSchemaUpdate(), engineConfiguration
            .getDatabaseSchemaUpdate()));

        engineConfiguration.setDbHistoryUsed(flowableProperties.isDbHistoryUsed());

        if (flowableProperties.getCustomMybatisMappers() != null) {
            engineConfiguration.setCustomMybatisMappers(getCustomMybatisMapperClasses(flowableProperties.getCustomMybatisMappers()));
        }

        if (flowableProperties.getCustomMybatisXMLMappers() != null) {
            engineConfiguration.setCustomMybatisXMLMappers(new HashSet<>(flowableProperties.getCustomMybatisXMLMappers()));
        }

        if (flowableProperties.getCustomMybatisMappers() != null) {
            engineConfiguration.setCustomMybatisMappers(getCustomMybatisMapperClasses(flowableProperties.getCustomMybatisMappers()));
        }

        if (flowableProperties.getCustomMybatisXMLMappers() != null) {
            engineConfiguration.setCustomMybatisXMLMappers(new HashSet<>(flowableProperties.getCustomMybatisXMLMappers()));
        }
    }

    public List<Resource> discoverDeploymentResources(String prefix, List<String> suffixes, boolean loadResources) throws IOException {
        if (loadResources) {

            List<Resource> result = new ArrayList<>();
            for (String suffix : suffixes) {
                String path = prefix + suffix;
                Resource[] resources = resourcePatternResolver.getResources(path);
                if (resources != null && resources.length > 0) {
                    Collections.addAll(result, resources);
                }
            }

            if (result.isEmpty()) {
                logger.info("No deployment resources were found for autodeployment");
            }

            return result;
        }
        return new ArrayList<>();
    }

    protected Set<Class<?>> getCustomMybatisMapperClasses(List<String> customMyBatisMappers) {
        Set<Class<?>> mybatisMappers = new HashSet<>();
        for (String customMybatisMapperClassName : customMyBatisMappers) {
            try {
                Class customMybatisClass = Class.forName(customMybatisMapperClassName);
                mybatisMappers.add(customMybatisClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Class " + customMybatisMapperClassName + " has not been found.", e);
            }
        }
        return mybatisMappers;
    }

    protected String defaultText(String deploymentName, String defaultName) {
        if (StringUtils.hasText(deploymentName)) {
            return deploymentName;
        }
        return defaultName;
    }

}
