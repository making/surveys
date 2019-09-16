SELECT ans.answer_id, ans.question_choice_id, ans.answer_text
FROM (
         SELECT ca.answer_id, ca.question_choice_id, ca.answer_text
         FROM chosen_answer ca
         UNION ALL
         SELECT da.answer_id, NULL AS question_choice_id, da.answer_text
         FROM descriptive_answer da
     ) ans
WHERE ans.answer_id = :answer_id
ORDER BY ans.answer_id