import React, { Component } from 'react';
import axios from "axios";

class Definition extends Component {

  constructor(props) {
    super(props);
    this.state = {
      vocab: props.character,
      language: props.language,
      definition: ""
    };
  }

  componentDidMount() {
    const url = "https://" + window.location.hostname + "/dictionary/v1/" + this.state.language + "/definition/" + this.state.vocab;

    axios
      .get(url)
      .then(response => {
        const responseBody = response.data;

        if (responseBody.status === "OK") {
          const entries = responseBody.entries;

          if (entries.length === 0) {
            this.setState({definition: "No entry found"});
          }
          else {
            const definitions = entries[0].definitions.join(", ");
            this.setState({definition: definitions});
          }
        }
      })
      .catch(error => {
        this.setState({definition: "Error loading definition"});
        console.log(error);
      });
  }

  render() {
    return (
      <div className="Definition">
        {this.state.vocab} - {this.state.definition}<br />
      </div>
    );
  }
}

export default Definition;
