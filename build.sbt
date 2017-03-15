name := "crawler1"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.google.code.gson" % "gson" % "2.2.4",
  "com.google.apis" % "google-api-services-webmasters" % "v3-rev19-1.22.0",
  "org.apache.poi" % "poi-ooxml" % "3.11",
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",
  "com.googlecode.libphonenumber" % "libphonenumber" % "8.3.1",
  "org.jsoup" % "jsoup" % "1.7.2",
  "mysql" % "mysql-connector-java" % "5.1.34",
  "junit" % "junit" % "4.12",
  "com.novocode" % "junit-interface" % "0.10" % "test",
  "org.codehaus.groovy" % "groovy-all" % "1.8.2"
)
    