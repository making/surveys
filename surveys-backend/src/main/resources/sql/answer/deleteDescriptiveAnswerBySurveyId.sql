DELETE
FROM descriptive_answer
WHERE answer_id IN (SELECT answer_id FROM answer WHERE survey_id = :survey_id)