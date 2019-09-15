SELECT ca.answer_id, ca.question_choice_id, ca.answer_text
FROM chosen_answer ca
         INNER JOIN answer a ON ca.answer_id = a.answer_id
WHERE a.survey_id = :survey_id
ORDER BY ca.answer_id