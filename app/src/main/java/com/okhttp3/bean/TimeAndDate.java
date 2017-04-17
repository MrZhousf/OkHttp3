package com.okhttp3.bean;

/**
 * @author : zhousf
 * @description :
 * @date : 2017/4/17.
 */

public class TimeAndDate {


    /**
     * success : 1
     * result : {"timestamp":"1492413797","datetime_1":"2017-04-17 15:23:17","datetime_2":"2017年04月17日 15时23分17秒","week_1":"1","week_2":"星期一","week_3":"周一","week_4":"Monday"}
     */

    private String success;
    private ResultBean result;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * timestamp : 1492413797
         * datetime_1 : 2017-04-17 15:23:17
         * datetime_2 : 2017年04月17日 15时23分17秒
         * week_1 : 1
         * week_2 : 星期一
         * week_3 : 周一
         * week_4 : Monday
         */

        private String timestamp;
        private String datetime_1;
        private String datetime_2;
        private String week_1;
        private String week_2;
        private String week_3;
        private String week_4;

        @Override
        public String toString() {
            return "timestamp="+timestamp+",datetime_1="+datetime_1+",datetime_2="+datetime_2
                    +"week_1="+week_1+",week_2="+week_2+",week_3="+week_3+",week_4="+week_4;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getDatetime_1() {
            return datetime_1;
        }

        public void setDatetime_1(String datetime_1) {
            this.datetime_1 = datetime_1;
        }

        public String getDatetime_2() {
            return datetime_2;
        }

        public void setDatetime_2(String datetime_2) {
            this.datetime_2 = datetime_2;
        }

        public String getWeek_1() {
            return week_1;
        }

        public void setWeek_1(String week_1) {
            this.week_1 = week_1;
        }

        public String getWeek_2() {
            return week_2;
        }

        public void setWeek_2(String week_2) {
            this.week_2 = week_2;
        }

        public String getWeek_3() {
            return week_3;
        }

        public void setWeek_3(String week_3) {
            this.week_3 = week_3;
        }

        public String getWeek_4() {
            return week_4;
        }

        public void setWeek_4(String week_4) {
            this.week_4 = week_4;
        }
    }
}
