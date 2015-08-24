Skinny WARs with Skinny Configuration (and LTW Ready)
=====================================================

This maven plugin generates skinny WARs intended to facilitate the EAR pattern discussed in [AspectJ LTW in WebLogic 12](https://github.com/asegner/spring-ltw-weblogic).
Unlike the skinny war functionality built into maven-ear-plugin, this plugin requires very little configuration.

After installing this plugin to the local maven repository, the calling project pom need only add the following section to its build plugins:
```xml
    <plugin>
        <groupId>net.segner.maven.plugins</groupId>
        <artifactId>communalwar-maven-plugin</artifactId>
        <configuration>
            <communalWar>sharedwar.war</communalWar>
        </configuration>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>ear</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

```

Installing Plugin Locally
--------------------------------------------------

This plugin will soon be made available in maven central. Before that time, a local install can be run simply by downloading the project and building with
the standard maven command shown below. This will install the plugin into your local maven repository

```
mvn clean install
```

Current assumptions
--------------------------------------------------
* The calling project pom is declaring all WebModules in maven-ear-plugin with unpack set to true
```xml
<webModule>
    ...
    <unpack>true</unpack>
</webModule>
```
