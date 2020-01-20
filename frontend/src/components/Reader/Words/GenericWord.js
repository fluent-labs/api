import React from "react";
import PropTypes from "prop-types";

import { List } from "semantic-ui-react";

const Definition = props => {
  const { definitions } = props;

  if (definitions && definitions != null) {
    return (
      <List.Description>
        <List>
          {definitions.map(definition => {
            return <List.Header key={definition}>{definition}</List.Header>;
          })}
        </List>
      </List.Description>
    );
  } else {
    return <List.Description>No definitions found</List.Description>;
  }
};
Definition.propTypes = {
  definitions: PropTypes.array
};

const GenericWord = props => {
  const { language, text, partOfSpeech, lemma, definitions } = props;
  // TODO use language to give specific components for specific languages

  let header = text;
  if (lemma) {
    header += ` (${lemma})`;
  }
  if (partOfSpeech) {
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
};

GenericWord.propTypes = {
  language: PropTypes.string.isRequired,
  text: PropTypes.string.isRequired,
  partOfSpeech: PropTypes.string,
  lemma: PropTypes.string,
  definitions: PropTypes.array
};

export default GenericWord;
