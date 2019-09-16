import React, {Component} from 'react';
import {Panel} from 'pivotal-ui/react/panels';
import {DangerButton} from 'pivotal-ui/react/buttons';
import {Redirect} from "react-router-dom";
import {Icon} from 'pivotal-ui/react/iconography';

class Question extends Component {

    state = {
        question: {
            question_choices: []
        },
        disableButton: false,
        deleted: false
    };

    loadFromServer() {
        fetch(`/questions/${this.props.match.params.id}`)
            .then(r => r.json())
            .then(json => {
                this.setState({
                    question: json
                });
            });
    }

    componentDidMount() {
        this.loadFromServer();
    }

    deleteQuestion() {
        this.setState({
            disableButton: true
        });
        fetch(`/questions/${this.props.match.params.id}`,
            {
                method: 'DELETE'
            })
            .catch(e => {
                alert("Error! " + e);
                console.error(e);
            })
            .then(() => this.setState({
                disableButton: false,
                deleted: true
            }));
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
            return <Redirect to="/_/questions"/>;
        }
        const questionsChoices = this.state.question.question_choices.map(qc =>
            <li key={qc.question_choice_id}>
                {qc.question_choice_text} {qc.allow_free_text && <span>(自由記述可)</span>}
                <DangerButton flat={true}
                              disabled={this.state.disableButton}
                              icon={<Icon src="trash"/>}
                              aria-label={"Delete"}
                              iconOnly={true}
                              onClick={() => window.confirm(`"${qc.question_choice_text}"を削除しますか？`) && this.deleteQuestionChoice(qc)}/>
            </li>);

        return (
            <Panel>
                <h2>{this.state.question.question_text}</h2>
                <ul>
                    {questionsChoices}
                </ul>
                <DangerButton disabled={this.state.disableButton}
                              icon={<Icon src="trash"/>}
                              aria-label={"Delete"}
                              iconOnly={true}
                              onClick={() => window.confirm(`"${this.state.question.question_text}"を削除しますか？`) && this.deleteQuestion()}/>
            </Panel>
        );
    }
}

export default Question;
