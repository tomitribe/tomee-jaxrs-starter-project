/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Arquillian will start the container, deploy all @Deployment bundles, then run all the @Test methods.
 *
 * A strong value-add for Arquillian is that the test is abstracted from the server.
 * It is possible to rerun the same test against multiple adapters or server configurations.
 *
 * A second value-add is it is possible to build WebArchives that are slim and trim and therefore
 * isolate the functionality being tested.  This also makes it easier to swap out one implementation
 * of a class for another allowing for easy mocking.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ColorResourceTest {
    private static final String JSON_PROVIDER_CLASS = "org.apache.johnzon.jaxrs.JohnzonProvider";

    /**
     * ShrinkWrap is used to create a war file on the fly.
     *
     * The API is quite expressive and can build any possible
     * flavor of war file.  It can quite easily return a rebuilt
     * war file as well.
     *
     * More than one @Deployment method is allowed.
     */
    @Deployment(testable=false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(Color.class, ColorApplication.class, ColorResource.class);
    }

    /**
     * This URL will contain the following URL data
     *
     *  - http://<host>:<port>/<webapp>/
     *
     * This allows the test itself to be agnostic of server information or even
     * the name of the webapp.
     */
    private @ArquillianResource URL webappUrl;

    private Client client;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        client = createResourceClient();
        target = buildResourceTarget(client);
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void postAndGet() throws Exception {
        // GET
        {
            final Response response = target.path("color").request().get();
            assertEquals(200, response.getStatus());
            final String content = response.readEntity(String.class);
            assertNotNull(content);
            assertEquals("white", content);
        }

        // POST
        {
            final Response response = target.path("color/green").request().post(null);
            assertEquals(204, response.getStatus());
        }

        // GET
        {
            final Response response = target.path("color").request().get();
            assertEquals(200, response.getStatus());
            final String content = response.readEntity(String.class);
            assertNotNull(content);
            assertEquals("green", content);
        }
    }

    @Test
    public void getColorObject() throws Exception {
        final Response response = target.path("color/object").request(MediaType.APPLICATION_JSON).get();
        final Color color = response.readEntity(Color.class);
        assertNotNull(color);
        assertEquals("orange", color.getName());
        assertEquals(0xE7, color.getR());
        assertEquals(0x71, color.getG());
        assertEquals(0x00, color.getB());
    }

    private Client createResourceClient() {
        final ClientBuilder client = ClientBuilder.newBuilder();
        try {
            // client side
            client.register(Class.forName(JSON_PROVIDER_CLASS));
        }
        catch (ClassNotFoundException e) {}
        return client.build();
    }

    private WebTarget buildResourceTarget(Client client) throws Exception {
        return client.target(webappUrl.toURI()).path(getApplicationPath());
    }

    private String getApplicationPath() {
        return ColorApplication.class.getAnnotation(ApplicationPath.class).value();
    }
}
