import React, { Component } from "react";
import PropTypes from "prop-types";
import { Button, Form, Select, TextArea } from "semantic-ui-react";

class LanguageInput extends Component {
  state = { text: "", language: "" };

  handleChange = (e, { name, value }) => this.setState({ [name]: value });

  handleSubmit = () => {
    const { text, language } = this.state;
    this.props.onSubmit({ text: text, language: language });
  };

  render = () => {
    const { text, language } = this.state;

    const languages = [
      { key: "en", value: "ENGLISH", text: "English" },
      { key: "es", value: "SPANISH", text: "Spanish" },
      { key: "zh", value: "CHINESE", text: "Chinese" }
    ];

    return (
      <React.Fragment>
        <Form onSubmit={this.handleSubmit}>
          <TextArea
            placeholder="Enter some text that you would like to read"
            name="text"
            onChange={this.handleChange}
            value={text}
          />
          <Select
            placeholder="Which language is it in?"
            options={languages}
            name="language"
            onChange={this.handleChange}
            value={language}
          />
          <Button type="submit">Submit</Button>
        </Form>
      </React.Fragment>
    );
  };
}

LanguageInput.propTypes = {
  onSubmit: PropTypes.func.isRequired
};

export default LanguageInput;
