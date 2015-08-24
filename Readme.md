Skinny WARs with Skinny Configuration
=================================================

This maven plugin generates skinny wars to follow the EAR pattern discussed in [AspectJ LTW in WebLogic 12](https://github.com/asegner/spring-ltw-weblogic).
Unlike the skinny war functionality built into maven-ear-plugin, this plugin requires very little configuration.

After installing this plugin to the local maven repository, the calling project pom need only add the following section to its build plugins:
```xml
    <plugin>
        <groupId>communalwar-maven-plugin</groupId>
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

This plugin will be made available in maven central. However, to install locally without the use of maven central, simply download the project and build with
the standard maven command shown below. This will make the plugin available in your local maven repository.

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
