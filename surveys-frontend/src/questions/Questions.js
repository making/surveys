import React, {Component} from 'react';
import {Panel} from 'pivotal-ui/react/panels';
import {FlexTable, withCellLink, withRenderTdChildren, withRowLink, withSorting} from 'pivotal-ui/react/table';
import {decodeTime} from "ulid";

class Questions extends Component {

    state = {
        questions: []
    };

    loadFromServer() {
        fetch("/questions")
            .then(r => r.json())
            .then(json => {
                this.setState({
                    questions: json
                });
            });
    }

    componentDidMount() {
        this.loadFromServer();
    }

    render() {
        const columns = [{
            attribute: 'question_text',
            displayName: '質問文',
            sortable: true,
            link: s => `/_/questions/${s.question_id}`
        }, {
            attribute: 'question_id',
            displayName: '作成日時',
            sortable: true,
            renderTdChildren: s => {
                return new Date(decodeTime(s.question_id)).toLocaleString();
            }
        }];

        const QuestionTable = withCellLink(withRenderTdChildren(withSorting(withRowLink(FlexTable))));
        const questions = this.state.questions;

        return (
            <Panel>
                <h2>Questions</h2>
                <QuestionTable columns={columns} data={questions}/>
            </Panel>
        );
    }
}

export default Questions;
