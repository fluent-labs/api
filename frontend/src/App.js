import React, { Component } from "react";
import Reader from "./components/Reader/Reader";

import "./App.css";
import "semantic-ui-css/semantic.min.css";

class App extends Component {
  state = {
    language: "",
    text: "",
    data: "",
    submitted: false
  };

  handleSubmit = ({ text, language }) => {
    this.setState({
      language: language,
      submitted: true,
      text: text
    });
  };

  render() {
    return <Reader onSubmit={this.handleSubmit} />;
  }
}

export default App;
