## Survey API

https://scrapbox.io/kawasima/%E3%82%A2%E3%83%B3%E3%82%B1%E3%83%BC%E3%83%88


### Demo Scenario

```
# Create a survey
surveyId=$(curl -XPOST http://localhost:8080/surveys -H 'Content-Type: application/json' -d '{}' | jq -r .surveyId)

# Create a question
questionId=$(curl -XPOST http://localhost:8080/questions -H 'Content-Type: application/json' -d '{"questionText": "How are you?", "maxChoices": 1}' | jq -r .questionId)

# Map a question to a survey
curl -XPOST http://localhost:8080/surveys/${surveyId}/survey_questions/${questionId} -H 'Content-Type: application/json' -d '{"required": true}'

# Create question choices
questionChoiceId1=$(curl -XPOST http://localhost:8080/questions/${questionId}/question_choices -H 'Content-Type: application/json' -d '{"question_choiceText": "Excellent", "allowFreeText": false}' | jq -r .questionChoiceId)
questionChoiceId2=$(curl -XPOST http://localhost:8080/questions/${questionId}/question_choices -H 'Content-Type: application/json' -d '{"question_choiceText": "Good", "allowFreeText": false}' | jq -r .questionChoiceId)
questionChoiceId3=$(curl -XPOST http://localhost:8080/questions/${questionId}/question_choices -H 'Content-Type: application/json' -d '{"question_choiceText": "Okay", "allowFreeText": false}' | jq -r .questionChoiceId)
questionChoiceId4=$(curl -XPOST http://localhost:8080/questions/${questionId}/question_choices -H 'Content-Type: application/json' -d '{"question_choiceText": "Other", "allowFreeText": true}' | jq -r .questionChoiceId)

# Create answers
curl -XPOST http://localhost:8080/surveys/${surveyId}/answers -H 'Content-Type: application/json' -d "{\"questionId\": \"${questionId}\", \"respondentId\": \"demo1\", \"details\": [{\"questionChoiceId\": \"${questionChoiceId1}\"}]}"
curl -XPOST http://localhost:8080/surveys/${surveyId}/answers -H 'Content-Type: application/json' -d "{\"questionId\": \"${questionId}\", \"respondentId\": \"demo2\", \"details\": [{\"questionChoiceId\": \"${questionChoiceId2}\"}]}"
curl -XPOST http://localhost:8080/surveys/${surveyId}/answers -H 'Content-Type: application/json' -d "{\"questionId\": \"${questionId}\", \"respondentId\": \"demo3\", \"details\": [{\"questionChoiceId\": \"${questionChoiceId3}\"}]}"
curl -XPOST http://localhost:8080/surveys/${surveyId}/answers -H 'Content-Type: application/json' -d "{\"questionId\": \"${questionId}\", \"respondentId\": \"demo4\", \"details\": [{\"questionChoiceId\": \"${questionChoiceId4}\", \"answerText\": \"Not Bad\"}]}"
```