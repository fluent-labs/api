import React, { Component } from "react";
import LanguageSelector from "./LanguageSelector";

class Assessor extends Component {
  state = {
    language: "",
    text: "",
    submitted: false
  };

  handleSubmit = ({ text, language }) => {
    this.setState({
      language: language,
      submitted: true,
      text: text
    });
  };

  render = () => {
    return <LanguageSelector />;
  };
}

export default Assessor;
