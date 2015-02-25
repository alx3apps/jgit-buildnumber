package ru.concerteza.util.buildnumber;

/**
 * User: alexey
 * Date: 11/16/11
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
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
     * Short revision property name
     *
     * @parameter expression="${shortRevisionProperty}"
     */
    private String shortRevisionProperty = "git.shortRevision";

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
     * Parent property name
     *
     * @parameter expression="${parentProperty}"
     */
    private String parentProperty = "git.parent";
    /**
     * Commits count property name
     *
     * @parameter expression="${commitsCountProperty}"
     */
    private String commitsCountProperty = "git.commitsCount";
    /**
     * Buildnumber property name
     *
     * @parameter expression="${buildnumberProperty}"
     */
    private String buildnumberProperty = "git.buildnumber";
	/**
     * commitDate property name
     *
     * @parameter expression="${commitDateProperty}"
     */
    private String commitDateProperty = "git.commitDate";
    /**
     * Java Script buildnumber callback
     *
     * @parameter expression="${javaScriptBuildnumberCallback}"
     */
    private String javaScriptBuildnumberCallback = null;
    /**
     * Setting this parameter to 'false' allows to execute plugin in every
     * submodule, not only in root one.
     *
     * @parameter expression="${runOnlyAtExecutionRoot}" default-value="true"
     */
    private boolean runOnlyAtExecutionRoot;
    /**
     * Directory to start searching git root from, should contain '.git' directory
     * or be a subdirectory of such directory. '${project.basedir}' is used by default.
     *
     * @parameter expression="${repositoryDirectory}" default-value="${project.basedir}"
     */
    private File repositoryDirectory;
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

    /**
     * Extracts buildnumber fields from git repository and publishes them as maven properties.
     * Executes only once per build. Return default (unknown) buildnumber fields on error.
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Properties props = project.getProperties();
        try {
            // executes only once per build
            // http://www.sonatype.com/people/2009/05/how-to-make-a-plugin-run-once-during-a-build/
            if (executionRootDirectory.equals(baseDirectory) || !runOnlyAtExecutionRoot) {
                // build started from this projects root
                BuildNumber bn = BuildNumberExtractor.extract(repositoryDirectory);
                props.setProperty(revisionProperty, bn.getRevision());
                props.setProperty(shortRevisionProperty, bn.getShortRevision());
                props.setProperty(branchProperty, bn.getBranch());
                props.setProperty(tagProperty, bn.getTag());
                props.setProperty(parentProperty, bn.getParent());
                props.setProperty(commitsCountProperty, bn.getCommitsCountAsString());
				props.setProperty(commitDateProperty, bn.getCommitDate());
                // create composite buildnumber
                String composite = createBuildnumber(bn);
                props.setProperty(buildnumberProperty, composite);
                getLog().info("Git info extracted, revision: '" + bn.getShortRevision() + "', branch: '" + bn.getBranch() +
                        "', tag: '" + bn.getTag() + "', commitsCount: '" + bn.getCommitsCount() + "', commitDate: '" + bn.getCommitDate() + "', buildnumber: '" + composite + "'");
            } else if("pom".equals(parentProject.getPackaging())) {
                // build started from parent, we are in subproject, lets provide parent properties to our project
                Properties parentProps = parentProject.getProperties();
                String revision = parentProps.getProperty(revisionProperty);
                if(null == revision) {
                    // we are in subproject, but parent project wasn't build this time,
                    // maybe build is running from parent with custom module list - 'pl' argument
                    getLog().info("Cannot extract Git info, maybe custom build with 'pl' argument is running");
                    fillPropsUnknown(props);
                    return;
                }
                props.setProperty(revisionProperty, revision);
                props.setProperty(shortRevisionProperty, parentProps.getProperty(shortRevisionProperty));
                props.setProperty(branchProperty, parentProps.getProperty(branchProperty));
                props.setProperty(tagProperty, parentProps.getProperty(tagProperty));
                props.setProperty(parentProperty, parentProps.getProperty(parentProperty));
                props.setProperty(commitsCountProperty, parentProps.getProperty(commitsCountProperty));
                props.setProperty(buildnumberProperty, parentProps.getProperty(buildnumberProperty));
				props.setProperty(commitDateProperty, parentProps.getProperty(commitDateProperty));
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
        props.setProperty(shortRevisionProperty, "UNKNOWN_REVISION");
        props.setProperty(branchProperty, "UNKNOWN_BRANCH");
        props.setProperty(tagProperty, "UNKNOWN_TAG");
        props.setProperty(parentProperty, "UNKNOWN_PARENT");
        props.setProperty(commitsCountProperty, "-1");
        props.setProperty(buildnumberProperty, "UNKNOWN_BUILDNUMBER");
		props.setProperty(commitDateProperty, "UNKNOWN_COMMIT_DATE");
    }

    private String createBuildnumber(BuildNumber bn) throws ScriptException {
        if(null != javaScriptBuildnumberCallback) return buildnumberFromJS(bn);
        return bn.defaultBuildnumber();
    }

    private String buildnumberFromJS(BuildNumber bn) throws ScriptException {
        ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        jsEngine.put("tag", bn.getTag());
        jsEngine.put("branch", bn.getBranch());
        jsEngine.put("revision", bn.getRevision());
        jsEngine.put("parent", bn.getParent());
        jsEngine.put("shortRevision", bn.getShortRevision());
        jsEngine.put("commitsCount", bn.getCommitsCount());
		jsEngine.put("commitDate", bn.getCommitDate());
        Object res = jsEngine.eval(javaScriptBuildnumberCallback);
        if(null == res) throw new IllegalStateException("JS buildnumber callback returns null");
        return res.toString();
    }
}
