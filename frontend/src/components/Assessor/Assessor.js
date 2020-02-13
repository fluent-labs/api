import React, { Component } from "react";
import LanguageSelector from "./LanguageSelector";

class Assessor extends Component {
  state = {
    language: ""
  };

  handleLanguageSelect = language => {
    this.setState({ language: language });
  };

  render = () => {
    return <LanguageSelector onLanguageSelected={this.handleLanguageSelect} />;
  };
}

export default Assessor;
