import React, { Component } from "react";
import PropTypes from "prop-types";
import axios from "axios";

class Definition extends Component {
  constructor(props) {
    super(props);
    this.state = {
      definition: ""
    };
  }

  componentDidMount() {
    const urlBase = "https://" + window.location.hostname + "/dictionary/v1/";
    const url =
      urlBase + this.props.language + "/definition/" + this.props.vocab;

    axios
      .get(url)
      .then(response => {
        const responseBody = response.data;

        if (responseBody.status === "OK") {
          const entries = responseBody.entries;

          if (entries.length === 0) {
            this.setState({ definition: "No entry found" });
          } else {
            const definitions = entries[0].definitions.join(", ");
            this.setState({ definition: definitions });
          }
        }
      })
      .catch(error => {
        this.setState({ definition: "Error loading definition" });
      });
  }

  render() {
    return (
      <div className="Definition">
        {this.state.vocab} - {this.state.definition}
        <br />
      </div>
    );
  }
}

Definition.propTypes = {
  language: PropTypes.String.isRequired,
  vocab: PropTypes.String.isRequired
};

export default Definition;
