# Apache TomEE JAX-RS Starter Project

Know JAX-RS, but haven't yet dug into Apache TomEE?  Way too busy or impatient to read documentation?  This repo is for you.

The only thing better than a Maven archetype is a repo you can fork with everything already setup.  Skip the documentation and just fork-and-code.  This starter project contains:

 - 1 JAX-RS class, 1 JAXB class and 1 JUnit/Arquillian test
 - Maven pom for building a war file
 - Arquillian setup for testing against TomEE JAX-RS Embedded
 - TomEE Maven Plugin for deploying and running our war file

Everything ready-to-run with a simple `maven clean install tomee:run`

Delete the sample code, replace with your own and you're good to go.

Have time for some reading and curious on how everything works?  Read here.

## Basic JAX-RS Example

In our project we have one simple JAX-RS Service called `ColorService` which has a few simple `GET` and `POST` methods.


    import javax.ejb.Singleton;
    import javax.ws.rs.GET;
    import javax.ws.rs.POST;
    import javax.ws.rs.Path;
    import javax.ws.rs.PathParam;
    import javax.ws.rs.Produces;

    import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

    @Path("/color")
    @Singleton
    public class ColorService {

        private String color;

        public ColorService() {
            this.color = "white";
        }

        @GET
        public String getColor() {
            return color;
        }

        @Path("{color}")
        @POST
        public void setColor(@PathParam("color") String color) {
            this.color = color;
        }

        @Path("object")
        @GET
        @Produces({ APPLICATION_JSON })
        public Color getColorObject() {
            return new Color("orange", 0xE7, 0x71, 0x00);
        }
    }

We make use of both the default both the `text/plain` and `application/json` mime types so we can show a variety of responses.

In the simple case of returning a `String`, no work is necessary.  We can see our `getColor` method does not specify which mime type it produces.
When `javax.ws.rs.Produces`, the default mime type is `text/plain`.

### Returning JSON Objects

Returning strings is a fine start, but how does one return complex objects formatted in json?  The `getColorObject` method does exactly that by doing two things:

 - Specifies the formatting is json via `@Produces({ APPLICATION_JSON })`
 - Returns a class annotated with JAXB `@XmlRootElement`

With this our work is done and we can now return and accept complex objects using JSON.  The code for our response object is quite simple.


    import javax.xml.bind.annotation.XmlRootElement;

    @XmlRootElement
    public class Color {

        private String name;
        private int r;
        private int g;
        private int b;

        public Color() {
        }

        public Color(String name, int r, int g, int b) {
            this.name = name;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getR() {
            return r;
        }

        public void setR(int r) {
            this.r = r;
        }

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }

We're we to invoke the `getColorObject` method of our `ColorService` via a simple HTTP GET to `/color/object`, we would see the following response.

`{"color":{"b":0,"g":113,"name":"orange","r":231}}`

## Build, Deploy and Excute via TomEE Maven Plugin

Building and Running this example is quite easy.  Simply running a standard Maven `mvn clean install` will build our war file.

    mingus:/tmp/tomee-jaxrs-starter-project 06:06:55
    $ jar tvf target/tomee-rest-arquillian-1.0-SNAPSHOT.war
         0 Wed Jun 25 06:07:52 PDT 2014 META-INF/
       133 Wed Jun 25 06:07:50 PDT 2014 META-INF/MANIFEST.MF
         0 Wed Jun 25 06:07:52 PDT 2014 WEB-INF/
         0 Wed Jun 25 06:07:52 PDT 2014 WEB-INF/classes/
         0 Wed Jun 25 06:07:52 PDT 2014 WEB-INF/classes/org/
         0 Wed Jun 25 06:07:52 PDT 2014 WEB-INF/classes/org/superbiz/
      1310 Wed Jun 25 06:07:50 PDT 2014 WEB-INF/classes/org/superbiz/Color.class
      1114 Wed Jun 25 06:07:50 PDT 2014 WEB-INF/classes/org/superbiz/ColorService.class

Deploying this war file is as easy as copying it into the `webapps/` directory of your Apache TomEE install.  While this is incredibly easy, this can become a little tedious
to repeat over and over again.

Here is where the TomEE Maven Plugin comes in handy.  Simply adding the following plugin to our `war` project in our Maven `pom.xml` will allow us to not just build our war
via Maven, but also to completely setup our TomEE server as well.

    <plugin>
      <groupId>org.apache.openejb.maven</groupId>
      <artifactId>tomee-maven-plugin</artifactId>
      <version>1.6.0.2</version>
      <configuration>
        <tomeeVersion>1.6.0.2</tomeeVersion>
        <tomeeClassifier>jaxrs</tomeeClassifier>
      </configuration>
    </plugin>

With this plugin in place we can now use the following Maven command to build and execute our basic JAX-RS enabled web application.

    mingus:/tmp/tomee-jaxrs-starter-project 06:07:56
    $ mvn clean install tomee:run
    ...[output trimmed]...
    Jun 25, 2014 6:17:44 AM org.apache.openejb.server.cxf.rs.CxfRsHttpListener logEndpoints
    INFO: REST Application: http://localhost:8080/tomee-rest-arquillian-1.0-SNAPSHOT/              -> org.apache.openejb.server.rest.InternalApplication
    Jun 25, 2014 6:17:44 AM org.apache.openejb.server.cxf.rs.CxfRsHttpListener logEndpoints
    INFO:      Service URI: http://localhost:8080/tomee-rest-arquillian-1.0-SNAPSHOT/color         ->  EJB org.superbiz.ColorService
    Jun 25, 2014 6:17:44 AM org.apache.openejb.server.cxf.rs.CxfRsHttpListener logEndpoints
    INFO:               GET http://localhost:8080/tomee-rest-arquillian-1.0-SNAPSHOT/color/        ->      String getColor()
    Jun 25, 2014 6:17:44 AM org.apache.openejb.server.cxf.rs.CxfRsHttpListener logEndpoints
    INFO:               GET http://localhost:8080/tomee-rest-arquillian-1.0-SNAPSHOT/color/object  ->      Color getColorObject()
    Jun 25, 2014 6:17:44 AM org.apache.openejb.server.cxf.rs.CxfRsHttpListener logEndpoints
    INFO:              POST http://localhost:8080/tomee-rest-arquillian-1.0-SNAPSHOT/color/{color} ->      void setColor(String)
    Jun 25, 2014 6:17:44 AM org.apache.coyote.AbstractProtocol start
    INFO: Starting ProtocolHandler ["http-bio-8080"]
    Jun 25, 2014 6:17:44 AM org.apache.coyote.AbstractProtocol start
    INFO: Starting ProtocolHandler ["ajp-bio-8009"]
    Jun 25, 2014 6:17:44 AM org.apache.catalina.startup.Catalina start
    INFO: Server startup in 2529 ms

Our TomEE server and JAX-RS application is now running!  For convenience, TomEE will print the location of all JAX-RS resource URLs and their respective Java method.  To execute
 our `getColorObject()` method we can execute a simple `curl` command like the following.

    mingus:/tmp/tomee-jaxrs-starter-project 06:21:49
    $ curl http://localhost:8080/tomee-rest-arquillian-1.0-SNAPSHOT/color/object
    {"color":{"b":0,"g":113,"name":"orange","r":231}}

To shut down our server, simple Control-C the window where it was launched.

## Setup Arquillian and TomEE JAX-RS Embedded

At this point we've written, built, deployed and executed our basic JAX-RS application on TomEE.  Now the hard question, how do we do this all over again and automate it in a test
 case?

Any experienced developer can attest that several things complicate any real testing efforts:

 - Starting, stopping the server
 - Deploying war files
 - Hardcoding URLs, ports and paths into our Test Case
 - Port conflicts when running in CI environments


Here's where Arquillian and TomEE Embedded come in.  Arquillian is a wonderful testing framework which can handle the business of starting/stopping servers, build/deploying war files
and finally running your tests.  Arquillian can do a considerable amount more, but for the purposes of this tutorial that's more than enough.

The tests themselves are still plain JUnit tests, but with the details of the server completely abstracted.  Let's take a look at our `ColorServiceTest` and see what
Arquillian is doing for us.  The first three sections are the most interesting.

    import org.jboss.arquillian.container.test.api.Deployment;
    import org.jboss.arquillian.junit.Arquillian;
    import org.jboss.arquillian.test.api.ArquillianResource;
    import org.jboss.shrinkwrap.api.ShrinkWrap;
    import org.jboss.shrinkwrap.api.spec.WebArchive;
    import org.junit.runner.RunWith;

    /** [1] **/
    @RunWith(Arquillian.class)
    public class ColorServiceTest extends Assert {

        /** [2] **/
        @Deployment
        public static WebArchive createDeployment() {
            return ShrinkWrap.create(WebArchive.class).addClasses(ColorService.class, Color.class);
        }

        /** [3] **/
        @ArquillianResource
        private URL webappUrl;


At a high level, here's what is happening behind the scenes at each of these items:

 1. JUnit runs and fires up Arquillian which will find TomEE Embedded and start it right there in the same JVM as the test case.  No separate processes.
 2. Arquillian will find all `@Deployment` annotated static methods in our test case and execute them giving us a chance to decide exactly what is deployed into the server.  We can
 of course deploy bit-fat war files or we can be a little more precise and make them on the fly, trimming out all but the most critical classes required to complete the test.
 3. The URL of our deployed application, hosts and ports and all, will be injected back into our testcase via `@ArquillianResource`.  We are now free to fire requests at our
 server using that URL as a base.

Needless to say, an experienced developer will note we've saved days, weeks or months of work.  The result is quite powerful.  We have test case completely abstracted from the server
 itself.  The implications and benefits of that will take a while to sink in -- we don't often get the luxury of that experience.

For our purposes, it means we have a plain Java SE enabled dev environment.  We can run this testcase via Maven or in our IDE without any additional fanciness.  We can run and debug
  our service code like we would any other plain java code.  This is a huge win.

For a larger enterprise or environment, it can also have the benefit of running these same tests again against other servers or other dev, test or QA environments.

### Random TomEE ports via arquillian.xml

To avoid failed tests due to simple things like port conflicts, we add a `src/test/resources/arquillian.xml` file and tell TomEE to use random ports.  Simply specifing `-1` as
the port for all of our Tomcat connectors will do the trick.  The TomEE Arquillian Adapter will pick random available ports and report those back to Arquillian and to the test case
via the `@ArquillianResource` above.

    <arquillian>
      <container qualifier="tomee" default="true">
        <configuration>
          <property name="httpPort">-1</property>
          <property name="stopPort">-1</property>
          <property name="ajpPort">-1</property>
        </configuration>
      </container>
    </arquillian>

Omit this file and TomEE will simply use the default ports of `8080`, etc.

## Test JAX-RS evaluate HTTP Response

With our server up, application deployed and everything ready to go, we still have the work of writing the tests themselves.  Testing JAX-RS can be quite hard if we take the common
approach of using pure HTTP client libraries like Apache HttpClient.  We find ourselves building a lot of URLs, checking a lot of HTTP status codes and reading a lot of `InputStream`s.

    @Test
    public void postAndGet() throws Exception {

        // POST
        {
            final WebClient webClient = WebClient.create(webappUrl.toURI());
            final Response response = webClient.path("color/green").post(null);

            assertEquals(204, response.getStatus());
        }

        // GET
        {
            final WebClient webClient = WebClient.create(webappUrl.toURI());
            final Response response = webClient.path("color").get();

            assertEquals(200, response.getStatus());

            final String content = slurp((InputStream) response.getEntity());

            assertEquals("green", content);
        }
    }

Using the CXF `WebClient` (`org.apache.cxf.jaxrs.client.WebClient`) we can do that and more, but save quite a bit on the usual plumbing of building `HttpGet` and `HttpPost` objects
and dealing with "default" client and pool implementation details.  The act of building up our URLs is also achievable quite elegantly via methods that have a very natural REST-feel.

Invoking our POST and GET methods for updating and reading colors as plain strings using `text/plain` as the content type is quite easy.

## Test CXF WebClient and JSON Response

When it comes to testing JSON, often this is where JAX-RS testing becomes truly difficult.  If one continues on the path of reading `InputStream`s, the road ahead gets quite rough and
 turns into a parsing and marshalling task that is no fun to do even once, let alone repeat.  This is where `WebClient` shines.

Our very same `Color` class that was used to create the JSON response on the server side can be used via the `WebClient` on the client-side in our testcase.  This usage is actually considerably shorter than the `InputStream` equivalent.

    @Test
    public void getColorObject() throws Exception {

        final WebClient webClient = WebClient.create(webappUrl.toURI());
        webClient.accept(MediaType.APPLICATION_JSON);

        final Color color = webClient.path("color/object").get(Color.class);

        assertNotNull(color);
        assertEquals("orange", color.getName());
        assertEquals(0xE7, color.getR());
        assertEquals(0x71, color.getG());
        assertEquals(0x00, color.getB());
    }

The two points to note over the previous `WebClient` usage:

 - `webClient.accept(MediaType.APPLICATION_JSON);` forces the response to be `application/json`.  This is very useful if the JAX-RS `GET` method is capable of returning multiple formats such as `application/xml`.
 - passing `Color.class` into our `get(Color.class)` method is the magic that tells `WebClient` to marshall the JSON into an instance of `Color` which is then returned from the `get()` call instead of `InputStream`.
