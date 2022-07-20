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
package org.superbiz.rest;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.model.Color;
import org.superbiz.services.api.ColorTransformService;
import org.superbiz.services.impl.ColorTransformServiceImpl;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;

/**
 * Arquillian will start the container, deploy all @Deployment bundles, then run all the @Test methods.
 *
 * A strong value-add for Arquillian is that the test is abstracted from the server.
 * It is possible to rerun the same test against multiple adapters or server configurations.
 *
 * A second value-add is it is possible to build WebArchives that are slim and trim and therefore
 * isolate the functionality being tested.  This also makes it easier to swap out one implementation
 * of a class for another allowing for easy mocking.
 *
 */
@RunWith(Arquillian.class)
public class ColorResourceTest extends Assert {

    /**
     * ShrinkWrap is used to create a war file on the fly.
     *
     * The API is quite expressive and can build any possible
     * flavor of war file.  It can quite easily return a rebuilt
     * war file as well.
     *
     * More than one @Deployment method is allowed.
     */
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClasses(ColorResource.class, Color.class,
                ColorTransformService.class,
                ColorTransformServiceImpl.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml"); // needed for CDI to work in arquilian test :)
    }

    /**
     * This URL will contain the following URL data
     *
     *  - http://<host>:<port>/<webapp>/
     *
     * This allows the test itself to be agnostic of server information or even
     * the name of the webapp
     *
     */
    @ArquillianResource
    private URL webappUrl;


    @Test
    public void postAndGet() throws Exception {

        // POST
        {
            final WebTarget webTarget = ClientBuilder.newClient().target(webappUrl.toURI());
            final Response response = webTarget.path("color/green").request().post(null);

            assertEquals(204, response.getStatus());
        }

        // GET
        {
            final WebTarget webTarget = ClientBuilder.newClient().target(webappUrl.toURI());
            final Response response = webTarget.path("color").request().get();

            assertEquals(200, response.getStatus());

            final String content = response.readEntity(String.class);

            assertEquals("green", content);
        }

    }

    @Test
    public void getColorObject() throws Exception {

        final WebTarget webTarget = ClientBuilder.newClient().target(webappUrl.toURI());

        final Color color = webTarget.path("color/object").request()
                .accept(MediaType.APPLICATION_JSON)
                .get(Color.class);

        assertNotNull(color);
        assertEquals("orange", color.getName());
        assertEquals(0xE7, color.getR());
        assertEquals(0x71, color.getG());
        assertEquals(0x00, color.getB());
    }


    @Test
    public void getTransformedColorObject() throws Exception {

        final WebClient webClient = WebClient.create(webappUrl.toURI());
        webClient.accept(MediaType.APPLICATION_JSON);

        final Color color = webClient.path("color/transform/object").get(Color.class);

        assertNotNull(color);
        assertEquals("CMYKorange", color.getName());
        assertEquals(0xE7, color.getR());
        assertEquals(0x71, color.getG());
        assertEquals(0x00, color.getB());
    }

    /**
     * Reusable utility method
     * Move to a shared class or replace with equivalent
     */
    public static String slurp(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
        return new String(out.toByteArray());
    }

}
