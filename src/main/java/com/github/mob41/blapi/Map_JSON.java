
/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2016, 2017 Anthony Law
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package com.github.mob41.blapi;


import java.util.*;

public class Map_JSON
{
    // -------------------------- Map<String, Object> --> JSON String --------------------------
    /**
     * 原生实现：将Map<String, Object>转为紧凑JSON字符串（无空格、无缩进）
     * 适配常见类型：String、Integer、Boolean、Long（可根据实际需求扩展）
     * @param map 待序列化的Map
     * @return 紧凑JSON字符串
     */
    public static String map_2_json(Map<String, Object> map)
    {
        if (map == null || map.isEmpty())
        {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        boolean first = true;

        for (Map.Entry<String, Object> entry : entrySet)
        {
            if (!first)
            {
                sb.append(","); // 无空格，对应Python的separators=(",", ":")
            }
            first = false;

            // 拼接键（字符串类型，需加双引号）
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");

            // 拼接值（处理常见类型）
            Object value = entry.getValue();
            if (value == null)
            {
                sb.append("null");
            }
            else if (value instanceof String)
            {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            }
            else if (value instanceof Number)
            {
                sb.append(value.toString()); // 数字类型直接拼接（int/long/float等）
            }
            else if (value instanceof Boolean)
            {
                sb.append(value.toString()); // true/false，无需加引号
            }
            else
            {
                // 若有其他类型（如List），可在此扩展，此处默认转字符串
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 辅助方法：JSON字符串转义（处理双引号、反斜杠等）
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private static String escapeJson(String str)
    {
        if (str == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray())
        {
            switch (c)
            {
                case '"': sb.append("\\\"");break;
                case '\\': sb.append("\\\\");break;
                case '/':  sb.append("\\/");break;
                case '\b':  sb.append("\\b");break;
                case '\f':  sb.append("\\f");break;
                case '\n':  sb.append("\\n");break;
                case '\r':  sb.append("\\r");break;
                case '\t':  sb.append("\\t");break;
                default:  sb.append(c);break;
            }
        }
        return sb.toString();
    }





    // -------------------------- JSON String --> Map<String, Object> --------------------------
    /**
     * 原生实现：解析紧凑JSON字符串为Map<String, Object>
     * 适配场景：仅解析键为字符串、值为String/Integer/Boolean的扁平JSON（和之前序列化的格式匹配）
     * 若需支持嵌套/List，可扩展此方法
     * @param jsonStr 紧凑JSON字符串
     * @return 解析后的Map
     * @throws Exception JSON格式错误异常
     */
    public static Map<String, Object> json_2_map(String jsonStr) throws Exception
    {
        Map<String, Object> result = new HashMap<>();
        // 去除首尾的{}，并去除可能的空白（兼容少量意外空格）
        String content = jsonStr.trim();
        if (!content.startsWith("{") || !content.endsWith("}"))
        {
            throw new IllegalArgumentException("无效的JSON对象格式：必须以{}包裹");
        }
        content = content.substring(1, content.length() - 1).trim();
        if (content.isEmpty())
        {
            return result; // 空JSON对象
        }

        // 拆分键值对（处理JSON字符串内的逗号，避免误拆分）
        List<String> keyValuePairs = splitJsonKeyValuePairs(content);
        for (String pair : keyValuePairs)
        {
            // 拆分键和值（处理值内的冒号，避免误拆分）
            int colonIndex = findFirstUnescapedColon(pair);
            if (colonIndex == -1)
            {
                throw new IllegalArgumentException("无效的键值对：缺少冒号 " + pair);
            }
            // 解析键
            String key = pair.substring(0, colonIndex).trim();
            key = unescapeJson(key); // 去除双引号并转义
            // 解析值
            String valueStr = pair.substring(colonIndex + 1).trim();
            Object value = parseJsonValue(valueStr);
            // 存入Map
            result.put(key, value);
        }
        return result;
    }

    /**
     * 辅助方法：拆分JSON键值对（避免拆分字符串内的逗号）
     */
    private static List<String> splitJsonKeyValuePairs(String content)
    {
        List<String> pairs = new ArrayList<>();
        int start = 0;
        int quoteCount = 0;
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            // 统计双引号数量（奇数=在字符串内，偶数=字符串外）
            if (c == '"' && (i == 0 || chars[i-1] != '\\'))
            {
                quoteCount++;
            }
            // 仅当在字符串外时，才拆分逗号
            if (c == ',' && quoteCount % 2 == 0)
            {
                pairs.add(content.substring(start, i).trim());
                start = i + 1;
            }
        }
        // 添加最后一个键值对
        pairs.add(content.substring(start).trim());
        return pairs;
    }

    /**
     * 辅助方法：找到第一个不在字符串内的冒号（键值分隔符）
     */
    private static int findFirstUnescapedColon(String pair)
    {
        int quoteCount = 0;
        char[] chars = pair.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if (c == '"' && (i == 0 || chars[i-1] != '\\'))
            {
                quoteCount++;
            }
            if (c == ':' && quoteCount % 2 == 0)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * 辅助方法：解析JSON值（支持String/Integer/Boolean/null）
     */
    private static Object parseJsonValue(String valueStr) throws Exception
    {
        if (valueStr.startsWith("\"") && valueStr.endsWith("\""))
        {
            // 字符串类型：去除双引号并转义
            return unescapeJson(valueStr.substring(1, valueStr.length() - 1));
        }
        else if ("true".equals(valueStr) /*|| "1".equals(valueStr)*/)
        {
            // 布尔值true
            return true;
        }
        else if ("false".equals(valueStr) /*|| "0".equals(valueStr)*/)
        {
            // 布尔值false
            return false;
        }
        else if ("null".equals(valueStr))
        {
            // null值
            return null;
        }
        else
        {
            // 数字类型（适配整数，若需浮点数可扩展）
            try
            {
                return Integer.parseInt(valueStr);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("不支持的JSON值类型：" + valueStr);
            }
        }
    }

    /**
     * 辅助方法：JSON字符串反转义（还原特殊字符）
     */
    private static String unescapeJson(String str)
    {
        if (str == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < str.length())
        {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length())
            {
                // 处理转义字符
                char next = str.charAt(i + 1);
                switch (next)
                {
                    case '"': sb.append("\""); break;
                    case '\\': sb.append("\\"); break;
                    case '/': sb.append("/"); break;
                    case 'b': sb.append("\b"); break;
                    case 'f': sb.append("\f"); break;
                    case 'n': sb.append("\n"); break;
                    case 'r': sb.append("\r"); break;
                    case 't': sb.append("\t"); break;
                    default: sb.append(next); break;
                }
                i += 2;
            }
            else
            {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }
}
