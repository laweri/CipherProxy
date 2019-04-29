package cn.org.bjca.cipherproxy.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.org.bjca.cipherproxy.bean.ActionBean;


/**
 * Created by 吴腾飞 on 2019/3/25.
 */

public class ParseXML {
    private static ActionBean actionBean;
    private static List<ActionBean.ParamBean> paramBeanList;
    // private static Encrypt.ActionBean actionBean;
    //  private static List<Encrypt.ActionBean.ParamlistBean.ParamBean> paramBeanList;

//    public static Encrypt.ActionBean getActionBean(InputStream inStream) throws Throwable {
//
//
//        //========创建XmlPullParser,有两种方式=======
//        //方式一:使用工厂类XmlPullParserFactory
//        XmlPullParserFactory pullFactory = XmlPullParserFactory.newInstance();
//        XmlPullParser parser = pullFactory.newPullParser();
//        //方式二:使用Android提供的实用工具类android.util.Xml
//        //XmlPullParser parser = Xml.newPullParser();
//
//        //解析文件输入流
//        parser.setInput(inStream, "UTF-8");
//        //产生第一个事件
//        int eventType = parser.getEventType();
//        //只要不是文档结束事件，就一直循环
//        while (eventType != XmlPullParser.END_DOCUMENT) {
//            switch (eventType) {
//                //触发开始文档事件
//                case XmlPullParser.START_DOCUMENT:
//                    actionBean = new Encrypt.ActionBean();
//                    break;
//                //触发开始元素事件
//                case XmlPullParser.START_TAG:
//                    //获取解析器当前指向的元素的名称
//                    String name = parser.getName();
//                    if ("action".equals(name)) {
//                        actionBean.setId(parser.getAttributeValue(null, "id"));
//                        actionBean.setFlow(parser.getAttributeValue(null, "flow"));
//                        actionBean.setType(parser.getAttributeValue(null, "type"));
//                        actionBean.setCount(parser.getAttributeValue(null, "count"));
//                        actionBean.setNext(parser.getAttributeValue(null, "next"));
//                    }
//
//                    if (actionBean != null) {
//                        if ("funcname".equals(name)) {
//                            //获取解析器当前指向元素的下一个文本节点的值
//
//                            actionBean.setFuncname(parser.nextText());
//                        }
//                        if ("funcid".equals(name)) {
//                            actionBean.setFuncid(parser.nextText());
//                        }
//
//                        if ("paramlist".equals(name)) {
//                            paramBeanList = new ArrayList<>();
//
//                        }
//                        if (paramBeanList != null) {
//                            if ("param".equals(name)) {
//                                Encrypt.ActionBean.ParamlistBean.ParamBean paramBean = new Encrypt.ActionBean.ParamlistBean.ParamBean();
//                                paramBean.setName(parser.getAttributeValue(null, "name"));
//                                paramBean.setRequired(parser.getAttributeValue(null, "required"));
//                                paramBean.setType(parser.getAttributeValue(null, "type"));
//                                paramBean.setContent(parser.nextText());
//                                paramBeanList.add(paramBean);
//                            }
//                            Encrypt.ActionBean.ParamlistBean paramlistBean = new Encrypt.ActionBean.ParamlistBean();
//                            paramlistBean.setParam(paramBeanList);
//                            actionBean.setParamlist(paramlistBean);
//                        }
//
//                    }
//                    break;
//                //触发结束元素事件
//                case XmlPullParser.END_TAG:
//                    if ("action".equals(parser.getName())) {
//                    }
//                    break;
//                default:
//                    break;
//            }
//            eventType = parser.next();
//        }
//        return actionBean;
//    }

    public static ActionBean getActionBean(InputStream inStream) throws Throwable {

        //========创建XmlPullParser,有两种方式=======
        //方式一:使用工厂类XmlPullParserFactory
        XmlPullParserFactory pullFactory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = pullFactory.newPullParser();
        //方式二:使用Android提供的实用工具类android.util.Xml
        //XmlPullParser parser = Xml.newPullParser();

        //解析文件输入流
        parser.setInput(inStream, "UTF-8");
        //产生第一个事件
        int eventType = parser.getEventType();
        //只要不是文档结束事件，就一直循环
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                //触发开始文档事件
                case XmlPullParser.START_DOCUMENT:
                    actionBean = new ActionBean();
                    paramBeanList = new ArrayList<>();
                    break;
                //触发开始元素事件
                case XmlPullParser.START_TAG:

                    //获取解析器当前指向的元素的名称
                    String name = parser.getName();
                    if ("action".equals(name)) {
                        actionBean.setId(parser.getAttributeValue(null, "id"));
                        actionBean.setFlow(parser.getAttributeValue(null, "flow"));
                        actionBean.setType(parser.getAttributeValue(null, "type"));
                        actionBean.setCount(parser.getAttributeValue(null, "count"));
                        actionBean.setNext(parser.getAttributeValue(null, "next"));
                        actionBean.setFuncid(parser.getAttributeValue(null, "funcid"));
                        actionBean.setFuncname(parser.getAttributeValue(null, "funcname"));
                    }
                    if ("param".equals(name)) {
                        ActionBean.ParamBean paramBean = new ActionBean.ParamBean();
                        paramBean.setName(parser.getAttributeValue(null, "name"));
                        paramBean.setType(parser.getAttributeValue(null, "type"));
                        paramBean.setContent(parser.nextText());
                        paramBeanList.add(paramBean);
                    }


                    break;
                //触发结束元素事件
                case XmlPullParser.END_TAG:
                    if ("action".equals(parser.getName())) {
                        actionBean.setParam(paramBeanList);
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return actionBean;
    }
}
