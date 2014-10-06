package ru.concerteza.util.buildnumber.task;

import org.junit.Before;
import org.junit.Test;
import ru.concerteza.util.buildnumber.BuildNumber;
import ru.concerteza.util.buildnumber.utils.GitBuildInformation;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GitBuildInformationTest {

    private GitBuildInformation gitBuildInformation;

    @Before
    public void beforeEachTest() throws IOException {
        BuildNumber buildNumber = new BuildNumber("1234567890", "branch", "tag", "parent", 37);
        gitBuildInformation = new GitBuildInformation(buildNumber);
    }

    @Test
    public void returnsCorrectNumberOfGitCommitsCount() throws IOException {
        assertThat(gitBuildInformation.getCommitsCountAsString(), is("37"));
    }

    @Test
    public void returnsCorrectBranch() throws IOException {
        assertThat(gitBuildInformation.getBranch(), is("branch"));
    }

    @Test
    public void returnsCorrectParent() throws IOException {
        assertThat(gitBuildInformation.getParent(), is("parent"));
    }

    @Test
    public void returnsCorrectRevision() throws IOException {
        assertThat(gitBuildInformation.getRevision(), is("1234567890"));
    }

    @Test
    public void returnsCorrectTag() throws IOException {
        assertThat(gitBuildInformation.getTag(), is("tag"));
    }

    @Test
    public void returnsShortRevision() throws IOException {
        assertThat(gitBuildInformation.getShortRevision(), is("1234567"));
    }

    @Test
    public void returnsCorrectDefaultBuildNumber() throws IOException {
        assertThat(gitBuildInformation.getDefaultBuildNumber(), is("tag.37.1234567"));
    }

    @Test
    public void setsBranchAsNameIfTagIsNotPresent() throws IOException {
        BuildNumber buildNumber = new BuildNumber("1234567890", "branch", "", "parent", 37);
        gitBuildInformation = new GitBuildInformation(buildNumber);

        assertThat(gitBuildInformation.getDefaultBuildNumber(), is("branch.37.1234567"));
    }

    @Test
    public void setsUNNAMEDAsNameIdTagAndBranchAreNotPresent() throws IOException {
        BuildNumber buildNumber = new BuildNumber("1234567890", "", "", "parent", 37);
        gitBuildInformation = new GitBuildInformation(buildNumber);

        assertThat(gitBuildInformation.getDefaultBuildNumber(), is("UNNAMED.37.1234567"));
    }
}
