#!/bin/bash
set -eox
API_URL=${API_URL:-http://localhost:8080}

# アンケート作成
survey1_id=$(curl -s -XPOST ${API_URL}/surveys -H 'Content-Type: application/json' -d '{"survey_title":"テストアンケート", "start_date_time":"2019-10-01T00:00:00.000+09:00", "end_date_time":"2020-10-01T00:00:00.000+09:00"}' | jq -r .survey_id)

# 選択回答設問作成
question1_id=$(curl -s -XPOST ${API_URL}/questions -H 'Content-Type: application/json' -d '{"question_text": "この設計はいけてますか?", "max_choices": 1}' | jq -r .question_id)
# 記述式回答設問作成
question2_id=$(curl -s -XPOST ${API_URL}/questions -H 'Content-Type: application/json' -d '{"question_text": "どういうところがいけてますか?"}' | jq -r .question_id)
# 選択回答設問作成
question3_id=$(curl -s -XPOST ${API_URL}/questions -H 'Content-Type: application/json' -d '{"question_text": "他にも取り上げて欲しい設計がありますか?", "max_choices": 3}' | jq -r .question_id)

# アンケートと設問をマッピング
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/survey_questions/${question1_id} -H 'Content-Type: application/json' -d '{"required": true}'
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/survey_questions/${question2_id} -H 'Content-Type: application/json' -d '{"required": false}'
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/survey_questions/${question3_id} -H 'Content-Type: application/json' -d '{"required": true}'

# アンケート表示
curl -s ${API_URL}/surveys/${survey1_id} | jq .

# 設問選択肢追加
question1_choice1_id=$(curl -s -XPOST ${API_URL}/questions/${question1_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "はい", "allow_free_text": false}' | jq -r .question_choice_id)
question1_choice2_id=$(curl -s -XPOST ${API_URL}/questions/${question1_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "いいえ", "allow_free_text": false}' | jq -r .question_choice_id)

question3_choice1_id=$(curl -s -XPOST ${API_URL}/questions/${question3_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "在庫", "allow_free_text": false}' | jq -r .question_choice_id)
question3_choice2_id=$(curl -s -XPOST ${API_URL}/questions/${question3_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "カート", "allow_free_text": false}' | jq -r .question_choice_id)
question3_choice3_id=$(curl -s -XPOST ${API_URL}/questions/${question3_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "お気に入り", "allow_free_text": false}' | jq -r .question_choice_id)
question3_choice4_id=$(curl -s -XPOST ${API_URL}/questions/${question3_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "リコメンド", "allow_free_text": false}' | jq -r .question_choice_id)
question3_choice5_id=$(curl -s -XPOST ${API_URL}/questions/${question3_id}/question_choices -H 'Content-Type: application/json' -d '{"question_choice_text": "その他", "allow_free_text": true}' | jq -r .question_choice_id)

# 選択回答作成
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question1_id}\", \"respondent_id\": \"demo1\", \"details\": [{\"question_choice_id\": \"${question1_choice1_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question1_id}\", \"respondent_id\": \"demo2\", \"details\": [{\"question_choice_id\": \"${question1_choice1_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question1_id}\", \"respondent_id\": \"demo3\", \"details\": [{\"question_choice_id\": \"${question1_choice1_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question1_id}\", \"respondent_id\": \"demo4\", \"details\": [{\"question_choice_id\": \"${question1_choice1_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question1_id}\", \"respondent_id\": \"demo5\", \"details\": [{\"question_choice_id\": \"${question1_choice2_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question1_id}\", \"respondent_id\": \"demo6\", \"details\": [{\"question_choice_id\": \"${question1_choice2_id}\"}]}"

curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question2_id}\", \"respondent_id\": \"demo1\", \"details\": [{\"answer_text\": \"具体的なデータがあってわかりやすい\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question2_id}\", \"respondent_id\": \"demo2\", \"details\": [{\"answer_text\": \"ER図がわかりやすい\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question2_id}\", \"respondent_id\": \"demo2\", \"details\": [{\"answer_text\": \"ここまで複雑なモデルが必要なの?\"}]}"

curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question3_id}\", \"respondent_id\": \"demo1\", \"details\": [{\"question_choice_id\": \"${question3_choice1_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question3_id}\", \"respondent_id\": \"demo2\", \"details\": [{\"question_choice_id\": \"${question3_choice1_id}\"}, {\"question_choice_id\": \"${question3_choice2_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question3_id}\", \"respondent_id\": \"demo3\", \"details\": [{\"question_choice_id\": \"${question3_choice2_id}\"}, {\"question_choice_id\": \"${question3_choice3_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question3_id}\", \"respondent_id\": \"demo4\", \"details\": [{\"question_choice_id\": \"${question3_choice4_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question3_id}\", \"respondent_id\": \"demo5\", \"details\": [{\"question_choice_id\": \"${question3_choice4_id}\"}]}"
curl -s -XPOST ${API_URL}/surveys/${survey1_id}/answers -H 'Content-Type: application/json' -d "{\"question_id\": \"${question3_id}\", \"respondent_id\": \"demo6\", \"details\": [{\"question_choice_id\": \"${question3_choice5_id}\", \"answer_text\": \"検索\"}]}"

# アンケート回答表示
curl -s ${API_URL}/surveys/${survey1_id}/answers | jq .