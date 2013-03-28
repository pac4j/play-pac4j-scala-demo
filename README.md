<h2>What is this project ?</h2>

This <b>play-pac4j-scala-demo</b> project is a Play 2.x Scala project to test the <a href="https://github.com/leleuj/play-pac4j">play-pac4j library</a> with Facebook, Twitter, form authentication, basic auth, CAS, myopenid.com...<br />
The <b>play-pac4j</b> library is built to delegate authentication to a provider and be authenticated back in the protected application with a complete user profile retrieved from the provider.

<h2>Quick start & test</h2> 

If you want to use this demo with Play framework <b>2.0.4 or 2.1.0</b>, comment/uncomment the appropriate properties in the *project/build.properties* file :
<pre><code># play 2.1
#sbt.version=0.12.2
# play 2.0.4
#sbt.version=0.11.3</code></pre>
and in the *project/plugins.sbt* file :
<pre><code>// Use the Play sbt plugin for Play projects
// play 2.1 :
//addSbtPlugin("play" % "sbt-plugin" % "2.1.0")
// play 2.0.4 :
// addSbtPlugin("play" % "sbt-plugin" % "2.0.4")</code></pre>
and the appropriate dependency in the *project/Build.scala* file :
<pre><code>//      for play 2.0 :
//      "org.pac4j" % "play-pac4j_scala2.9" % "1.1.0-SNAPSHOT",
//      for play 2.1 :
//      "org.pac4j" % "play-pac4j_scala2.10" % "1.1.0-SNAPSHOT",</code></pre>

To start quickly :<pre><code>cd play-pac4j-java-demo
play run</code></pre>

To test, you can call a protected url by clicking on the "Protected by <b>xxx</b> : <b>xxx</b>/index.html" url, which will start the authentication process with the <b>xxx</b> provider.<br />
Or you can click on the "Authenticate with <b>xxx</b>" link, to start manually the authentication process with the <b>xxx</b> provider.

If you need to test with CAS, you can easily setup a CAS server by using one of the following CAS demos :
- <a href="https://github.com/leleuj/cas-overlay-3.5.x">cas-overlay-3.5.x</a> for CAS server version 3.5.x-SNAPSHOT
- <a href="https://github.com/leleuj/cas-overlay-demo">cas-overlay-demo</a> for CAS server version 4.0.0-SNAPSHOT
