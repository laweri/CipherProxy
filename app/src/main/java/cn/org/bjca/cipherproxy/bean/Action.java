package cn.org.bjca.cipherproxy.bean;

import java.util.List;

/**
 * Created by 吴腾飞 on 2019/4/25.
 */

public class Action {

    private ActionBean action;

    public ActionBean getAction() {
        return action;
    }

    public void setAction(ActionBean action) {
        this.action = action;
    }

    public static class ActionBean {
        private String next;
        private String funcname;
        private String count;
        private String id;
        private String type;
        private String funcid;
        private String flow;
        private List<ParamBean> param;

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public String getFuncname() {
            return funcname;
        }

        public void setFuncname(String funcname) {
            this.funcname = funcname;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
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

        public String getFuncid() {
            return funcid;
        }

        public void setFuncid(String funcid) {
            this.funcid = funcid;
        }

        public String getFlow() {
            return flow;
        }

        public void setFlow(String flow) {
            this.flow = flow;
        }

        public List<ParamBean> getParam() {
            return param;
        }

        public void setParam(List<ParamBean> param) {
            this.param = param;
        }

        public static class ParamBean {
            private String name;
            private String type;
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

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }
    }
}
