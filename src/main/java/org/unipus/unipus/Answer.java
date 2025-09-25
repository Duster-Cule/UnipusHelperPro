package org.unipus.unipus;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.List;

public class Answer {
    private static final transient Gson gson = new Gson();

    private long id;
    private String content;
    private String answer;
    private String analysis;
    private String quesId;
    private Analysis questionAnalysis;
    private AnswerContent questionAnswers;

    public Answer parse() {
        questionAnswers = gson.fromJson(answer, AnswerContent.class);
        questionAnalysis = gson.fromJson(analysis, Analysis.class);
        return this;
    }

    /**
     * 通过JSON字符串创建Answer实例
     * @param answerJSON JSON字符串
     * @param index      题目索引，从0开始 - 有可能一个任务包含多个题目，需要指定索引
     * @return
     */
    public static Answer getInstanceByJSON(String answerJSON, int index) {
        JsonElement ans = JsonParser.parseString(answerJSON).getAsJsonArray().get(index);
        Answer instance = gson.fromJson(ans, Answer.class);
        return instance.parse();
    }

    private Answer() {}

    public long getId() {
        return id;
    }

    public AnswerContent getQuestionAnswers() {
        return questionAnswers;
    }

    public Analysis getQuestionAnalysis() {
        return questionAnalysis;
    }

    public static class Analysis{
        private List<ChildAnalysis> children;
        private String analysis;

        public List<ChildAnalysis> getChildren() {
            return children;
        }

        public String getAnalysis() {
            return analysis;
        }
    }

    public static class ChildAnalysis{
        private String analysis;

        public String getAnalysis() {
            return analysis;
        }
    }

    public static class AnswerContent{
        private List<Answers> children;

        public List<Answers> getChildren() {
            return children;
        }
    }

    public static class Answers{
        private List<String> answers;

        public List<String> getAnswer() {
            return answers;
        }
    }
}
