package com.july.doc.filter;

import com.july.doc.constant.GlobalConstants;
import org.apache.maven.artifact.Artifact;

/**
 * 过滤某些依赖
 * @author zengxueqi
 * @program july-doc-plugin
 * @since 2020-03-25 11:10
 **/
public class FilterArtifact {

    /**
     * 过滤不需要获取源码的依赖
     * @param artifact
     * @return java.lang.Boolean
     * @author zengxueqi
     * @since 2020/3/25
     */
    public static Boolean ignoreArtifact(Artifact artifact) {
        if (GlobalConstants.SCOPE_TEST.equals(artifact.getScope())) {
            return true;
        }
        String artifactId = artifact.getArtifactId();
        if(ignoreSpringBootArtifact(artifactId)
                || ignoreStartArtifact(artifactId)
                || ignoreContainArtifact(artifactId)
                || ignoreOtherArtifact(artifactId)){
            return true;
        }
        return false;
    }

    /**
     * 过滤某些springboot开头的依赖
     * @param artifactId
     * @return java.lang.Boolean
     * @author zengxueqi
     * @since 2020/3/25
     */
    public static Boolean ignoreSpringBootArtifact(String artifactId) {
        switch (artifactId) {
            case "spring-boot":
            case "spring-boot-starter-actuator":
            case "spring-boot-starter":
            case "spring-boot-starter-undertow":
            case "spring-boot-starter-aop":
            case "spring-boot-starter-json":
            case "spring-boot-starter-web":
            case "spring-boot-starter-data-redis":
            case "spring-boot-dingtalk-robot-starter":
            case "spring-boot-starter-logging":
                return true;
            default:
                return false;
        }
    }

    /**
     * 过滤某些包含的依赖
     * @param artifactId
     * @return java.lang.Boolean
     * @author zengxueqi
     * @since 2020/3/25
     */
    public static Boolean ignoreContainArtifact(String artifactId) {
        if (artifactId.contains("log4j")
                || artifactId.contains("logback")
                || artifactId.contains("mybatis")
                || artifactId.contains("nacos")
                || artifactId.contains("json")
                || artifactId.contains("jjwt")
                || artifactId.contains("redis")
                || artifactId.contains("job")
                || artifactId.contains("excel")
                || artifactId.contains("slf4j")) {
            return true;
        }
        return false;
    }

    /**
     * 过滤某些开头的依赖
     * @param artifactId
     * @return boolean
     * @author zengxueqi
     * @since 2020/3/25
     */
    public static boolean ignoreStartArtifact(String artifactId) {
        if (artifactId.startsWith("maven")) {
            return true;
        }
        if (artifactId.startsWith("asm")) {
            return true;
        }
        if (artifactId.startsWith("tomcat") ||
                artifactId.startsWith("jboss") ||
                artifactId.startsWith("undertow")) {
            return true;
        }
        if (artifactId.startsWith("jackson")) {
            return true;
        }
        if (artifactId.startsWith("micrometer") ||
                artifactId.startsWith("spring-boot-actuator")) {
            return true;
        }
        if (artifactId.startsWith("sharding")) {
            return true;
        }
        return false;
    }

    /**
     * 获取某些指定依赖
     * @param artifactId
     * @return java.lang.Boolean
     * @author zengxueqi
     * @since 2020/3/25
     */
    public static Boolean ignoreOtherArtifact(String artifactId){
        switch (artifactId) {
            case "itextpdf":
            case "itext-asian":
            case "xxl-job-core":
            case "com.flex.reflection":
            case "tools-excel":
            case "easyexcel":
            case "spring-cloud-starter-alicloud-oss":
            case "spring-cloud-starter-alicloud-sms":
            case "easypoi-web":
            case "jna-platform":
            case "jna":
            case "mybatis-plus-generator":
            case "bcprov-jdk15on":
            case "lombok":
            case "jsqlparser":
            case "disruptor":
            case "commons-codec":
            case "snakeyaml":
            case "spring-boot-autoconfigure":
            case "HikariCP":
            case "mysql-connector-java":
            case "classmate":
            case "mybatis-plus-boot-starter":
            case "spring-cloud-starter-alibaba-nacos-config":
            case "spring-cloud-starter-alibaba-nacos-discovery":
            case "feign-httpclient":
            case "commons-lang3":
            case "spring-web":
            case "spring-webmvc":
            case "hibernate-validator":
            case "xstream":
            case "guava":
            case "jjwt":
            case "com.jcraft":
            case "bcpkix-jdk15on":
            case "bcprov-ext-jdk15on":
            case "httpmime":
            case "hutool-all":
            case "commons-io":
            case "jpinyin":
            case "poi":
            case "fastjson":
            case "spring-tx":
                return true;
            default:
                return false;
        }
    }

}