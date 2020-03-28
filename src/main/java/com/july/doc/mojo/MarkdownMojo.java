package com.july.doc.mojo;

import com.july.doc.JulyDoc;
import com.july.doc.entity.ApiConfig;
import com.july.doc.showdoc.JulyShowDocUtil;
import com.july.doc.showdoc.ShowDoc;
import com.july.doc.showdoc.ShowDocModel;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.List;

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
            List<ShowDocModel> showDocModelList = JulyDoc.generateDocApiPlugin(apiConfig,javaProjectBuilder);
            //上传markdown文档至show doc
            if(apiConfig.getShowDoc() != null){
                ShowDoc showDoc = apiConfig.getShowDoc();
                JulyShowDocUtil.doPost(new ShowDoc(apiConfig.getIsOpenLocal(), showDoc.getShowDocUrl(),
                        showDoc.getApiKey(), showDoc.getApiToken(), showDocModelList));
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
