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

/**
 * Goal which creates build number.
 *
 * @goal extract-buildnumber
 * @phase validate
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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // executes only one per build
            // http://www.sonatype.com/people/2009/05/how-to-make-a-plugin-run-once-during-a-build/
            if (executionRootDirectory.equals(baseDirectory)) {
                BuildNumber bn = BuildNumberExtractor.extract();
                getLog().info("Git info extracted, revision: '" + bn.getRevision() + "', branch: '" + bn.getBranch() +
                        "', tag: '" + bn.getTag() + "', commitsCount: '" + bn.getCommitsCount() + "'");
                project.getProperties().setProperty(revisionProperty, bn.getRevision());
                project.getProperties().setProperty(branchProperty, bn.getBranch());
                project.getProperties().setProperty(tagProperty, bn.getTag());
                project.getProperties().setProperty(commitsCountProperty, bn.getCommitsCountAsString());
            }
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
