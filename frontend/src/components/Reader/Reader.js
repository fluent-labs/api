import React, { Component } from "react";
import PropTypes from "prop-types";
import LanguageInput from "./LanguageInput";

import data from "../../testData";

class Reader extends Component {
  state = {
    language: "",
    text: "",
    data: "",
    submitted: false
  };

  handleSubmit = ({ text, language }) => {
    this.setState({
      data: data.data.wordsInText,
      language: language,
      submitted: true,
      text: text
    });
  };

  render = () => {
    if (!this.state.submitted) {
      return <LanguageInput onSubmit={this.handleSubmit} />;
    } else if (this.state.data === "") {
      return <label>Loading</label>;
    } else {
      return <label>Loaded</label>;
    }
  };
}

Reader.propTypes = {
  onSubmit: PropTypes.func.isRequired
};

export default Reader;
