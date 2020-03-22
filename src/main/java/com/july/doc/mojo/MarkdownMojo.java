package com.july.doc.mojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.july.doc.JulyDoc;
import com.july.doc.constant.GlobalConstants;
import com.july.doc.entity.ApiConfig;
import com.july.doc.entity.SourceCodePath;
import com.july.doc.showdoc.JulyShowDocUtil;
import com.july.doc.showdoc.ShowDoc;
import com.july.doc.showdoc.ShowDocModel;
import com.july.doc.utils.CollectionUtil;
import com.july.doc.utils.FileUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * 生成markdown文档信息
 * @author zengxueqi
 * @program july-doc-plugin
 * @since 2020-03-22 14:10
 **/
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "markdown")
public class MarkdownMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(property = "configFile", defaultValue = GlobalConstants.DEFAULT_CONFIG)
    private File configFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<String> sourceRoots = project.getCompileSourceRoots();

        try{
            String data = FileUtil.getFileContent(new FileInputStream(configFile));
            ApiConfig apiConfig = JSON.parseObject(data, ApiConfig.class);
            apiConfig.setSourceCodePath(SourceCodePath.path().setPath(CollectionUtil.isEmpty(sourceRoots) ? "src/main/java" : sourceRoots.get(0)));
            //获取所有的Markdown文件
            List<ShowDocModel> showDocModels = JulyDoc.generateOneApi(apiConfig);
            ShowDoc showDoc = apiConfig.getShowDoc();
            if(showDoc != null){
                //上传markdown文档至show doc
                JulyShowDocUtil.doPost(new ShowDoc(apiConfig.getIsOpenLocal(), showDoc.getShowDocUrl(),
                        showDoc.getApiKey(), showDoc.getApiToken(), showDocModels));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
