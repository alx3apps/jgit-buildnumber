package ru.concerteza.util.buildnumber;

import org.apache.tools.ant.Project;

import java.io.IOException;

/**
 * Ant task, extracts buildnumber fields from git repository and publishes them as ant properties
 *
 * @author alexey
 * Date: 11/16/11
 * @see BuildNumber
 * @see BuildNumberExtractor
 */
public class JGitBuildNumberAntTask {
    private Project project;

    /**
     * @param project ant project setter
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Extracted properties names:
     * <ul>
     *     <li>{@code git.revision}</li>
     *     <li>{@code git.shortRevision}</li>
     *     <li>{@code git.branch}</li>
     *     <li>{@code git.tag}</li>
     *     <li>{@code git.commitsCount}</li>
     *     <li>{@code git.buildnumber}</li>
     * </ul>
     *
     * @throws IOException
     */
    public void execute() throws IOException {
        BuildNumber bn = BuildNumberExtractor.extract();
        project.setProperty("git.revision", bn.getRevision());
        project.setProperty("git.shortRevision", bn.getShortRevision());
        project.setProperty("git.branch", bn.getBranch());
        project.setProperty("git.tag", bn.getTag());
        project.setProperty("git.commitsCount", bn.getCommitsCountAsString());
        project.setProperty("git.buildnumber", bn.defaultBuildnumber());
    }
}
