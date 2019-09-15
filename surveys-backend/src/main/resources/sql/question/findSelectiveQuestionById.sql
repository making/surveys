SELECT question_id, question_text, max_choices
FROM selective_question
WHERE question_id = :question_id
ORDER BY question_id