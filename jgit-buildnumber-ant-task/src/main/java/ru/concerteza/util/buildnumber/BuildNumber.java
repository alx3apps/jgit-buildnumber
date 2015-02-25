package ru.concerteza.util.buildnumber;

/**
 * Contains fields, extracted from git repository
 *
 * @author alexey
 * Date: 11/16/11
 * @see BuildNumberExtractor
 */
public class BuildNumber {
    private final String revision;
    private final String branch;
    private final String tag;
    private final String parent;
    private final int commitsCount;
	private final String commitDate;

    /**
     * @param revision git revision
     * @param branch git branch name
     * @param tag git tag name or multiple names concatenated with ";"
     * @param parent git revision or multiple revisions concatenated with ";"
     * @param commitsCount number of commits in current branch
     */
    public BuildNumber(String revision, String branch, String tag, String parent, int commitsCount, String commitDate) {
        this.revision = revision;
        this.branch = branch;
        this.tag = tag;
        this.parent = parent;
        this.commitsCount = commitsCount;
		this.commitDate = commitDate;
    }

    /**
     * @return revision, corresponding git command {@code git rev-parse HEAD}
     */
    public String getRevision() {
        return revision;
    }

    /**
     * @return shortened revision, corresponding git command {@code git rev-parse --short HEAD}
     */
    public String getShortRevision() {
        if(null == revision) return null;
        if(revision.length() > 7) return revision.substring(0, 7);
        return revision;
    }

    /**
     * @return branch name, corresponding git command {@code git symbolic-ref -q HEAD}
     * (output is different, git returns full ref name)
     */
    public String getBranch() {
        return branch;
    }

    /**
     * @return tag name, or multiple names concatenated with ";", corresponding
     * git command {@code git describe --exact-match --tags HEAD} (output is different, git returns latest tag only)
     */
    public String getTag() {
        return tag;
    }

    /**
     * @return parent revision, or multiple revisions concatenated with ";", corresponding
     * git command {@code git log --pretty=%P -n 1 HEAD} (output is different, git separates with spaces )
     */
    public String getParent() {
        return parent;
    }

    //

    /**
     * @return commits count in current branch, corresponding git command {@code git rev-list --all | wc -l}
     * (output is different, git returns commits count in all branches)
     */
    public int getCommitsCount() {
        return commitsCount;
    }

    /**
     * @return commits count in current branch
     */
    public String getCommitsCountAsString() {
        return Integer.toString(commitsCount);
    }

	/**
	 * @return commitDate of current commit
	 */
	public String getCommitDate() { return commitDate; }

    /**
     * @return buildnumber string in form {@code <tag or branch>.<commitsCount>.<shortRevision>}
     */
    public String defaultBuildnumber() {
        final String name;
        if(tag.length() > 0) name = tag;
        else if(branch.length() > 0) name = branch;
        else name = "UNNAMED";
        return String.format("%s.%d.%s", name, commitsCount, getShortRevision());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("JGitBuildNumber");
        sb.append("{revision='").append(revision).append('\'');
        sb.append(", branch='").append(branch).append('\'');
        sb.append(", tag='").append(tag).append('\'');
        sb.append(", parent='").append(parent).append('\'');
        sb.append(", commitsCount=").append(commitsCount);
		sb.append(", commitDate=").append(commitDate);
        sb.append('}');
        return sb.toString();
    }
}
