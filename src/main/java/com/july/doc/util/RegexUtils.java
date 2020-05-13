package com.july.doc.util;

import com.july.doc.utils.CollectionUtil;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 正则匹配工具
 * @author zengxueqi
 * @program july-doc-plugin
 * @since 2020-05-13 08:40
 **/
public class RegexUtils {

    public static boolean isMatches(Set<String> patterns, String str) {
        if (CollectionUtil.isEmpty(patterns)) {
            return false;
        }
        return patterns.stream().anyMatch(s -> {
            return Pattern.compile(s).matcher(str).matches();
        });
    }

}
