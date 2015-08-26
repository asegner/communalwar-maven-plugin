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


## Installing Plugin Locally
--------------------------------------------------

This plugin is currently in the snapshot repositories and will soon be promoted to the central repository. Before that time, the
plugin can be downloaded by adding the snapshot repository to the project pom

```xml
    <pluginRepositories>
    ...
        <pluginRepository>
            <id>ossrh</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        </pluginRepository>
    </pluginRepositories>
```

A local install can be run simply by downloading the project and building with
the standard maven command shown below. This will install the plugin into your local maven repository

`$ mvn clean install`


## Current assumptions
--------------------------------------------------
* The calling project pom is declaring all WebModules in maven-ear-plugin with unpack set to true
```xml
<webModule>
    ...
    <unpack>true</unpack>
</webModule>
```


## Plugin Usage
--------------------------------------------------

* `communalWar`
  * Name of the war module to treat as the communal war
* `warningBreaksBuild`
  * `true` | `false` (default: `true`)
  * Force a warning to fail a build
* `forceAspectJLibToEar`
  * `true` | `false` (default: `true`)
  * Add AspectJ related libraries to the ear libraries list below
* `earLibraries`
  * List of libraries that should be relocated to the EAR
```xml
<earLibraries>
    <library><prefix>spring-webmvc</prefix></library>
</earLibraries>
```
* `pinnedLibraries`
  * List of libraries that should not be relocated. List should be specified in the same manner as ear libraries.


## License
--------------------------------------------------
* [MIT License](http://www.opensource.org/licenses/mit-license.php)
