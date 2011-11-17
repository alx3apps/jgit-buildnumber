package ru.concerteza.util.buildnumber;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: alexey
 * Date: 11/16/11
 */

// put it here, not in common module, because we don't want
// ant task to had dependencies except jgit and ant
public class BuildNumberExtractor {
    private static final String EMPTY_STRING = "";

    public static BuildNumber extract() throws IOException {
        // open repo
        FileRepository repo = new FileRepositoryBuilder().findGitDir().build();
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
