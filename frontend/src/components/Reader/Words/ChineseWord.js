import React from "react";
import PropTypes from "prop-types";

import { Card } from "semantic-ui-react";
import Definition from "./Definition";

const ChineseWord = props => {
  const { text, partOfSpeech, definitions, hsk, pinyin } = props;

  let header = text;
  if (pinyin && pinyin != null) {
    header += ` (${pinyin.join(", ")})`;
  }

  return (
    <Card>
      <Card.Content>
        <Card.Header>{header}</Card.Header>
        <Card.Meta>
          <span>
            {partOfSpeech}
            {hsk && hsk != null && ` - HSK: ${hsk}`}
          </span>
        </Card.Meta>
        <Card.Description>
          <Definition definitions={definitions} />
        </Card.Description>
      </Card.Content>
    </Card>
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
