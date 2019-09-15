SELECT survey_id, question_id, required
FROM survey_question
WHERE survey_id = :survey_id