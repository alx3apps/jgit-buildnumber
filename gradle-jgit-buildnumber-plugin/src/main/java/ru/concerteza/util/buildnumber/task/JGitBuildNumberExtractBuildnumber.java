package ru.concerteza.util.buildnumber.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import ru.concerteza.util.buildnumber.BuildNumber;
import ru.concerteza.util.buildnumber.BuildNumberExtractor;
import ru.concerteza.util.buildnumber.utils.GitBuildInformation;

import java.io.File;
import java.io.IOException;

public class JGitBuildNumberExtractBuildnumber extends DefaultTask {

    String dir = ".";

    @TaskAction
    public void jGitBuildnumber_ExtractBuildnumber() throws IOException {
        BuildNumber buildNumber = BuildNumberExtractor.extract(new File(dir));
        final GitBuildInformation gitBuildInformation = new GitBuildInformation(buildNumber);
        this.getProject().getExtensions().getExtraProperties().set("gitBranch", gitBuildInformation.getBranch());
        this.getProject().getExtensions().getExtraProperties().set("gitCommitsCount", gitBuildInformation.getCommitsCountAsString());
        this.getProject().getExtensions().getExtraProperties().set("gitTag", gitBuildInformation.getTag());
        this.getProject().getExtensions().getExtraProperties().set("gitRevision", gitBuildInformation.getRevision());
        this.getProject().getExtensions().getExtraProperties().set("gitParent", gitBuildInformation.getParent());
        this.getProject().getExtensions().getExtraProperties().set("gitBuildnumber", gitBuildInformation.getDefaultBuildNumber());
    }
}
