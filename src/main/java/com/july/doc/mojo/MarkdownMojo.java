package com.july.doc.mojo;

import com.july.doc.JulyDoc;
import com.july.doc.entity.ApiConfig;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 生成markdown文档信息
 * @author zengxueqi
 * @program july-doc-plugin
 * @since 2020-03-22 14:10
 **/
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "markdown")
public class MarkdownMojo extends BaseMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        try {
            JulyDoc.generateDocApiPlugin(apiConfig,javaProjectBuilder);
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
