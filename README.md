## Survey API

* https://scrapbox.io/kawasima/%E3%82%A2%E3%83%B3%E3%82%B1%E3%83%BC%E3%83%88
* https://surveys.cfapps.io/docs/index.html

### Demo Scenario

```
# Create a survey
survey_id=$(curl -s -XPOST http://localhost:8080/surveys -H 'Content-Type: application/json' -d '{"survey_title":"Demo Survey","start_date_time":"2019-10-01T00:00:00.000+09:00", "end_date_time":"2019-10-05T00:00:00.000+09:00"}' | jq -r .survey_id)

# Create a question
question_id=$(curl -s -XPOST http://localhost:8080/questions -H 'Content-Type: application/json' -d '{"question_text": "How are you?", "max_choices": 1}' | jq -r .question_id)

# Map a question to a survey
curl -s -XPOST http://localhost:8080/surveys/${survey_id}/survey_questions/${question_id} -H 'Content-Type: application/json' -d '{"required": true}'

# Show a survey
curl -s http://localhost:8080/surveys/${survey_id} | jq .

# Create question choices
question_choice_id1=$(curl -s -XPOST http://localhost:8080/questions/${question_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "Excellent", "allow_free_text": false}' | jq -r .question_choice_id)
question_choice_id2=$(curl -s -XPOST http://localhost:8080/questions/${question_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "Good", "allow_free_text": false}' | jq -r .question_choice_id)
question_choice_id3=$(curl -s -XPOST http://localhost:8080/questions/${question_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "Okay", "allow_free_text": false}' | jq -r .question_choice_id)
question_choice_id4=$(curl -s -XPOST http://localhost:8080/questions/${question_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "Other", "allow_free_text": true}' | jq -r .question_choice_id)

# Create answers
curl -s -XPOST http://localhost:8080/surveys/${survey_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question_id}\", \"respondent_id\": \"demo1\", \"details\": [{\"question_choice_id\": \"${question_choice_id1}\"}]}"
curl -s -XPOST http://localhost:8080/surveys/${survey_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question_id}\", \"respondent_id\": \"demo2\", \"details\": [{\"question_choice_id\": \"${question_choice_id2}\"}]}"
curl -s -XPOST http://localhost:8080/surveys/${survey_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question_id}\", \"respondent_id\": \"demo3\", \"details\": [{\"question_choice_id\": \"${question_choice_id3}\"}]}"
curl -s -XPOST http://localhost:8080/surveys/${survey_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question_id}\", \"respondent_id\": \"demo4\", \"details\": [{\"question_choice_id\": \"${question_choice_id4}\", \"answer_text\": \"Not Bad\"}]}"

# Show answers
curl -s http://localhost:8080/surveys/${survey_id}/answers | jq .
```