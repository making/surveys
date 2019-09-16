import React, {Component} from 'react';
import {Panel} from 'pivotal-ui/react/panels';
import {FlexTable, withCellLink, withRenderTdChildren, withRowLink, withSorting} from 'pivotal-ui/react/table';
import {decodeTime} from "ulid";

class Surveys extends Component {

    state = {
        surveys: []
    };

    loadFromServer() {
        fetch("/surveys")
            .then(r => r.json())
            .then(json => {
                this.setState({
                    surveys: json
                });
            });
    }

    componentDidMount() {
        this.loadFromServer();
    }

    render() {
        const columns = [{
            attribute: 'survey_title',
            displayName: 'アンケートタイトル',
            sortable: true,
            link: s => `/_/surveys/${s.survey_id}`
        }, {
            attribute: 'survey_id',
            displayName: '作成日時',
            sortable: true,
            renderTdChildren: s => {
                return new Date(decodeTime(s.survey_id)).toLocaleString();
            }
        }, {
            attribute: 'start_date_time',
            displayName: '開始予定時刻',
            sortable: true
        }, {
            attribute: 'end_date_time',
            displayName: '終了予定時刻',
            sortable: true
        }];

        const SurveyTable = withCellLink(withRenderTdChildren(withSorting(withRowLink(FlexTable))));
        const surveys = this.state.surveys;

        return (
            <Panel>
                <h2>Surveys</h2>
                <SurveyTable columns={columns} data={surveys}/>
            </Panel>
        );
    }
}

export default Surveys;
