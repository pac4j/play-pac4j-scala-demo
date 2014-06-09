## What is this project ?

This **play-pac4j-scala-demo** project is a Play 2.2 Scala project to test the [play-pac4j library](https://github.com/leleuj/play-pac4j) with Facebook, Twitter, form authentication, basic auth, CAS, Google OpenID...  
The **play-pac4j** library is built to delegate authentication to a provider and be authenticated back in the protected application with a complete user profile retrieved from the provider.

## Live demo

Find a live demo on Heroku: [http://play-pac4j-scala-demo.herokuapp.com](http://play-pac4j-scala-demo.herokuapp.com/)

## Quick start & test

To start quickly :

    cd play-pac4j-scala-demo
    play run

To test, you can call a protected url by clicking on the "Protected by **xxx** : **xxx**/index.jsp" url, which will start the authentication process with the **xxx** provider.  
Or you can click on the "Authenticate with **xxx**" link, to start manually the authentication process with the **xxx** provider.

