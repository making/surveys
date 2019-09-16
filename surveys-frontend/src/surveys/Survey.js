import React, {Component} from 'react';
import {Panel} from 'pivotal-ui/react/panels';

class Survey extends Component {

    state = {
        survey: {
            survey_questions: []
        }
    };

    loadFromServer() {
        fetch(`/surveys/${this.props.match.params.id}`)
            .then(r => r.json())
            .then(json => {
                this.setState({
                    survey: json
                });
            });
    }

    componentDidMount() {
        this.loadFromServer();
    }

    deleteSurvey(surveyId) {
        fetch(`/surveys/${surveyId}`,
            {
                method: 'DELETE'
            })
            .then(() => this.loadFromServer());
    }

    render() {
        const questions = this.state.survey.survey_questions.map(sq =>
            <div style={{'margin-bottom': '10px'}}>
                <Panel key={sq.question_id}
                       header={sq.question_text}>
                    {sq.required && <span><strong>* 必須</strong><br/></span>}
                    {sq.max_choices ? <span>{sq.max_choices}件まで回答可能<br/></span> : <span>自由記述<br/></span>}
                    {sq.question_choices &&
                    <ul>
                        {sq.question_choices.map(qc => {
                            return <li key={qc.question_choice_id}>
                                {qc.question_choice_text} {qc.allow_free_text && <span>(自由記述可)</span>}
                            </li>
                        })}
                    </ul>}
                </Panel>
            </div>);

        return (
            <Panel>
                <h2>{this.state.survey.survey_title}</h2>
                <p>公開期間: {this.state.survey.start_date_time} 〜 {this.state.survey.end_date_time}</p>
                {questions}
            </Panel>
        );
    }
}

export default Survey;
