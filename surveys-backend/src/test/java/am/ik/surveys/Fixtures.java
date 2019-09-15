package am.ik.surveys;

import am.ik.surveys.answer.Answer;
import am.ik.surveys.answer.AnswerDetail;
import am.ik.surveys.answer.ChosenAnswer;
import am.ik.surveys.answer.DescriptiveAnswer;
import am.ik.surveys.answer.Respondent;
import am.ik.surveys.question.Question;
import am.ik.surveys.question.SelectiveQuestion;
import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.survey.Survey;
import am.ik.surveys.surveyquestion.SurveyQuestion;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class Fixtures {

    public static final List<Survey> surveys = List.of(
        new Survey.Builder()
            .withSurveyId(Survey.Id.valueOf("01DJ9KW69W1059TF3WE5GJTEDM"))
            .withSurveyTitle("テストアンケート")
            .withStartDateTime(OffsetDateTime.parse("2019-08-01T00:00:00+09:00"))
            .withEndDateTime(OffsetDateTime.parse("2019-08-31T00:00:00+09:00"))
            .build()
    );

    public static final Map<Survey.Id, Survey> surveyMap = surveys.stream()
        .collect(toMap(Survey::getSurveyId, identity()));

    static final Survey s1 = surveys.get(0);

    public static final List<Question> questions = List.of(
        new SelectiveQuestion.Builder()
            .withQuestionId(Question.Id.valueOf("01DJHN4RZ39MAY7PAWEYZX5RPC"))
            .withQuestionText("この設計はいけてますか?")
            .withMaxChoices(1)
            .build(),
        new Question.Builder()
            .withQuestionId(Question.Id.valueOf("01DJHN4RZ30657T87H7804YT9N"))
            .withQuestionText("どういうところがいけてますか?")
            .build(),
        new SelectiveQuestion.Builder()
            .withQuestionId(Question.Id.valueOf("01DJHN4RZ3DNQK3J4YSRTEVYC3"))
            .withQuestionText("他にも取り上げて欲しい設計がありますか?")
            .withMaxChoices(3)
            .build()
    );

    public static final Map<Question.Id, Question> questionMap = questions.stream()
        .collect(toMap(Question::getQuestionId, identity()));

    static final Question q1 = questions.get(0);

    static final Question q2 = questions.get(1);

    static final Question q3 = questions.get(2);

    public static final List<QuestionChoice> questionChoices = List.of(
        new QuestionChoice.Builder()
            .withQuestionChoiceId(QuestionChoice.Id.valueOf("01DJHN4RZ32BXRK7DG8QS1RH5N"))
            .withQuestionId(q3.getQuestionId())
            .withQuestionChoiceText("在庫")
            .withAllowFreeText(false)
            .build(),
        new QuestionChoice.Builder()
            .withQuestionChoiceId(QuestionChoice.Id.valueOf("01DJHN4RZ4TFX5EVSE4B9C1KGF"))
            .withQuestionId(q3.getQuestionId())
            .withQuestionChoiceText("カート")
            .withAllowFreeText(false)
            .build(),
        new QuestionChoice.Builder()
            .withQuestionChoiceId(QuestionChoice.Id.valueOf("01DJHN4RZ4PDY7S98Q82AWBSES"))
            .withQuestionId(q3.getQuestionId())
            .withQuestionChoiceText("お気に入り")
            .withAllowFreeText(false)
            .build(),
        new QuestionChoice.Builder()
            .withQuestionChoiceId(QuestionChoice.Id.valueOf("01DJHN4RZ4ETXY2QTN4J1SMSF1"))
            .withQuestionId(q3.getQuestionId())
            .withQuestionChoiceText("リコメンド")
            .withAllowFreeText(false)
            .build(),
        new QuestionChoice.Builder()
            .withQuestionChoiceId(QuestionChoice.Id.valueOf("01DJHN4RZ43GQRMXQ60HK0BRWS"))
            .withQuestionId(q3.getQuestionId())
            .withQuestionChoiceText("その他")
            .withAllowFreeText(true)
            .build(),
        new QuestionChoice.Builder()
            .withQuestionChoiceId(QuestionChoice.Id.valueOf("01DJHN4RZ4ZDS8BMW44WVPZ22M"))
            .withQuestionId(q1.getQuestionId())
            .withQuestionChoiceText("はい")
            .withAllowFreeText(false)
            .build(),
        new QuestionChoice.Builder()
            .withQuestionChoiceId(QuestionChoice.Id.valueOf("01DJHN4RZ4ABY55E0FND0NF1VJ"))
            .withQuestionId(q1.getQuestionId())
            .withQuestionChoiceText("いいえ")
            .withAllowFreeText(false)
            .build());

    public static final Map<QuestionChoice.Id, QuestionChoice> questionChoiceMap = questionChoices.stream()
        .collect(toMap(QuestionChoice::getQuestionChoiceId, identity()));

    static QuestionChoice c1 = questionChoices.get(0);

    static QuestionChoice c2 = questionChoices.get(1);

    static QuestionChoice c3 = questionChoices.get(2);

    static QuestionChoice c4 = questionChoices.get(3);

    static QuestionChoice c5 = questionChoices.get(4);

    static QuestionChoice c6 = questionChoices.get(5);

    static QuestionChoice c7 = questionChoices.get(6);

    public static final List<SurveyQuestion> surveyQuestions = List.of(
        new SurveyQuestion.Builder()
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q1.getQuestionId())
            .withRequired(true)
            .build(),
        new SurveyQuestion.Builder()
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q2.getQuestionId())
            .withRequired(false)
            .build(),
        new SurveyQuestion.Builder()
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q3.getQuestionId())
            .withRequired(true)
            .build());

    public static final List<Respondent> respondents = List.of(
        new Respondent.Builder()
            .withRespondentId(Respondent.Id.valueOf("aaa"))
            .build(),
        new Respondent.Builder()
            .withRespondentId(Respondent.Id.valueOf("bbb"))
            .build(),
        new Respondent.Builder()
            .withRespondentId(Respondent.Id.valueOf("ccc"))
            .build()
    );

    static Respondent r1 = respondents.get(0);

    static Respondent r2 = respondents.get(1);

    static Respondent r3 = respondents.get(2);

    public static final List<Answer> answers = List.of(
        new Answer.Builder()
            .withAnswerId(Answer.Id.valueOf("01DJHN4RZ55F85W4P5FV24S86C"))
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q1.getQuestionId())
            .withRespondentId(r1.getRespondentId())
            .build(),
        new Answer.Builder()
            .withAnswerId(Answer.Id.valueOf("01DJHN4RZ52A55ARMZ6XDHN5SR"))
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q2.getQuestionId())
            .withRespondentId(r1.getRespondentId())
            .build(),
        new Answer.Builder()
            .withAnswerId(Answer.Id.valueOf("01DJHN4RZ5CHAA0H4X7PT27FDZ"))
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q3.getQuestionId())
            .withRespondentId(r1.getRespondentId())
            .build(),
        new Answer.Builder()
            .withAnswerId(Answer.Id.valueOf("01DJHN4RZ55CGYJYVDT27NKFP8"))
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q1.getQuestionId())
            .withRespondentId(r2.getRespondentId())
            .build(),
        new Answer.Builder()
            .withAnswerId(Answer.Id.valueOf("01DJHN4RZ6CG08PFYCT42QG2R2"))
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q2.getQuestionId())
            .withRespondentId(r2.getRespondentId())
            .build(),
        new Answer.Builder()
            .withAnswerId(Answer.Id.valueOf("01DJHN4RZ6E98BACTNQSKH6AFE"))
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q3.getQuestionId())
            .withRespondentId(r2.getRespondentId())
            .build(),
        new Answer.Builder()
            .withAnswerId(Answer.Id.valueOf("01DJHN4RZ6B6PNZEQ3HHWVNFRA"))
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q1.getQuestionId())
            .withRespondentId(r3.getRespondentId())
            .build(),
        new Answer.Builder()
            .withAnswerId(Answer.Id.valueOf("01DJHN4RZ6ZAN2G4A78RYG498F"))
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q2.getQuestionId())
            .withRespondentId(r3.getRespondentId())
            .build(),
        new Answer.Builder()
            .withAnswerId(Answer.Id.valueOf("01DJHN4RZ64Q2ZPB8W684APZZ0"))
            .withSurveyId(s1.getSurveyId())
            .withQuestionId(q3.getQuestionId())
            .withRespondentId(r3.getRespondentId())
            .build()
    );

    public static final Map<Answer.Id, Answer> answerMap = answers.stream()
        .collect(toMap(Answer::getAnswerId, identity()));

    static Answer a1 = answers.get(0);

    static Answer a2 = answers.get(1);

    static Answer a3 = answers.get(2);

    static Answer a4 = answers.get(3);

    static Answer a5 = answers.get(4);

    static Answer a6 = answers.get(5);

    static Answer a7 = answers.get(6);

    static Answer a8 = answers.get(7);

    static Answer a9 = answers.get(8);

    public static List<AnswerDetail<?>> answerDetails = List.of(
        new DescriptiveAnswer.Builder()
            .withAnswerId(a2.getAnswerId())
            .withAnswerText("具体的なデータがあってわかりやすい")
            .build(),
        new DescriptiveAnswer.Builder()
            .withAnswerId(a5.getAnswerId())
            .withAnswerText("ER図がわかりやすい")
            .build(),
        new DescriptiveAnswer.Builder()
            .withAnswerId(a8.getAnswerId())
            .withAnswerText("ここまで複雑なモデルが必要なの?")
            .build(),
        new ChosenAnswer.Builder()
            .withAnswerId(a1.getAnswerId())
            .withQuestionChoiceId(c6.getQuestionChoiceId())
            .build(),
        new ChosenAnswer.Builder()
            .withAnswerId(a3.getAnswerId())
            .withQuestionChoiceId(c1.getQuestionChoiceId())
            .build(),
        new ChosenAnswer.Builder()
            .withAnswerId(a3.getAnswerId())
            .withQuestionChoiceId(c2.getQuestionChoiceId())
            .build(),
        new ChosenAnswer.Builder()
            .withAnswerId(a4.getAnswerId())
            .withQuestionChoiceId(c7.getQuestionChoiceId())
            .build(),
        new ChosenAnswer.Builder()
            .withAnswerId(a6.getAnswerId())
            .withQuestionChoiceId(c4.getQuestionChoiceId())
            .build(),
        new ChosenAnswer.Builder()
            .withAnswerId(a7.getAnswerId())
            .withQuestionChoiceId(c6.getQuestionChoiceId())
            .build(),
        new ChosenAnswer.Builder()
            .withAnswerId(a9.getAnswerId())
            .withQuestionChoiceId(c5.getQuestionChoiceId())
            .withAnswerText("検索")
            .build()
    );

}
