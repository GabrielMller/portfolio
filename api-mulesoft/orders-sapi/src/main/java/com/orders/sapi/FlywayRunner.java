package com.orders.sapi;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import javax.sql.DataSource;

public class FlywayRunner {

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void init() {
        ClassLoader appClassLoader = this.getClass().getClassLoader();

        ClassicConfiguration config = new ClassicConfiguration();
        config.setDataSource(this.dataSource);
        
        config.setClassLoader(appClassLoader); 
        config.setLocationsAsStrings("classpath:db/migration");
        
        config.setBaselineOnMigrate(true);

        Flyway flyway = new Flyway(config);
        flyway.migrate();
    }
}