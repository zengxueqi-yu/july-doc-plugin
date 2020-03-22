package com.july.doc.util;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * 读取配置信息
 * @author zengxueqi
 * @program july-doc-plugin
 * @since 2020-03-22 12:05
 **/
public class ClassLoaderUtil {

    public static ClassLoader getRuntimeClassLoader(MavenProject project) throws MojoExecutionException {
        try {
            List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
            List<String> compileClasspathElements = project.getCompileClasspathElements();
            URL[] runtimeUrls = new URL[runtimeClasspathElements.size() + compileClasspathElements.size()];
            for (int i = 0; i < runtimeClasspathElements.size(); i++) {
                String element = runtimeClasspathElements.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }

            int j = runtimeClasspathElements.size();

            for (int i = 0; i < compileClasspathElements.size(); i++) {
                String element = compileClasspathElements.get(i);
                runtimeUrls[i + j] = new File(element).toURI().toURL();
            }
            return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to load project runtime !", e);
        }
    }

}
