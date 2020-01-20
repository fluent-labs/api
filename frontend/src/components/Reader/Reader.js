import React, { Component } from "react";
import PropTypes from "prop-types";
import { Card, Dimmer, Loader } from "semantic-ui-react";

import data from "../../testData";
import LanguageInput from "./LanguageInput";
import Word from "./Word";

class Reader extends Component {
  state = {
    language: "",
    text: "",
    data: "",
    submitted: false
  };

  handleSubmit = ({ text, language }) => {
    // TODO hook this up to data source
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
      return (
        <Dimmer active>
          <Loader />
        </Dimmer>
      );
    } else {
      return (
        <div>
          <LanguageInput onSubmit={this.handleSubmit} />
          <Card.Group>
            {this.state.data.map(word => (
              <Word key={word.text} {...word} />
            ))}
          </Card.Group>
        </div>
      );
    }
  };
}

Reader.propTypes = {
  onSubmit: PropTypes.func.isRequired
};

export default Reader;
