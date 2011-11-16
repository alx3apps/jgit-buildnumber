package ru.concerteza.util.buildnumber;

/**
 * User: alexey
 * Date: 11/16/11
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Goal which creates build number.
 *
 * @goal extract-buildnumber
 * @phase validate
 */
public class JGitBuildNumberMojo extends AbstractMojo {
    private static final String EMPTY_STRING = "";
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
            // execute only one per build
            // http://www.sonatype.com/people/2009/05/how-to-make-a-plugin-run-once-during-a-build/
            if (executionRootDirectory.equals(baseDirectory)) {
                doExecute();
            }
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

    }

    private void doExecute() throws IOException {
        // open repo
        FileRepository repo = new FileRepositoryBuilder().findGitDir().build();
        // extract HEAD revision
        ObjectId revisionObject = repo.resolve(Constants.HEAD);
        if(null == revisionObject) throw new IOException("Cannot read current revision from repository: " + repo);
        String revision = revisionObject.name();
        // extract current branch
        String branch = readCurrentBranch(repo, revision);
        // extract current tag
        String tag = readCurrentTag(repo, revision);
        // count total commits
        int commitsCount = countCommits(repo, revisionObject);

        getLog().info("Git info extracted, revision: '" + revision + "', branch: '" + branch + "', tag: '" + tag + "', commitsCount: '" + commitsCount + "'");
        // set maven properties
        project.getProperties().setProperty(revisionProperty, revision);
        project.getProperties().setProperty(branchProperty, branch);
        project.getProperties().setProperty(tagProperty, tag);
        project.getProperties().setProperty(commitsCountProperty, Integer.toString(commitsCount));
    }

    private String readCurrentBranch(FileRepository repo, String revision) throws IOException {
        String branch = repo.getBranch();
        getLog().debug("Current git branch: " + branch);
        // should not happen
        if(null == branch) return EMPTY_STRING;
        if(revision.equals(branch)) return EMPTY_STRING;
        return branch;
    }

    private String readCurrentTag(FileRepository repo, String revision) {
        Map<String, String> tagMap = loadTagsMap(repo);
        getLog().debug("Total tags count: " + tagMap.size());
        String tag = tagMap.get(revision);
        if(null == tag) return EMPTY_STRING;
        return tag;
    }

    // sha1 -> tag name
    private Map<String, String> loadTagsMap(FileRepository repo) {
        Map<String, Ref> refMap = repo.getTags();
        Map<String, String> res = new HashMap<String, String>(refMap.size());
        for (Map.Entry<String, Ref> en : refMap.entrySet()) {
            res.put(en.getValue().getObjectId().name(), en.getKey());
        }
        return res;
    }

    // takes about 1 sec to count 69939 in intellijidea repo
    private int countCommits(FileRepository repo, ObjectId revision) throws IOException {
        RevWalk walk = new RevWalk(repo);
        walk.setRetainBody(false);
        RevCommit head = walk.parseCommit(revision);
        walk.markStart(head);
        int res = 0;
        for (RevCommit commit : walk) res += 1;
        return res;
    }
}
