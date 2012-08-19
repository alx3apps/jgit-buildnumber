package ru.concerteza.util.buildnumber;

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
 * Extracts buildnumber fields from git repository. Put it here, not in common module, because we don't want
 * any ant task dependencies except jgit and ant
 *
 * @author alexey
 * Date: 11/16/11
 * @see BuildNumber
 */
public class BuildNumberExtractor {
    private static final String EMPTY_STRING = "";

    /**
     * @param repoDirectory directory to start searching git root from, should contain '.git' directory
     *                      or be a subdirectory of such directory
     * @return extracted buildnumber object
     * @throws IOException
     */
    public static BuildNumber extract(File repoDirectory) throws IOException {
        if(!(repoDirectory.exists() && repoDirectory.isDirectory())) throw new IOException(
                "Invalid repository directory provided: " + repoDirectory.getAbsolutePath());
        // open repo
        FileRepository repo = new FileRepositoryBuilder().findGitDir(repoDirectory).build();
        // extract HEAD revision
        ObjectId revisionObject = repo.resolve(Constants.HEAD);
        if (null == revisionObject) throw new IOException("Cannot read current revision from repository: " + repo);
        String revision = revisionObject.name();
        // extract current branch
        String branch = readCurrentBranch(repo, revision);
        // extract current tag
        String tag = readCurrentTag(repo, revision);
        // count total commits
        int commitsCount = countCommits(repo, revisionObject);
        return new BuildNumber(revision, branch, tag, commitsCount);
    }

    private static String readCurrentBranch(FileRepository repo, String revision) throws IOException {
        String branch = repo.getBranch();
        // should not happen
        if (null == branch) return EMPTY_STRING;
        if (revision.equals(branch)) return EMPTY_STRING;
        return branch;
    }

    private static String readCurrentTag(FileRepository repo, String revision) {
        Map<String, String> tagMap = loadTagsMap(repo);
        String tag = tagMap.get(revision);
        if (null == tag) return EMPTY_STRING;
        return tag;
    }

    // sha1 -> tag name
    private static Map<String, String> loadTagsMap(FileRepository repo) {
        Map<String, Ref> refMap = repo.getTags();
        Map<String, String> res = new HashMap<String, String>(refMap.size());
        for (Map.Entry<String, Ref> en : refMap.entrySet()) {
            String sha1 = en.getValue().getObjectId().name();
            String existed = res.get(sha1);
            String value;
            if (null == existed) {
                value = en.getKey();
            } else {
                value = existed + ";" + en.getKey();
            }
            res.put(sha1, value);
        }
        return res;
    }

    // takes about 1 sec to count 69939 in intellijidea repo
    private static int countCommits(FileRepository repo, ObjectId revision) throws IOException {
        RevWalk walk = new RevWalk(repo);
        walk.setRetainBody(false);
        RevCommit head = walk.parseCommit(revision);
        walk.markStart(head);
        int res = 0;
        for (RevCommit commit : walk) res += 1;
        return res;
    }
}
