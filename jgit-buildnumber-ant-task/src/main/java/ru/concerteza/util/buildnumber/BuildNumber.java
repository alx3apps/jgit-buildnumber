package ru.concerteza.util.buildnumber;

/**
 * User: alexey
 * Date: 11/16/11
 */
public class BuildNumber {
    private final String revision;
    private final String branch;
    private final String tag;
    private final int commitsCount;

    public BuildNumber(String revision, String branch, String tag, int commitsCount) {
        this.revision = revision;
        this.branch = branch;
        this.tag = tag;
        this.commitsCount = commitsCount;
    }

    // git rev-parse HEAD
    public String getRevision() {
        return revision;
    }

    // git rev-parse --short HEAD
    public String getShortRevision() {
        if(null == revision) return null;
        if(revision.length() > 7) return revision.substring(0, 7);
        return revision;
    }

    // git symbolic-ref -q HEAD
    public String getBranch() {
        return branch;
    }

    // git describe --exact-match --tags HEAD
    public String getTag() {
        return tag;
    }

    // git rev-list --all | wc -l
    public int getCommitsCount() {
        return commitsCount;
    }

    public String getCommitsCountAsString() {
        return Integer.toString(commitsCount);
    }

    public String defaultBuildnumber() {
        final String name;
        if(tag.length() > 0) name = tag;
        else if(branch.length() > 0) name = branch;
        else name = "UNNAMED";
        return String.format("%s.%d.%s", name, commitsCount, getShortRevision());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("JGitBuildNumber");
        sb.append("{revision='").append(revision).append('\'');
        sb.append(", branch='").append(branch).append('\'');
        sb.append(", tag='").append(tag).append('\'');
        sb.append(", commitsCount=").append(commitsCount);
        sb.append('}');
        return sb.toString();
    }
}
