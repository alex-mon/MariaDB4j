/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2014 Michael Vorburger
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ch.vorburger.mariadb4j.springframework.boot;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;

/**
 * Spring Boot based MariaDB4j main() "Application" launcher.
 *
 * @author Michael Vorburger
 * @see MariaDB4jSpringService
 */
@Configuration
@EnableAutoConfiguration
public class MariaDB4jApplication implements ExitCodeGenerator {

    private final MariaDB4jSpringService mariaDB4j;
    private static ConfigurableApplicationContext ctx;
    private static CountDownLatch shutdownLatch;


    @Autowired
    public MariaDB4jApplication(MariaDB4jSpringService mariaDB4j) {
        this.mariaDB4j = mariaDB4j;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(MariaDB4jApplication.class);
        app.setBannerMode(Mode.OFF);
        ctx = app.run(args);

        shutdownLatch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new ShutDownHook());
        System.out.println("Startup Finished. Use Ctrl+C to exit.");
        shutdownLatch.await();
    }

    @Override
    public int getExitCode() {
        return mariaDB4j.getLastException() == null ? 0 : -1;
    }

    private static class ShutDownHook extends Thread {
        public void run() {
            if (null != ctx) {
                ctx.stop();
                ctx.close();
                shutdownLatch.countDown();
            }
        }
    }

}