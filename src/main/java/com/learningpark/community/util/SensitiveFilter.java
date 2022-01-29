package com.learningpark.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);

    //敏感词替换符
    private static final String REPLACEMENT = "**";

    //根节点
    private TrieNode rootNode = new TrieNode();

    //前缀树
    private class TrieNode {

        //敏感词结束标识
        private boolean isSensitiveWordEnd = false;

        public boolean isSensitiveWordEnd() {
            return isSensitiveWordEnd;
        }

        public void setSensitiveWordEnd(boolean sensitiveWordEnd) {
            isSensitiveWordEnd = sensitiveWordEnd;
        }

        //子节点(key是下级字符,value是下级结点)
        private Map<Character, TrieNode> sonNodes = new HashMap<>();

        //添加子节点
        public void addSonNode(Character c, TrieNode node) {
            sonNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSonNode(Character c) {
            return sonNodes.get(c);
        }

    }

    //初始化前缀树(将敏感词库的敏感词初始化到前缀树中)
    @PostConstruct
    public void init() {

        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String sensitiveWord;
            while ((sensitiveWord = reader.readLine()) != null) {
                //添加到前缀树
                this.addSensitiveWord(sensitiveWord);
            }
        } catch (IOException e) {
            LOGGER.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    //将敏感词添加到前缀树中
    private void addSensitiveWord(String sensitiveWord) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < sensitiveWord.length(); i++) {
            char c = sensitiveWord.charAt(i);
            TrieNode sonNode = tempNode.getSonNode(c);

            if (sonNode == null) {
                //初始化子节点
                sonNode = new TrieNode();
                tempNode.addSonNode(c, sonNode);
            }

            //指向子节点，进入下一轮循环
            tempNode = sonNode;

            //设置结束标识
            if (i == sensitiveWord.length() - 1) {
                tempNode.setSensitiveWordEnd(true);
            }
        }
    }

    //判断语词是否为符号
    private boolean isSymbol(Character c) {
        //0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 过滤敏感词
     *
     * @param text 需要过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {

        if (StringUtils.isBlank(text)) {
            return null;
        }

        //指针1(指向前缀树)
        TrieNode tempNode = rootNode;
        //指针2(指向敏感词头部)
        int begin = 0;
        //指针3(指向敏感词尾部)
        int end = 0;
        //过滤后文本
        StringBuilder sb = new StringBuilder();

        while (begin < text.length()) {

            if (begin == text.length() - 1) {
                sb.append(text.charAt(begin));
                break;
            }

            char c = text.charAt(end);

            //跳过符号
            if (isSymbol(c)) {
                //若指针1位于根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或者中间，指针3都向下走一步
                //检查下一个字符
                if (end < text.length() - 1) {
                    end++;
                } else {
                    sb.append(text.charAt(begin));
                    end = ++begin;
                    tempNode = rootNode;
                }
                continue;
            }

            //检查下级节点
            tempNode = tempNode.getSonNode(c);
            if (tempNode == null) {
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                end = ++begin;
                //指针1重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isSensitiveWordEnd()) {
                //发现敏感词，将begin~end之间的字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin = ++end;
                //指针1重新回到根节点
                tempNode = rootNode;
            } else {
                //检查下一个字符
                if (end < text.length() - 1) {
                    end++;
                } else {
                    sb.append(text.charAt(begin));
                    end = ++begin;
                    tempNode = rootNode;
                }
            }
        }

        return sb.toString();

    }

}
