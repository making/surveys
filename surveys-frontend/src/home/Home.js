import React, {Component} from 'react';
import {Panel} from 'pivotal-ui/react/panels';

class Home extends Component {
    render() {
        return (<Panel>
                <h2>Home</h2>
                <p><a href={"/docs/index.html"}>APIドキュメント</a></p>
            </Panel>
        );
    }
}

export default Home;
