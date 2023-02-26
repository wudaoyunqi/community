package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @Projectname: community
 * @Filename: SesitiveFilter
 * @Author: yunqi
 * @Date: 2023/2/26 9:15
 * @Description: TODO
 */

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    // 替换符
    private static final String REPLACEMENT = "***";
    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 前缀树（内部类）
    private class TrieNode {
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符，value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    // 初始化前缀树（在构造器之后调用）
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        }

    }

    // 将一个敏感词添加到前缀树
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); ++i) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            // 字符c的子节点不存在
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指向子节点，进入下一轮循环
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.isKeywordEnd = true;
            }
        }
    }

    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder result = new StringBuilder();
        while (begin < text.length()) {
            Character c = text.charAt(begin);
            // 如果当前字符为特殊字符或者当前字符不在敏感词中
            if (isSymbol(c) || tempNode.getSubNode(c) == null) {
                result.append(c);
                position = (++begin) + 1;
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            while (position < text.length() && tempNode != null && !tempNode.isKeywordEnd) {
                c = text.charAt(position);
                if (isSymbol(c)) {
                    position++;
                    continue;
                }
                tempNode = tempNode.getSubNode(c);
                position++;
            }
            // 发现敏感词
            if (tempNode != null && tempNode.isKeywordEnd) {
                result.append(REPLACEMENT);
                begin = position++;
            } else {
                // position遍历越界或者匹配失败
                result.append(text.charAt(begin));
                position = (++begin) + 1;
            }
            tempNode = rootNode;
        }
        return result.toString();
    }

}
