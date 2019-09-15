package am.ik.surveys;

import am.ik.surveys.answer.web.AnswerHandler;
import am.ik.surveys.infra.sql.SqlSupplier;
import am.ik.surveys.question.web.QuestionHandler;
import am.ik.surveys.questionchoice.web.QuestionChoiceHandler;
import am.ik.surveys.survey.web.SurveyHandler;
import am.ik.surveys.surveyquestion.web.SurveyQuestionHandler;
import de.huxhorn.sulky.ulid.ULID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.function.server.RouterFunction;

@SpringBootApplication
@EnableR2dbcRepositories
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public RouterFunction<?> routes(WelcomeHandler welcomeHandler,
                                    SurveyHandler surveyHandler,
                                    SurveyQuestionHandler surveyQuestionHandler,
                                    AnswerHandler answerHandler,
                                    QuestionHandler questionHandler,
                                    QuestionChoiceHandler questionChoiceHandler) {
        return welcomeHandler.routes()
            .and(surveyHandler.routes())
            .and(surveyQuestionHandler.routes())
            .and(answerHandler.routes())
            .and(questionHandler.routes())
            .and(questionChoiceHandler.routes());
    }

    @Bean
    public ULID ulid() {
        return new ULID();
    }

    @Bean
    public SqlSupplier sqlSupplier() {
        return new SqlSupplier();
    }
}

