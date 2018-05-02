import React, { Component } from 'react';
import axios from "axios";

class ChineseVocab extends Component {

  constructor(props) {
    super(props);
    this.state = {
      vocab: props.character,
      definition: ""
    };
  }

  componentDidMount() {
    const url = "http://localhost:5000/v1/chinese/definition/" + this.state.vocab;

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
      .catch(error => console.log(error));
  }

  render() {
    return (
      <div className="ChineseVocab">
        {this.state.vocab} - {this.state.definition}<br />
      </div>
    );
  }
}

export default ChineseVocab;
