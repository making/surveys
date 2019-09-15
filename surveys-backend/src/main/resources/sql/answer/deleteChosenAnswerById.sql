DELETE
FROM chosen_answer
WHERE answer_id = :answer_id
  AND question_choice_id = :question_choice_id