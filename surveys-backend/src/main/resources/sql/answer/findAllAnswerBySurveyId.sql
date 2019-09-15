SELECT answer_id, survey_id, question_id, respondent_id
FROM answer
WHERE survey_id = :survey_id
ORDER BY answer_id