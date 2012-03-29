package ru.concerteza.util.buildnumber;

/**
 * User: alexey
 * Date: 11/16/11
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Goal which creates build number.
 *
 * @goal extract-buildnumber
 * @phase prepare-package
 */
public class JGitBuildNumberMojo extends AbstractMojo {
    /**
     * Revision property name
     *
     * @parameter expression="${revisionProperty}"
     */
    private String revisionProperty = "git.revision";
    /**
     * Branch property name
     *
     * @parameter expression="${branchProperty}"
     */
    private String branchProperty = "git.branch";
    /**
     * Tag property name
     *
     * @parameter expression="${tagProperty}"
     */
    private String tagProperty = "git.tag";
    /**
     * Commits count property name
     *
     * @parameter expression="${commitsCountProperty}"
     */
    private String commitsCountProperty = "git.commitsCount";
    /**
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File baseDirectory;
    /**
     * @parameter expression="${session.executionRootDirectory}"
     * @required
     * @readonly
     */
    private File executionRootDirectory;
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;
     /**
     * The maven parent project.
     *
     * @parameter expression="${project.parent}"
     * @readonly
     */
    private MavenProject parentProject;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Properties props = project.getProperties();
        try {
            // executes only one per build
            // http://www.sonatype.com/people/2009/05/how-to-make-a-plugin-run-once-during-a-build/
            if (executionRootDirectory.equals(baseDirectory)) {
                // build started from this projects root
                BuildNumber bn = BuildNumberExtractor.extract();
                getLog().info("Git info extracted, revision: '" + bn.getRevision() + "', branch: '" + bn.getBranch() +
                        "', tag: '" + bn.getTag() + "', commitsCount: '" + bn.getCommitsCount() + "'");
                props.setProperty(revisionProperty, bn.getRevision());
                props.setProperty(branchProperty, bn.getBranch());
                props.setProperty(tagProperty, bn.getTag());
                props.setProperty(commitsCountProperty, bn.getCommitsCountAsString());
            } else if("pom".equals(parentProject.getPackaging())) {
                // build started from parent, we are in subproject, lets provide parent properties to our project
                Properties parentProps = parentProject.getProperties();
                props.setProperty(revisionProperty, parentProps.getProperty(revisionProperty));
                props.setProperty(branchProperty, parentProps.getProperty(branchProperty));
                props.setProperty(tagProperty, parentProps.getProperty(tagProperty));
                props.setProperty(commitsCountProperty, parentProps.getProperty(commitsCountProperty));
            } else {
                // should not happen
                getLog().warn("Cannot extract JGit version: something wrong with build process, we're not in parent, not in subproject!");
                fillPropsUnknown(props);
            }
        } catch (Exception e) {
            getLog().error(e);
            fillPropsUnknown(props);
        }
    }

    private void fillPropsUnknown(Properties props) {
        props.setProperty(revisionProperty, "UNKNOWN_REVISION");
        props.setProperty(branchProperty, "UNKNOWN_BRANCH");
        props.setProperty(tagProperty, "UNKNOWN_TAG");
        props.setProperty(commitsCountProperty, "-1");
    }
}
