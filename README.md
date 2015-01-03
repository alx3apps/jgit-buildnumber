Git buildnumber plugin for Maven and Ant based on JGit
======================================================

Allows to get git buildnumbers, while building java projects, in pure Java without Git command-line tool.

Note: Maven already has ubiquitous [buildnumber-maven-plugin](http://mojo.codehaus.org/buildnumber-maven-plugin/) , this project is NIH substitution for it with Git support only.

Plugin was added to [Maven central](http://repo1.maven.org/maven2/ru/concerteza/buildnumber/maven-jgit-buildnumber-plugin/).
Maven-generated site is available [here](http://alx3apps.github.com/jgit-buildnumber).

Build number
------------

Build number is project-build's id, it is generated during build process, stored in MANIFEST.MF file. On application startup it is retrived from MANIFEST.MF to be showed in -v ouput or in webpage footer.

In our case buildnumber consists of:

__Human__ __readable__ __id__: tag name or branch name

    git describe --exact-match --tags HEAD # tag name
    git symbolic-ref -q HEAD # branch name

__Globally__ __unique__ __id__: commit sha1

    git rev-parse HEAD # revision
    git rev-parse --short HEAD # short revision

__Build__ __incremental__ __id__: commits count in this branch

    git rev-list HEAD | wc -l

This plugin will extract parameters above and expose them as Maven or Ant properties.
It does __NOT__ use Git CLI commands above, it uses pure java [JGit](http://www.jgit.org/) API instead.

*Note: result properties won't coincide exactly with output of git CLI commands above,
there are differences that are not taken into consideration - checkouted tag or not, all tags returned instead of latest,
commits are counted form HEAD to root without branches*

Usage in Maven 3
----------------

Note: this plugin accesses Git repo only once during multi-module build.

###Store raw buildnumber parts in MANIFEST.MF file

In this case extracted buildnumber parts are stored in manifest as is, and may be read from there on startup and composed into buildnumber.

Plugin config:

    <!-- enable JGit plugin -->
    <plugin>
        <groupId>ru.concerteza.buildnumber</groupId>
        <artifactId>maven-jgit-buildnumber-plugin</artifactId>
        <version>1.2.9</version>
        <executions>
            <execution>
                <id>git-buildnumber</id>
                <goals>
                    <goal>extract-buildnumber</goal>
                </goals>
                <phase>prepare-package</phase>
            </execution>
        </executions>
    </plugin> 
    <!-- specify manifest fields -->
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>2.3.2</version>
        <configuration>
            <archive>
                <manifestEntries>
                    <Specification-Title>${project.name}</Specification-Title>
                    <Specification-Version>${project.version}</Specification-Version>
                    <Specification-Vendor>${project.specification_vendor}</Specification-Vendor>
                    <Implementation-Title>${project.groupId}.${project.artifactId}</Implementation-Title>
                    <Implementation-Version>${git.revision}</Implementation-Version>
                    <Implementation-Vendor>${project.implementation_vendor}</Implementation-Vendor>
                    <X-Git-Branch>${git.branch}</X-Git-Branch>
                    <X-Git-Tag>${git.tag}</X-Git-Tag>
                    <X-Git-Commits-Count>${git.commitsCount}</X-Git-Commits-Count>
                </manifestEntries>
            </archive>
        </configuration>
    </plugin>

Results exmple (from MANIFEST.MF):

    Implementation-Title: ru.concerteza.util.ctz-common-utils
    Implementation-Vendor: Con Certeza LLC
    Implementation-Version: bc810bdf4665d8a294da0d0efb47d98463bf0ff6
    Specification-Title: Con Certeza Common Utilities Library
    Specification-Vendor: Con Certeza LLC
    Specification-Version: 1.3.7
    X-Git-Branch: 
    X-Git-Commits-Count: 30
    X-Git-Tag: 1.3.7

###Create ready to use buildnumber

Plugin may be configured to produce ready-to-use buildnumber into `git.buildnumber` property.
By default buildnumber created as `<tag or branch>.<commitsCount>.<shortRevision>`.

Plugin also support custom buildnumber composition using JavaScript. This feature was added by [plevart](https://github.com/plevart).

JS snippet can be provided to `javaScriptBuildnumberCallback` configuration property. Snippet will be executed
by [Rhino JS engine](http://www.mozilla.org/rhino/) included with JDK6.

Configuration example:

    <plugin>
        <groupId>ru.concerteza.buildnumber</groupId>
        <artifactId>maven-jgit-buildnumber-plugin</artifactId>
        <version>1.2.9</version>
        <executions>
            <execution>
                <id>git-buildnumber</id>
                <goals>
                    <goal>extract-buildnumber</goal>
                </goals>
                <phase>prepare-package</phase>
                <configuration>
                    <javaScriptBuildnumberCallback>
                        tag + "_" + branch + "_" + revision.substring(10, 20) + "_" + shortRevision + "_" + commitsCount*42
                    </javaScriptBuildnumberCallback>
                </configuration>
            </execution>
        </executions>
    </plugin>

`tag`, `branch`, `revision`, `shortRevision` and `commitsCount` are exposed to JavaScript as global variables (`commitsCount` as numeric).

Script engine is initialized only if `javaScriptBuildnumberCallback` is provided so it won't break cross-platform support.

If JS snippet failed to execute, it won't break build process, Rhino error will be printed to Maven output and all properties will get "UNKNOWN" values.

###Maven properties configuration:

 * `revisionProperty`, default: `git.revision`
 * `branchProperty`, default: `git.branch`
 * `tagProperty`, default: `git.tag`
 * `commitsCountProperty`, default: `git.commitsCount`
 * `buildnumberProperty`, default: `git.buildnumber`
 * `repositoryDirectory` -  directory to start searching git root from, should contain '.git' directory
 or be a subdirectory of such directory, deafault: `${project.basedir}`
 * `runOnlyAtExecutionRoot`: setting this parameter to 'false' allows to execute plugin
 in every submodule, not only in root one. Default: `true`.
 This feature was added by [bradszabo](https://github.com/bradszabo).

Usage in Ant
------------

To use buildnumber ant task you need this jars on your classpath:

 - `jgit-buildnumber-ant-task-1.2.9.jar`
 - `org.eclipse.jgit-2.0.0.201206130900-r.jar`

Project directory that contains `.git` directory may be provided with `git.repositoryDirectory` property.
Curent work directory is used by defuault.

Extracted properties are put into:

 - `git.tag`
 - `git.branch`
 - `git.revision`
 - `git.shortRevision`
 - `git.commitsCount`

build.xml usage snippet:

    <target name="git-revision">
        <taskdef name="jgit-buildnumber" classname="ru.concerteza.util.buildnumber.JGitBuildNumberAntTask" classpathref="lib.static.classpath"/>
        <jgit-buildnumber/>
        <echo>Git version extracted ${git.commitsCount} (${git.shortRevision})</echo>
    </target>

###Ready to use buildnumber

Default buildnumber in form `<tag or branch>.<commitsCount>.<shortRevision>` will be put into property `git.buildnumber`.
If you want to customize it, you can use Ant [Script task](http://ant.apache.org/manual/Tasks/script.html) like this:

    <target name="git-revision">
        <taskdef name="jgit-buildnumber" classname="ru.concerteza.util.buildnumber.JGitBuildNumberAntTask" classpathref="lib.static.classpath"/>
        <jgit-buildnumber/>
        <script language="javascript">
            var tag = project.getProperty("git.tag")
            var revision = project.getProperty("git.shortRevision")
            var buildnumber = tag + "_" + revision
            project.setProperty("git.buildnumber", buildnumber)
        </script>
    </target>

Common errors
-------------

This exceptions will be reported by JGit if provided `repositoryDirectory` directory doesn't contain Git repository.

    java.lang.IllegalArgumentException: One of setGitDir or setWorkTree must be called

License information
-------------------

This project is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)