# Apache TomEE JAX-RS Starter Project

Know JAX-RS, but haven't yet dug into Apache TomEE?  Way too busy or impatient to read documentation?  This repo is for you.

The only thing better than a Maven archetype is a repo you can fork with everything already setup.  Skip the documentation and just fork-and-code.  This starter project contains:

 - 1 JAX-RS class, 1 JAXB class and 1 JUnit/Arquillian test
 - Maven pom for building a war file
 - Arquillian setup for testing against TomEE JAX-RS Embedded
 - TomEE Maven Plugin for deploying and running our war file

Everything ready-to-run with a simple `maven clean install tomee:run`

Delete the sample code, replace with your own and you're good to go.

Have time for some reading and curious on how everything works?  [Read here](http://www.tomitribe.com/blog/2014/06/apache-tomee-jax-rs-and-arquillian-starter-project/).

# Run on heroku

Once checkouted and heroku setup locally (heroku login) just go in the project folder and execute:

    heroku create
    git push heroku heroku:master

At the end of the push command you'll get the heroku base URL, just append /color/object and you'll
touch the JAX6RS service of the application.

Note: for this sample we specified `-Dopenejb.additional.include=tomee-`. This is because by default
TomEE excludes "tomee-*" modules but our project is named "tomee-jaxrs-starter-project".

