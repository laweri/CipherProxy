package cn.org.bjca.cipherproxy.bean;

import java.util.List;

/**
 * Created by 吴腾飞 on 2019/4/22.
 */

public class Encrypt {


    /**
     * next : ac2
     * count :
     * funcid : 方法ID
     * paramlist : {"param":[{"name":"seckey","type":"global","required":true,"content":"paramname1"},{"name":"text","type":"","required":true,"content":"待加密明文"}]}
     * id : ac1
     * type :
     * flow : 1
     * funcname : SM4Encrypt
     */

    private ActionBean action;

    public ActionBean getAction() {
        return action;
    }

    public void setAction(ActionBean action) {
        this.action = action;
    }

    public static class ActionBean {
        private String next;
        private String count;
        private String funcid;
        private ParamlistBean paramlist;
        private String id;
        private String type;
        private String flow;
        private String funcname;

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getFuncid() {
            return funcid;
        }

        public void setFuncid(String funcid) {
            this.funcid = funcid;
        }

        public ParamlistBean getParamlist() {
            return paramlist;
        }

        public void setParamlist(ParamlistBean paramlist) {
            this.paramlist = paramlist;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFlow() {
            return flow;
        }

        public void setFlow(String flow) {
            this.flow = flow;
        }

        public String getFuncname() {
            return funcname;
        }

        public void setFuncname(String funcname) {
            this.funcname = funcname;
        }

        public static class ParamlistBean {
            /**
             * name : seckey
             * type : global
             * required : true
             * content : paramname1
             */

            private List<ParamBean> param;

            public List<ParamBean> getParam() {
                return param;
            }

            public void setParam(List<ParamBean> param) {
                this.param = param;
            }

            public static class ParamBean {
                private String name;
                private String type;
                private String required;
                private String content;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }

                public String isRequired() {
                    return required;
                }

                public void setRequired(String required) {
                    this.required = required;
                }

                public String getContent() {
                    return content;
                }

                public void setContent(String content) {
                    this.content = content;
                }
            }
        }
    }
}
