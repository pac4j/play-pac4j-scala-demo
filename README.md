## What is this project?

This `play-pac4j-scala-demo` project is a Java Play framework web app to test the [play-pac4j-scala_2.11](https://github.com/pac4j/play-pac4j) library with various authentication mechanisms: Facebook, Twitter, form authentication, basic auth, CAS...

## Start & test

Build the project and launch the Play app:

    cd play-pac4j-scala-demo
    activator run

To test, you can call a protected url by clicking on the "Protected url by **xxx**" url, which will start the authentication process with the **xxx** provider.  
Or you can click on the "Authenticate with **xxx**" link to manually start the authentication process with the **xxx** provider.

To test the CAS support, you need to start a CAS server on port 8888. Use the following demo: [cas-overlay-demo](https://github.com/leleuj/cas-overlay-demo) with the option: `-Djetty.port=8888`.

## Live demo

Find a live demo on Heroku: [http://play-pac4j-scala-demo.herokuapp.com](http://play-pac4j-scala-demo.herokuapp.com/)
