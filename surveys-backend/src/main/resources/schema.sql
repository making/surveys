CREATE TABLE IF NOT EXISTS survey
(
    survey_id       CHAR(26) PRIMARY KEY,
    start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date_time   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS question
(
    question_id   CHAR(26) PRIMARY KEY,
    question_text VARCHAR(1024) NOT NULL
);

CREATE TABLE IF NOT EXISTS selective_question
(
    question_id   CHAR(26) PRIMARY KEY,
    question_text VARCHAR(1024) NOT NULL,
    max_choices   INTEGER       NOT NULL
);

CREATE TABLE IF NOT EXISTS question_choice
(
    question_choice_id   CHAR(26) PRIMARY KEY,
    question_id          CHAR(26)      NOT NULL,
    question_choice_text VARCHAR(1024) NOT NULL,
    allow_free_text      BOOL          NOT NULL DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES selective_question (question_id)
);

CREATE TABLE IF NOT EXISTS survey_question
(
    survey_id   CHAR(26) NOT NULL,
    question_id CHAR(26) NOT NULL,
    required    BOOL     NOT NULL,
    PRIMARY KEY (survey_id, question_id),
    FOREIGN KEY (survey_id) REFERENCES survey (survey_id)
);

CREATE TABLE IF NOT EXISTS answer
(
    answer_id     CHAR(26) PRIMARY KEY,
    survey_id     CHAR(26)     NOT NULL,
    question_id   CHAR(26)     NOT NULL,
    respondent_id VARCHAR(128) NOT NULL,
    FOREIGN KEY (survey_id) REFERENCES survey (survey_id)
);

CREATE TABLE IF NOT EXISTS descriptive_answer
(
    answer_id   CHAR(26) PRIMARY KEY,
    answer_text VARCHAR(1024) NOT NULL,
    FOREIGN KEY (answer_id) REFERENCES answer (answer_id)
);

CREATE TABLE IF NOT EXISTS chosen_answer
(
    answer_id          CHAR(26) NOT NULL,
    question_choice_id CHAR(26) NOT NULL,
    answer_text        VARCHAR(1024),
    PRIMARY KEY (answer_id, question_choice_id),
    FOREIGN KEY (answer_id) REFERENCES answer (answer_id),
    FOREIGN KEY (question_choice_id) REFERENCES question_choice (question_choice_id)
);