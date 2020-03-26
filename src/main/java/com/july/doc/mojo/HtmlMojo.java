package com.july.doc.mojo;

import com.july.doc.JulyDoc;
import com.july.doc.entity.ApiConfig;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

/**
 * 生成markdown文档信息
 * @author zengxueqi
 * @program july-doc-plugin
 * @since 2020-03-22 14:10
 **/
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "html")
public class HtmlMojo extends BaseMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) throws MojoExecutionException, MojoFailureException {
        try {
            JulyDoc.generateHtmlApiPlugin(apiConfig,javaProjectBuilder);
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
