import React from "react";
import PropTypes from "prop-types";

import { List } from "semantic-ui-react";
import Definition from "./Definition";

const ChineseWord = props => {
  const { text, partOfSpeech, definitions, hsk, pinyin } = props;

  let header = text;
  if (pinyin && pinyin != null) {
    header += ` (${pinyin.join(", ")})`;
  }
  if (partOfSpeech && partOfSpeech != null) {
    header += ` - ${partOfSpeech}`;
  }
  if (hsk && hsk != null) {
    header += ` HSK: ${hsk}`;
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

ChineseWord.propTypes = {
  language: PropTypes.string.isRequired,
  text: PropTypes.string.isRequired,
  partOfSpeech: PropTypes.string,
  lemma: PropTypes.string,
  definitions: PropTypes.array,
  hsk: PropTypes.number,
  pinyin: PropTypes.array
};

export default ChineseWord;
