SELECT que.question_id, que.question_text, que.max_choices
FROM (
         SELECT question_id, question_text, NULL AS max_choices
         FROM question
         UNION ALL
         SELECT question_id, question_text, max_choices
         FROM selective_question
     ) que
ORDER BY question_id