SELECT answer_id, survey_id, question_id, respondent_id
FROM answer
WHERE answer_id = :answer_id
ORDER BY answer_id