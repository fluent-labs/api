import React, { Component } from "react";
import PropTypes from "prop-types";
import { Dimmer, List, Loader } from "semantic-ui-react";

import data from "../../testData";
import LanguageInput from "./LanguageInput";
import GenericWord from "./Words/GenericWord";

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
        <List>
          {this.state.data.map(word => (
            <GenericWord key={word.text} {...word} />
          ))}
        </List>
      );
    }
  };
}

Reader.propTypes = {
  onSubmit: PropTypes.func.isRequired
};

export default Reader;
