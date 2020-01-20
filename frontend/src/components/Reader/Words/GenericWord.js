import React from "react";
import PropTypes from "prop-types";

import { List } from "semantic-ui-react";
import ChineseWord from "./ChineseWord";
import Definition from "./Definition";

const GenericWord = props => {
  const { language, text, partOfSpeech, lemma, definitions } = props;

  if (language === "CHINESE") {
    return <ChineseWord {...props} />;
  } else {
    let header = text;
    if (lemma && lemma != null) {
      header += ` (${lemma})`;
    }
    if (partOfSpeech && partOfSpeech != null) {
      header += ` - ${partOfSpeech}`;
    }

    return (
      <List.Item>
        <List.Content>
          <List.Header>{header}</List.Header>
          <Definition definitions={definitions} />
        </List.Content>
      </List.Item>
    );
  }
};

GenericWord.propTypes = {
  language: PropTypes.string.isRequired,
  text: PropTypes.string.isRequired,
  partOfSpeech: PropTypes.string,
  lemma: PropTypes.string,
  definitions: PropTypes.array
};

export default GenericWord;
