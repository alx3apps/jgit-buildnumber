package ru.concerteza.util.buildnumber;

import org.apache.tools.ant.Project;

import java.io.IOException;

/**
 * User: alexey
 * Date: 11/16/11
 */

public class JGitBuildNumberAntTask {
    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    public void execute() throws IOException {
        BuildNumber bn = BuildNumberExtractor.extract();
        project.setProperty("git.revision", bn.getRevision());
        project.setProperty("git.shortRevision", bn.getShortRevision());
        project.setProperty("git.branch", bn.getBranch());
        project.setProperty("git.tag", bn.getTag());
        project.setProperty("git.commitsCount", bn.getCommitsCountAsString());
    }
}
