<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-play.png" width="300" />
</p>

This `play-pac4j-scala-demo` project [![Build Status](https://travis-ci.org/pac4j/play-pac4j-scala-demo.png?branch=master)](https://travis-ci.org/pac4j/play-pac4j-scala-demo) is a Scala Play framework web app to test the [play-pac4j-scala_2.11](https://github.com/pac4j/play-pac4j) security library with various authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...


## Start & test

Build the project and launch the Play app on [http://localhost:9000](http://localhost:9000):

    cd play-pac4j-scala-demo
    bin\activator run

To test, you can call a protected url by clicking on the "Protected url by **xxx**" link, which will start the authentication process with the **xxx** provider.


## Live demo

Find a live demo on Heroku: [http://play-pac4j-scala-demo.herokuapp.com](http://play-pac4j-scala-demo.herokuapp.com/)
