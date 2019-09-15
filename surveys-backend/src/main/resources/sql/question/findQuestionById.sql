SELECT question_id, question_text
FROM question
WHERE question_id = :question_id
ORDER BY question_id