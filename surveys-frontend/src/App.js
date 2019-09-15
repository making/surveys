import React, {Component} from 'react';
import './App.css';

class App extends Component {

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
            })
    }

    componentDidMount() {
        this.loadFromServer();
    }

    deleteSurvey(surveyId) {
        fetch(`/surveys/${surveyId}`,
            {
                method: 'DELETE'
            })
            .then(__ => this.loadFromServer());
    }

    render() {
        const surveys = this.state.surveys.map(survey =>
            <tr key={survey.survey_id}>
                <td>{survey.survey_id}</td>
                <td>{survey.survey_title}</td>
                <td>{survey.start_date_time}</td>
                <td>{survey.start_date_time}</td>
                <td>
                    <button onClick={() => this.deleteSurvey(survey.survey_id)}>Delete</button>
                </td>
            </tr>
        );
        return (
            <div>
                <p><a href={"/docs/index.html"}>API Document</a></p>
                <table>
                    <tbody>
                    <tr>
                        <th>Survey ID</th>
                        <th>Survey Title</th>
                        <th>Start Date Time</th>
                        <th>End Date Time</th>
                        <th>Delete</th>
                    </tr>
                    {surveys}
                    </tbody>
                </table>
            </div>
        );
    }
}

export default App;
