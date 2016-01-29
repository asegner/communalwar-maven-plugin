Skinny WARs with Skinny Configuration (and LTW Ready)
=====================================================

This maven plugin generates skinny WARs with skinny configuration. It will generate an ear with skinny wars using a standard layout or the load time
weaver friendly 'communal war' layout discussed in [AspectJ LTW in WebLogic 12](https://github.com/asegner/spring-ltw-weblogic). Unlike the skinny war functionality
built into maven-ear-plugin, this plugin requires very little configuration.

After installing this plugin to the local maven repository, the calling project pom need only add the following section to its build plugins:


For a communal skinny WAR layout:
```xml
    <plugin>
        <groupId>net.segner.maven.plugins</groupId>
        <artifactId>skinnywar-maven-plugin</artifactId>
        <configuration>
            <communalWar>sharedwar.war</communalWar>
        </configuration>
        <extensions>true</extensions>
    </plugin>

```

For a standard skinny WAR layout

```xml
    <plugin>
        <groupId>net.segner.maven.plugins</groupId>
        <artifactId>skinnywar-maven-plugin</artifactId>
        <extensions>true</extensions>
    </plugin>

```

Without setting extension to true, the plugin may still be used with the slightly more verbose:

```xml
    <plugin>
        <groupId>net.segner.maven.plugins</groupId>
        <artifactId>skinnywar-maven-plugin</artifactId>
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

This plugin requires a minimum maven version of 3.3.x. This plugin is currently in the snapshot repositories and will soon be promoted to the central repository.
Before that time, the plugin can be downloaded by adding the snapshot repository to the project pom

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


## Plugin Usage
--------------------------------------------------

* `communalWar`
  * Name of the war module to treat as the communal war
  * Facilitates layout discussed in [AspectJ LTW in WebLogic 12](https://github.com/asegner/spring-ltw-weblogic)
* `generateWeblogicLtwMetadata`
  * `true` | `false` (default: `true`)
  * Generate metadata to facilitate load time weaving on WebLogic
    * `<classloader-structure>` section of EAR's weblogic-application.xml, creates file if it does not exist
  * Ignored if the communal war layout is not in use
* `warningBreaksBuild`
  * `true` | `false` (default: `true`)
  * Force a warning to fail a build
* `forceAspectJLibToEar`
  * `true` | `false` (default: `true`)
  * Add AspectJ related libraries to the ear libraries list below
* `addToManifestClasspath`
  * `true` | `false` (default: `false`)
  * Inserts references to the shared libraries into the beginning of the MANIFEST.MF Class-Path attribute
* `earLibraries`
  * List of libraries that must be relocated to the EAR, if found (rarely needed)
```xml
<earLibraries>
    <libraryPrefixFilter>spring-webmvc spring-web-</libraryPrefixFilter>
</earLibraries>
```
* `pinnedLibraries`
  * List of libraries that should not be relocated. List should be specified in the same manner as ear libraries.


## Demo Project
--------------------------------------------------
A publicly available demo project illustrating this plugins use may be found here: [Plugin Demo Project](https://github.com/asegner/spring-ltw-weblogic)


## License
--------------------------------------------------
* [MIT License](http://www.opensource.org/licenses/mit-license.php)
