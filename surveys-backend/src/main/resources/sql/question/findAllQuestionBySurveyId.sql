SELECT que.question_id, que.question_text, que.max_choices
FROM (
         SELECT q.question_id, q.question_text, sq.survey_id, NULL AS max_choices
         FROM question q
                  INNER JOIN survey_question sq ON q.question_id = sq.question_id
         UNION ALL
         SELECT q.question_id, q.question_text, sq.survey_id, q.max_choices
         FROM selective_question q
                  INNER JOIN survey_question sq ON q.question_id = sq.question_id
     ) que
WHERE que.survey_id = :survey_id
ORDER BY que.question_id