import React, {Component} from 'react';
import './App.css';
import {BrowserRouter, Link, Route, Switch} from "react-router-dom";
import {NavTab} from "react-router-tabs";
import Surveys from "./surveys/Surveys";
import Home from "./home/Home";
import Survey from "./surveys/Survey";
import Questions from "./questions/Questions";
import Question from "./questions/Question";

class App extends Component {
    render() {
        return (
            <BrowserRouter>
                <div>
                    <h1><Link to="/">アンケート</Link></h1>
                    <section id={"main"}>
                        <article>
                            <NavTab exact to="/">{`Home`}</NavTab>
                            <NavTab exact to="/_/surveys">{`Surveys`}</NavTab>
                            <NavTab exact to="/_/questions">{`Questions`}</NavTab>
                            <Switch>
                                <Route exact path="/" component={Home}/>
                                <Route exact path="/_/surveys" component={Surveys}/>
                                <Route path="/_/surveys/:id" component={Survey}/>
                                <Route exact path="/_/questions" component={Questions}/>
                                <Route path="/_/questions/:id" component={Question}/>
                                <Route path="/index.html" component={Surveys}/>
                            </Switch>
                        </article>
                    </section>
                </div>
            </BrowserRouter>
        );
    }
}

export default App;
