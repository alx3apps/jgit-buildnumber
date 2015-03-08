package ru.concerteza.util.buildnumber.utils;

import ru.concerteza.util.buildnumber.BuildNumber;
import ru.concerteza.util.buildnumber.BuildNumberExtractor;

import java.io.File;
import java.io.IOException;

public class GitBuildInformation {

    private BuildNumber buildNumber;

    public GitBuildInformation(BuildNumber buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getCommitsCountAsString() throws IOException {
        return buildNumber.getCommitsCountAsString();
    }

    public String getBranch() {
        return buildNumber.getBranch();
    }

    public String getParent() {
        return buildNumber.getParent();
    }

    public String getRevision() {
        return buildNumber.getRevision();
    }

    public String getShortRevision() {
        return buildNumber.getShortRevision();
    }

    public String getTag() {
        return buildNumber.getTag();
    }

    public String getDefaultBuildNumber() throws IOException {
        return buildNumber.defaultBuildnumber();
    }
}
