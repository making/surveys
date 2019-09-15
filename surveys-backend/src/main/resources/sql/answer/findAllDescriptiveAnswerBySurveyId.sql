SELECT da.answer_id, da.answer_text
FROM descriptive_answer da
         INNER JOIN answer a ON da.answer_id = a.answer_id
WHERE a.survey_id = :survey_id
ORDER BY da.answer_id