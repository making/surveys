DELETE
FROM survey_question
WHERE survey_id = :survey_id
  AND question_id = :question_id