## What is this project ?

This **play-pac4j-scala-demo** project is a Play 2.x Scala project to test the [play-pac4j library](https://github.com/leleuj/play-pac4j) with Facebook, Twitter, form authentication, basic auth, CAS, myopenid.com...  
The **play-pac4j** library is built to delegate authentication to a provider and be authenticated back in the protected application with a complete user profile retrieved from the provider.

**This demo works for the version 2.2 of the Play framework. In that case, the play-pac4j_scala 1.2.0-SNAPSHOT version is required !**

To work with the version 2.0 or 2.1 of the Play framework, please use the branch *play2.0or2.1* of this project...

## Live demo

Find a live demo on Heroku: [http://play-pac4j-scala-demo.herokuapp.com](http://play-pac4j-scala-demo.herokuapp.com/)

## Quick start & test

To start quickly :

    cd play-pac4j-scala-demo
    play run

To test, you can call a protected url by clicking on the "Protected by **xxx** : **xxx**/index.jsp" url, which will start the authentication process with the **xxx** provider.  
Or you can click on the "Authenticate with **xxx**" link, to start manually the authentication process with the **xxx** provider.

If you need to test with CAS, you can easily setup a CAS server by using one of the following CAS demos (use the *-Djetty.port=8888* option to start it on port 8888 as expected by this demo) :
- [cas-overlay-3.5.x](https://github.com/leleuj/cas-overlay-3.5.x) for CAS server version 3.5.x-SNAPSHOT
- [cas-overlay-demo](https://github.com/leleuj/cas-overlay-demo) for CAS server version 4.0.0-SNAPSHOT

