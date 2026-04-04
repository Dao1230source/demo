package org.source.spring.doc.infrastructure.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JavaDoc标签解析器，用于从JavaDoc注释内容中提取各种标签信息。
 * 
 * <p>该类提供了解析JavaDoc标准标签的功能，包括@param、@return、@throws、
 * @see、@author、@version、@since等标签。使用正则表达式进行匹配提取，
 * 支持批量解析和单个标签解析两种方式。</p>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>解析方法注释中的参数说明和返回值说明</li>
 *   <li>提取类或方法的作者、版本等元信息</li>
 *   <li>生成API文档时获取结构化的注释信息</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * DocTagParser parser = new DocTagParser();
 * Map<String, String> params = parser.parseParamTags(docContent);
 * String returnDesc = parser.parseReturnTag(docContent);
 * }</pre>
 * 
 * @author source
 * @since 1.0.0
 */
public class DocTagParser {

    /**
     * @param标签的正则表达式模式。
     * 匹配格式：@param [类型] 参数名 描述
     */
    private static final Pattern PARAM_PATTERN = Pattern.compile("@param\\s+(?:\\w+\\s+)?(\\w+)\\s+(.*)");

    /**
     * @return标签的正则表达式模式。
     * 匹配格式：@return 返回值描述
     */
    private static final Pattern RETURN_PATTERN = Pattern.compile("@return\\s+(.*)");

    /**
     * @throws标签的正则表达式模式。
     * 匹配格式：@throws 异常类型 异常描述
     */
    private static final Pattern THROWS_PATTERN = Pattern.compile("@throws\\s+(\\w+)\\s+(.*)");

    /**
     * @see标签的正则表达式模式。
     * 匹配格式：@see 引用内容
     */
    private static final Pattern SEE_PATTERN = Pattern.compile("@see\\s+(.*)");

    /**
     * @author标签的正则表达式模式。
     * 匹配格式：@author 作者名称
     */
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("@author\\s+(.*)");

    /**
     * @version标签的正则表达式模式。
     * 匹配格式：@version 版本号
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("@version\\s+(.*)");

    /**
     * @since标签的正则表达式模式。
     * 匹配格式：@since 版本号
     */
    private static final Pattern SINCE_PATTERN = Pattern.compile("@since\\s+(.*)");

    /**
     * 解析JavaDoc内容中的所有@param标签。
     * 
     * @param docContent JavaDoc注释内容，可以为null
     * @return 参数名到参数描述的映射，如果没有找到参数标签则返回空Map
     */
    public Map<String, String> parseParamTags(String docContent) {
        Map<String, String> params = new HashMap<>();
        if (docContent == null) {
            return params;
        }
        
        Matcher matcher = PARAM_PATTERN.matcher(docContent);
        while (matcher.find()) {
            params.put(matcher.group(1), matcher.group(2).trim());
        }
        return params;
    }

    /**
     * 解析JavaDoc内容中的@return标签。
     * 
     * @param docContent JavaDoc注释内容，可以为null
     * @return 返回值描述，如果没有找到@return标签则返回null
     */
    public String parseReturnTag(String docContent) {
        if (docContent == null) {
            return null;
        }
        
        Matcher matcher = RETURN_PATTERN.matcher(docContent);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 解析JavaDoc内容中的所有@throws标签。
     * 
     * @param docContent JavaDoc注释内容，可以为null
     * @return 异常类型到异常描述的映射，如果没有找到@throws标签则返回空Map
     */
    public Map<String, String> parseThrowsTags(String docContent) {
        Map<String, String> throwsMap = new HashMap<>();
        if (docContent == null) {
            return throwsMap;
        }
        
        Matcher matcher = THROWS_PATTERN.matcher(docContent);
        while (matcher.find()) {
            throwsMap.put(matcher.group(1), matcher.group(2).trim());
        }
        return throwsMap;
    }

    /**
     * 解析JavaDoc内容中的所有@see标签。
     * 
     * @param docContent JavaDoc注释内容，可以为null
     * @return 引用内容列表，如果没有找到@see标签则返回空列表
     */
    public List<String> parseSeeTags(String docContent) {
        List<String> seeList = new ArrayList<>();
        if (docContent == null) {
            return seeList;
        }
        
        Matcher matcher = SEE_PATTERN.matcher(docContent);
        while (matcher.find()) {
            seeList.add(matcher.group(1).trim());
        }
        return seeList;
    }

    /**
     * 解析JavaDoc内容中的@author标签。
     * 
     * @param docContent JavaDoc注释内容，可以为null
     * @return 作者名称，如果没有找到@author标签则返回null
     */
    public String parseAuthorTag(String docContent) {
        if (docContent == null) {
            return null;
        }
        
        Matcher matcher = AUTHOR_PATTERN.matcher(docContent);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 解析JavaDoc内容中的@version标签。
     * 
     * @param docContent JavaDoc注释内容，可以为null
     * @return 版本号，如果没有找到@version标签则返回null
     */
    public String parseVersionTag(String docContent) {
        if (docContent == null) {
            return null;
        }
        
        Matcher matcher = VERSION_PATTERN.matcher(docContent);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 解析JavaDoc内容中的@since标签。
     * 
     * @param docContent JavaDoc注释内容，可以为null
     * @return 版本号，如果没有找到@since标签则返回null
     */
    public String parseSinceTag(String docContent) {
        if (docContent == null) {
            return null;
        }
        
        Matcher matcher = SINCE_PATTERN.matcher(docContent);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 解析JavaDoc内容中的所有标签信息。
     * 
     * <p>一次性提取所有支持的标签信息，返回一个包含所有解析结果的Map。
     * Map中包含以下键：</p>
     * <ul>
     *   <li>params - 参数标签映射（Map&lt;String, String&gt;）</li>
     *   <li>return - 返回值描述（String）</li>
     *   <li>throws - 异常标签映射（Map&lt;String, String&gt;）</li>
     *   <li>see - 引用列表（List&lt;String&gt;）</li>
     *   <li>author - 作者名称（String）</li>
     *   <li>version - 版本号（String）</li>
     *   <li>since - 起始版本（String）</li>
     * </ul>
     * 
     * @param docContent JavaDoc注释内容，可以为null
     * @return 包含所有标签信息的Map，如果没有找到任何标签则返回空Map
     */
    public Map<String, Object> parseAllTags(String docContent) {
        Map<String, Object> tags = new HashMap<>();
        if (docContent == null) {
            return tags;
        }
        
        tags.put("params", parseParamTags(docContent));
        tags.put("return", parseReturnTag(docContent));
        tags.put("throws", parseThrowsTags(docContent));
        tags.put("see", parseSeeTags(docContent));
        tags.put("author", parseAuthorTag(docContent));
        tags.put("version", parseVersionTag(docContent));
        tags.put("since", parseSinceTag(docContent));
        
        return tags;
    }
}