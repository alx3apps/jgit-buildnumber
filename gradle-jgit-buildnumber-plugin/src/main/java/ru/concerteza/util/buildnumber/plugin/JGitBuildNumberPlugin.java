package ru.concerteza.util.buildnumber.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import ru.concerteza.util.buildnumber.task.*;

public class JGitBuildNumberPlugin implements Plugin<Project> {

    public void apply(Project target) {
        target.getTasks().replace("jGitBuildnumber_ExtractBuildnumber", JGitBuildNumberExtractBuildnumber.class);
    }
}
