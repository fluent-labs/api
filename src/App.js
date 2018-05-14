import React, { Component } from 'react';
import './App.css';
import Definition from './Definition';

import axios from "axios";

class App extends Component {

  constructor(props) {
    super(props);
    this.state = {
      statusText: "Submit to get definitions",
      text: '',
      words: []
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({text: event.target.value});
  }

  handleSubmit(event) {
    event.preventDefault();

    this.setState({statusText: "Loading..."});

    const postBody = this.state.text;
    const apiUrl = "https://" + window.location.hostname + "/stemming/v1/chinese/document"

    axios
      .post(apiUrl, {
      	"text": postBody
      })
      .then(response => {
        const responseBody = response.data;

        if (responseBody.status === "OK") {
          this.setState({words: responseBody.words})
        }
      })
      .catch(error => {
        this.setState({statusText: "Error loading words"});
        console.log(error);
      });
  }

  render() {
    const statusText = this.state.words.length === 0 ? this.state.statusText : "Words in your text: ";

    return (
      <div className="App">
        <div className="form-group">
          <form onSubmit={this.handleSubmit}>
            <label>Enter some new text here:</label><br></br>
            <textarea className="form-control" rows="5" id="new-text" value={this.state.text} onChange={this.handleChange}></textarea><br></br>
            <input type="submit" value="Submit"></input>
          </form><br />
        </div>
        <div>
          <b>{ statusText }</b><br />
          { this.state.words.map(word => <Definition character={word} key={word} language={"chinese"} />) }
        </div>
      </div>
    );
  }
}

export default App;
