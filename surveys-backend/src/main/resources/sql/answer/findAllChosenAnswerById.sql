SELECT ca.answer_id, ca.question_choice_id, ca.answer_text
FROM chosen_answer ca
WHERE ca.answer_id = :answer_id
ORDER BY ca.answer_id