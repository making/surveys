SELECT q.question_id, q.question_text, q.max_choices
FROM selective_question q
         INNER JOIN survey_question sq ON q.question_id = sq.question_id
WHERE sq.survey_id = :survey_id
ORDER BY q.question_id