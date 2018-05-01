import React, { Component } from 'react';
import './App.css';
import axios from "axios";

class App extends Component {

  constructor(props) {
    super(props);
    this.state = {text: ''};

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({text: event.target.value});
  }

  handleSubmit(event) {
    event.preventDefault();
    const postBody = this.state.text;
    console.log(postBody);

    axios
      .post("http://localhost:8000/v1/chinese/document", {
      	"text": postBody
      })
      .then(response => {
        console.log(response);
      })
      .catch(error => console.log(error));
  }

  render() {
    return (
      <div className="App">
        <div className="form-group">
          <form onSubmit={this.handleSubmit}>
            <label>Enter some new text here:</label><br></br>
            <textarea className="form-control" rows="5" id="new-text" value={this.state.text} onChange={this.handleChange}></textarea><br></br>
            <input type="submit" value="Submit"></input>
          </form>
        </div>
      </div>
    );
  }
}

export default App;
