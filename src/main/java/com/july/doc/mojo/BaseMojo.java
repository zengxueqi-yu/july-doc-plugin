package com.july.doc.mojo;

import com.july.doc.filter.FilterArtifact;
import com.july.doc.constant.GlobalConstants;
import com.july.doc.entity.ApiConfig;
import com.july.doc.entity.ApiDataDictionary;
import com.july.doc.entity.ApiErrorCodeDictionary;
import com.july.doc.entity.SourceCodePath;
import com.july.doc.util.ClassLoaderUtil;
import com.july.doc.utils.CollectionUtil;
import com.july.doc.utils.FileUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.july.doc.util.MojoUtils.GSON;

/**
 * 基础mojo类
 * @author zengxueqi
 * @program july-doc-plugin
 * @since 2020-03-24 19:47
 **/
public abstract class BaseMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(property = "configFile", defaultValue = GlobalConstants.DEFAULT_CONFIG)
    private File configFile;
    @Parameter(property = "projectName")
    private String projectName;
    @Parameter(property = "scope")
    private String scope;
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    private List<MavenProject> reactorProjects;
    @Component
    protected RepositorySystem repositorySystem;
    private DependencyNode rootNode;
    protected JavaProjectBuilder javaProjectBuilder;
    @Parameter(required = false)
    private List excludes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        javaProjectBuilder = buildJavaProjectBuilder();
        javaProjectBuilder.setEncoding(GlobalConstants.DEFAULT_CHARSET);
        ApiConfig apiConfig = buildConfig(configFile, projectName, project, getLog());
        if (apiConfig == null) {
            getLog().info(GlobalConstants.ERROR_MSG);
            return;
        }
        if (!com.july.doc.util.FileUtil.isAbsPath(apiConfig.getOutPath())) {
            apiConfig.setOutPath(project.getBasedir().getPath() + "/" + apiConfig.getOutPath());
            getLog().info("API Documentation output to " + apiConfig.getOutPath());
        } else {
            getLog().info("API Documentation output to " + apiConfig.getOutPath());
        }
        this.executeMojo(apiConfig, javaProjectBuilder);
    }

    public abstract void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder)
            throws MojoExecutionException, MojoFailureException;

    public static ApiConfig buildConfig(File configFile, String projectName, MavenProject project, Log log) throws MojoExecutionException {
        try {
            ClassLoader classLoader = ClassLoaderUtil.getRuntimeClassLoader(project);
            String data = FileUtil.getFileContent(new FileInputStream(configFile));
            ApiConfig apiConfig = GSON.fromJson(data, ApiConfig.class);
            List<ApiDataDictionary> apiDataDictionaries = apiConfig.getDataDictionaries();
            List<ApiErrorCodeDictionary> apiErrorCodes = apiConfig.getErrorCodeDictionaries();
            if (apiErrorCodes != null) {
                apiErrorCodes.forEach(
                        apiErrorCode -> {
                            String className = apiErrorCode.getEnumClassName();
                            apiErrorCode.setEnumClass(getClassByClassName(className, classLoader));
                        }
                );
            }
            if (apiDataDictionaries != null) {
                apiDataDictionaries.forEach(
                        apiDataDictionary -> {
                            String className = apiDataDictionary.getEnumClassName();
                            apiDataDictionary.setEnumClass(getClassByClassName(className, classLoader));
                        }
                );
            }
            if (StringUtils.isBlank(apiConfig.getProjectName())) {
                apiConfig.setProjectName(projectName);
            }
            addSourcePaths(project, apiConfig, log);
            return apiConfig;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    private JavaProjectBuilder buildJavaProjectBuilder() throws MojoExecutionException {
        JavaProjectBuilder javaDocBuilder = new JavaProjectBuilder();
        javaDocBuilder.setEncoding(GlobalConstants.DEFAULT_CHARSET);
        javaDocBuilder.setErrorHandler(e -> getLog().warn(e.getMessage()));
        //addSourceTree
        javaDocBuilder.addSourceTree(new File("src/main/java"));
        //sources.stream().map(File::new).forEach(javaDocBuilder::addSourceTree);
        javaDocBuilder.addClassLoader(ClassLoaderUtil.getRuntimeClassLoader(project));
        loadSourcesDependencies(javaDocBuilder);
        return javaDocBuilder;
    }

    private void loadSourcesDependencies(JavaProjectBuilder javaDocBuilder) throws MojoExecutionException {
        try {
            List<String> currentProjectModules = getCurrentProjectArtifacts(this.project);
            ArtifactFilter artifactFilter = this.createResolvingArtifactFilter();
            ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(this.session.getProjectBuildingRequest());
            buildingRequest.setProject(this.project);
            this.rootNode = this.dependencyGraphBuilder.buildDependencyGraph(buildingRequest, artifactFilter, this.reactorProjects);
            List<DependencyNode> dependencyNodes = this.rootNode.getChildren();
            List<Artifact> artifactList = this.getArtifacts(dependencyNodes);
            artifactList.forEach(artifact -> {
                if (FilterArtifact.ignoreSpringBootArtifact(artifact.getArtifactId())) {
                    return;
                }
                String artifactName = artifact.getGroupId() + ":" + artifact.getArtifactId();
                if (currentProjectModules.contains(artifactName)) {
                    return;
                }
                if (Objects.nonNull(excludes) && excludes.contains(artifactName)) {
                    return;
                }
                Artifact sourcesArtifact = repositorySystem.createArtifactWithClassifier(artifact.getGroupId(),
                        artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), "sources");
                this.loadSourcesDependency(javaDocBuilder, sourcesArtifact);
            });
        } catch (DependencyGraphBuilderException var4) {
            throw new MojoExecutionException("Cannot build project dependency graph", var4);
        }
    }

    private void loadSourcesDependency(JavaProjectBuilder javaDocBuilder, Artifact sourcesArtifact) {
        // create request
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(sourcesArtifact);
        //request.setResolveTransitively(true);
        request.setRemoteRepositories(project.getRemoteArtifactRepositories());
        // resolve deps
        ArtifactResolutionResult result = repositorySystem.resolve(request);

        // load source file into javadoc builder
        result.getArtifacts().forEach(artifact -> {
            try (JarFile jarFile = new JarFile(artifact.getFile())) {
                //getLog().info("jar:" + artifact.getFile().toURI().toURL().toString() );
                for (Enumeration<?> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".java") && !name.endsWith("/package-info.java")) {
                        javaDocBuilder.addSource(
                                new URL("jar:" + artifact.getFile().toURI().toURL().toString() + "!/" + name));
                    }
                }
            } catch (Exception e) {
                getLog().warn("Unable to load jar source " + artifact + " : " + e.getMessage());
            }
        });
    }

    private List<String> getCurrentProjectArtifacts(MavenProject project) {
        if (project.hasParent()) {
            List<String> finalArtifactsName = new ArrayList<>();
            MavenProject mavenProject = project.getParent();
            if (Objects.nonNull(mavenProject)) {
                File file = mavenProject.getBasedir();
                if (!Objects.isNull(file)) {
                    String groupId = mavenProject.getGroupId();
                    List<String> moduleList = mavenProject.getModules();
                    moduleList.forEach(str -> finalArtifactsName.add(groupId + ":" + str));
                }
            }
            return finalArtifactsName;
        } else {
            return new ArrayList<>();
        }
    }

    private ArtifactFilter createResolvingArtifactFilter() {
        ScopeArtifactFilter filter;
        if (this.scope != null) {
            this.getLog().debug("+ Resolving dependency tree for scope '" + this.scope + "'");
            filter = new ScopeArtifactFilter(this.scope);
        } else {
            filter = null;
        }
        return filter;
    }

    private List<Artifact> getArtifacts(List<DependencyNode> dependencyNodes) {
        List<Artifact> artifacts = new ArrayList<>();
        if (CollectionUtil.isEmpty(dependencyNodes)) {
            return artifacts;
        }
        for (DependencyNode dependencyNode : dependencyNodes) {
            if (FilterArtifact.ignoreArtifact(dependencyNode.getArtifact())){
                continue;
            }
            artifacts.add(dependencyNode.getArtifact());
            if (dependencyNode.getChildren().size() > 0) {
                artifacts.addAll(getArtifacts(dependencyNode.getChildren()));
            }
        }
        return artifacts;
    }

    public static Class getClassByClassName(String className, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addSourcePaths(MavenProject project, ApiConfig apiConfig, Log log) {
        List<String> sourceRoots = project.getCompileSourceRoots();
        sourceRoots.forEach(s -> apiConfig.setSourceCodePath(SourceCodePath.path().setPath(s)));
        if (project.hasParent()) {
            MavenProject mavenProject = project.getParent();
            if (null != mavenProject) {
                File file = mavenProject.getBasedir();
                if (!Objects.isNull(file)) {
                    apiConfig.setSourceCodePath(SourceCodePath.path().setPath(file.getPath()));
                }
            }
        }
    }

}
