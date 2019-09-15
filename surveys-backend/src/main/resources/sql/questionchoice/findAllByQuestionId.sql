SELECT question_choice_id, question_id, question_choice_text, allow_free_text
FROM question_choice
WHERE question_id = :question_id
ORDER BY question_choice_id DESC