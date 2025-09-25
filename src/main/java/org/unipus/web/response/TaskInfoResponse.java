package org.unipus.web.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * @response {
 *     "success": true,
 *     "code": 0,
 *     "data": {
 *         "course": "course-v2:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
 *         "module": "xxxxxxxxxxxx-xxxxxxxxx",
 *         "base": "xxxxxxxxxxxxxxxxxxxxxx",
 *         "commit": "xxxxxxxxxxxxxxxxxxxxxx",
 *         "state": {
 *             "__EXTEND_DATA__": {
 *                 "__SUBMIT_INFO__": {
 *                     "course_id": "course-v2:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
 *                     "group_id": "xxxxxxxxxxxxxxxxxxxxxx",
 *                     "record_grade": {
 *                         "scorePct": 0.01,
 *                         "ts": xxxxxxxxxxxxx
 *                     },
 *                     "state": {
 *                         "expired": false,
 *                         "lastSubmit": xxxxxxxxx,
 *                         "not_start": false,
 *                         "real_score_pct": 1,
 *                         "score": [
 *                             0,
 *                             0
 *                         ],
 *                         "score_avg": 0,
 *                         "score_pct": 0,
 *                         "state": 1
 *                     },
 *                     "strategy": {
 *                         "endTime": xxxxxxxxxxxx,
 *                         "record_every_submit": false,
 *                         "record_max_submit": false,
 *                         "required": true,
 *                         "startTime": xxxxxxxx,
 *                         "task_mini_score_pct": 0
 *                     },
 *                     "strategyId": xxxxxx,
 *                     "user_id": xxxxxxxx,
 *                     "version": "xxxxxxxx"
 *                 },
 *                 "__SUMMARY__": {
 *                     "answerList": {
 *                         "0": {
 *                             "done": true,
 *                             "questionType": 2,
 *                             "question_type": "basic",
 *                             "right": false,
 *                             "rule": "subjective",
 *                             "signature": "",
 *                             "student_answer": {
 *                                 "answers": "1",
 *                                 "payloads": [],
 *                                 "question_type": "basic",
 *                                 "reply_type": "text-area",
 *                                 "value": "1",
 *                                 "versions": {
 *                                     "answer": 3,
 *                                     "content": 0,
 *                                     "course": xxxxxx,
 *                                     "group": 1,
 *                                     "template": 1
 *                                 }
 *                             },
 *                             "versions": {
 *                                 "answer": 3,
 *                                 "content": 0,
 *                                 "course": xxxxxxx,
 *                                 "group": 1,
 *                                 "template": 1
 *                             }
 *                         }
 *                     }
 *                 }
 *             },
 *             "correlationData": null,
 *             "quesData": "[{\"instanceId\":\"xxxxxxxxxxxxxxxx\",\"answer\":\"{\\\"value\\\":[],\\\"children\\\":[{\\\"value\\\":[\\\"1\\\"],\\\"isDone\\\":true},{\\\"value\\\":[\\\"2\\\"],\\\"isDone\\\":true}],\\\"progress\\\":{},\\\"record\\\":{\\\"url\\\":\\\"\\\"}}\",\"context\":\"{\\\"state\\\":\\\"submitted\\\"}\",\"contextVersion\":1,\"answerVersion\":4}]",
 *             "version": "1758707921"
 *         }
 *     },
 *     "msg": ""
 * }
 */
public class TaskInfoResponse extends Response {
    private int code;
    private boolean success;
    private String msg;
    private Data data;

    public int getCode() {
        return code;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        private String course;
        private String module;
        private String base;
        private String commit;
        private State state;

        public String getCourse() {
            return course;
        }

        public String getModule() {
            return module;
        }

        public String getBase() {
            return base;
        }

        public String getCommit() {
            return commit;
        }

        public State getState() {
            return state;
        }
    }

    public static class State {
        @SerializedName("__EXTEND_DATA__")
        private ExtendData extendData;
        private Object correlationData;
        private String quesData;
        private String version;

        public ExtendData getExtendData() {
            return extendData;
        }

        public Object getCorrelationData() {
            return correlationData;
        }

        public String getQuesData() {
            return quesData;
        }

        public String getVersion() {
            return version;
        }
    }

    public static class ExtendData {
        @SerializedName("__SUBMIT_INFO__")
        private SubmitInfo submitInfo;
        @SerializedName("__SUMMARY__")
        private Summary summary;

        public SubmitInfo getSubmitInfo() {
            return submitInfo;
        }

        public Summary getSummary() {
            return summary;
        }
    }

    public static class SubmitInfo {
        private String course_id;
        private String group_id;
        private RecordGrade record_grade;
        private SubmitState state;
        private Strategy strategy;
        private int strategyId;
        private int user_id;
        private String version;

        public String getCourse_id() {
            return course_id;
        }

        public String getGroup_id() {
            return group_id;
        }

        public RecordGrade getRecord_grade() {
            return record_grade;
        }

        public SubmitState getState() {
            return state;
        }

        public Strategy getStrategy() {
            return strategy;
        }

        public int getStrategyId() {
            return strategyId;
        }

        public int getUser_id() {
            return user_id;
        }

        public String getVersion() {
            return version;
        }
    }

    public static class RecordGrade {
        private double scorePct;
        private long ts;

        public double getScorePct() {
            return scorePct;
        }

        public long getTs() {
            return ts;
        }
    }

    public static class SubmitState {
        private boolean expired;
        private long lastSubmit;
        private boolean not_start;
        private int real_score_pct;
        private List<Integer> score;
        private int score_avg;
        private int score_pct;
        private int state;

        public boolean isExpired() {
            return expired;
        }

        public long getLastSubmit() {
            return lastSubmit;
        }

        public boolean isNot_start() {
            return not_start;
        }

        public int getReal_score_pct() {
            return real_score_pct;
        }

        public List<Integer> getScore() {
            return score;
        }

        public int getScore_avg() {
            return score_avg;
        }

        public int getScore_pct() {
            return score_pct;
        }

        public int getState() {
            return state;
        }
    }

    public static class Strategy {
        private long endTime;
        private boolean record_every_submit;
        private boolean record_max_submit;
        private boolean required;
        private long startTime;
        private int task_mini_score_pct;

        public long getEndTime() {
            return endTime;
        }

        public boolean isRecord_every_submit() {
            return record_every_submit;
        }

        public boolean isRecord_max_submit() {
            return record_max_submit;
        }

        public boolean isRequired() {
            return required;
        }

        public long getStartTime() {
            return startTime;
        }

        public int getTask_mini_score_pct() {
            return task_mini_score_pct;
        }
    }

    public static class Summary {
        private Map<String, AnswerInfo> answerList;

        public Map<String, AnswerInfo> getAnswerList() {
            return answerList;
        }
    }

    public static class AnswerInfo {
        private boolean done;
        private int questionType;
        private String question_type;
        private boolean right;
        private String rule;
        private String signature;
        private StudentAnswer student_answer;
        private Versions versions;


        public boolean isDone() {
            return done;
        }

        public int getQuestionType() {
            return questionType;
        }

        public String getQuestion_type() {
            return question_type;
        }

        public boolean isRight() {
            return right;
        }

        public String getRule() {
            return rule;
        }

        public String getSignature() {
            return signature;
        }

        public StudentAnswer getStudent_answer() {
            return student_answer;
        }

        public Versions getVersions() {
            return versions;
        }
    }

    public static class StudentAnswer {

    }

    public static class Versions {
        private int answer;
        private int content;
        private int course;
        private int group;
        private int template;

        public int getAnswer() {
            return answer;
        }

        public int getContent() {
            return content;
        }

        public int getCourse() {
            return course;
        }

        public int getGroup() {
            return group;
        }

        public int getTemplate() {
            return template;
        }
    }
}
