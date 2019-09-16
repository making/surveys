import React, {Component} from 'react';
import {Panel} from 'pivotal-ui/react/panels';
import {DangerButton} from 'pivotal-ui/react/buttons';
import {Redirect} from "react-router-dom";
import {Icon} from 'pivotal-ui/react/iconography';

class Survey extends Component {

    state = {
        survey: {
            survey_questions: []
        },
        disableButton: false,
        deleted: false
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

    deleteSurvey() {
        this.setState({
            disableButton: true
        });
        fetch(`/surveys/${this.props.match.params.id}`,
            {
                method: 'DELETE'
            })
            .then(() => this.setState({
                disableButton: false,
                deleted: true
            }));
    }

    deleteSurveyQuestion(surveyQuestion) {
        this.setState({
            disableButton: true
        });
        fetch(`/surveys/${surveyQuestion.survey_id}/survey_questions/${surveyQuestion.question_id}`,
            {
                method: 'DELETE'
            })
            .then(() => this.setState({
                disableButton: false
            }))
            .then(() => this.loadFromServer());
    }

    deleteQuestionChoice(questionChoice) {
        this.setState({
            disableButton: true
        });
        fetch(`/question_choices/${questionChoice.question_choice_id}`,
            {
                method: 'DELETE'
            })
            .then(() => this.setState({
                disableButton: false
            }))
            .then(() => this.loadFromServer());
    }

    render() {
        if (this.state.deleted) {
            return <Redirect to="/_/surveys"/>;
        }
        const questions = this.state.survey.survey_questions.map(sq =>
            <div key={sq.question_id} style={{marginBottom: '10px'}}>
                <Panel
                    header={sq.question_text}>
                    {sq.required && <span><strong>* 必須</strong><br/></span>}
                    {sq.max_choices ? <span>{sq.max_choices}件まで回答可能<br/></span> : <span>自由記述<br/></span>}
                    {sq.question_choices &&
                    <ul>
                        {sq.question_choices.map(qc => {
                            return <li key={qc.question_choice_id}>
                                {qc.question_choice_text} {qc.allow_free_text && <span>(自由記述可)</span>}
                                <DangerButton flat={true}
                                              disabled={this.state.disableButton}
                                              icon={<Icon src="trash"/>}
                                              aria-label={"Delete"}
                                              iconOnly={true}
                                              onClick={() => window.confirm(`"${qc.question_choice_text}"を削除しますか？`) && this.deleteQuestionChoice(qc)}/>
                            </li>
                        })}
                    </ul>}
                    <DangerButton disabled={this.state.disableButton}
                                  icon={<Icon src="trash"/>}
                                  aria-label={"Delete"}
                                  iconOnly={true}
                                  onClick={() => window.confirm(`"${this.state.survey.survey_title}"から"${sq.question_text}"を除きますか？`) && this.deleteSurveyQuestion(sq)}/>
                </Panel>
            </div>);

        return (
            <Panel>
                <h2>{this.state.survey.survey_title}</h2>
                <p>公開期間: {this.state.survey.start_date_time} 〜 {this.state.survey.end_date_time}</p>
                {questions}
                <DangerButton disabled={this.state.disableButton}
                              icon={<Icon src="trash"/>}
                              aria-label={"Delete"}
                              iconOnly={true}
                              onClick={() => window.confirm(`"${this.state.survey.survey_title}"を削除しますか？`) && this.deleteSurvey()}/>
            </Panel>
        );
    }
}

export default Survey;
