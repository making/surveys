SELECT da.answer_id, da.answer_text
FROM descriptive_answer da
WHERE da.answer_id = :answer_id
ORDER BY da.answer_id