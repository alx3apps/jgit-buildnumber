Git buildnumber plugin for Maven and Ant based on JGit
======================================================

Allows to get git buildnumbers, while building java projects, in pure Java without Git command-line tool.

Note: maven already have ubiquitous buildnumber plugin - http://mojo.codehaus.org/buildnumber-maven-plugin/ , this project is NIH substitute for it with Git support only.

Build number
------------

Build number is project-build's id, it is generated during build process, stored in MANIFEST.MF file. On application startup it is retrived from MANIFEST.MF to be showed in -v ouput or in webpage footer.

In our case buildnumber consists of:

__Human__ __readable__ __id__: tag name or branch name

    git describe --exact-match --tags HEAD # tag name
    git symbolic-ref -q HEAD # branch name

__Globally__ __unique__ __id__: commit sha1

    git rev-parse --short HEAD 

__Build__ __incremental__ __id__: commits count in this branch

    git rev-list --all | wc -l

This project __IS__ __NOT__ using Git CLI commands above, it is using pure java JGit (http://www.jgit.org/) API instead.

Plugin building
---------------

Use

    mvn clean install

to build maven plugin and ant task jars.

Usage in Maven 3
----------------

Note: this plugin accesses Git repo only once during multi-module build.

Plugin config example:

    <!-- enable JGit plugin -->
    <plugin>
        <groupId>ru.concerteza.buildnumber</groupId>
        <artifactId>maven-jgit-buildnumber-plugin</artifactId>
        <version>1.1</version>
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

Maven properties names can be configured:

 * revisionProperty, default: git.revision
 * branchProperty, default: git.branch
 * tagProperty, default: git.tag
 * commitsCountProperty, default: git.commitsCount

Usage in Ant
------------

To use buildnumber ant tasks you need this jars on your classpath (this is maven's JAR descriptions - not an ant config):

    <dependency>
        <groupId>ru.concerteza.buildnumber</groupId>
        <artifactId>jgit-buildnumber-ant-task</artifactId>
        <version>1.1</version>
    </dependency>
    <dependency>
        <groupId>org.eclipse.jgit</groupId>
        <artifactId>org.eclipse.jgit</artifactId>
        <version>1.1.0.201109151100-r</version>
    </dependency>

build.xml usage snippet:

    <!-- jgit buildnumber target -->
    <target name="git-revision" description="Store git buildnumber 'git.revision', 'git.shortRevision', 'git.branch', 'git.tag' and 'git.commitsCount' properties">
        <taskdef name="jgit-buildnumber" classname="ru.concerteza.util.buildnumber.JGitBuildNumberAntTask" classpathref="lib.static.classpath"/>
        <jgit-buildnumber/>
        <echo>Git version extracted ${git.commitsCount} (${git.shortRevision})</echo>
    </target>

License information
-------------------

You can use any code from this project under terms of Apache Licence (http://www.apache.org/licenses/LICENSE-2.0)
