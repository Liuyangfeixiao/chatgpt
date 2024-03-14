package com.example.chatgpt.infrastracture.util.wechat;

import cn.hutool.core.util.StrUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import java.io.Writer;

/**
 * @description 微信公众号XML工具类
 */
public class XmlUtil {
    private static final XStream xstream = initXStream();
    /**
     * @description 反序列化xml回bean
     * @param xml XML字符串
     * @param clazz 目标 Bean 类型的 Class 对象
     * @return Bean实例
     */
    public static <T> T xmlToBean(String xml, Class<T> clazz) {
        // XStream对象设置默认安全防护，同时设置允许的类
//        XStream.setupDefaultSecurity(xstream);
//        xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias("xml", clazz);
        xstream.allowTypes(new Class[]{clazz});
        xstream.processAnnotations(new Class[]{clazz});
        // 设置类加载器
        xstream.setClassLoader(clazz.getClassLoader());
        return (T) xstream.fromXML(xml);

    }
    public static String beanToXml(Object object) {
        xstream.alias("xml", object.getClass());
        xstream.processAnnotations(object.getClass());
        String xml = xstream.toXML(object);
        if (!StrUtil.isEmpty(xml)) {
            return xml;
        } else {
            return null;
        }
    }
    /**
     * @description 初始化并配置XStream实例
     */
    private static XStream initXStream() {
        return new XStream(new XppDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out) {
                    final boolean cdataWrap = true;
                    final String format = "<![CDATA[%s]]>";

                    /**
                     * @description 序列化为XML时将节点的首字母大写
                     * @param name 节点名称
                     * @param clazz 节点对应的类
                     */
                    @Override
                    public void startNode(String name, Class clazz) {
                        if ("xml".equals(name)) {
                            super.startNode(name, clazz);
                        } else {
                            super.startNode(StrUtil.upperFirst(name), clazz);
                        }
                    }

                    /**
                     * @description 使用CDATA包裹文本节点
                     * @param writer 用于实际写入操作
                     * @param text 文本内容
                     */
                    @Override
                    protected void writeText(QuickWriter writer, String text) {
                        if (cdataWrap && !StrUtil.isNumeric(text)) {
                            writer.write(String.format(format, text));
                        } else {
                            writer.write(text);
                        }
                    }
                };
            }
        }) {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public String realMember(Class type, String serialized) {
                        // 将序列化的XML节点转化为对象的字段名
                        return super.realMember(type, StrUtil.lowerFirst(serialized));
                    }
                };
            }
        };
    }

}
