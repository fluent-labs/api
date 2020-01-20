import React from "react";
import PropTypes from "prop-types";

import { Card } from "semantic-ui-react";
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

    return (
      <Card>
        <Card.Content>
          <Card.Header>{header}</Card.Header>
          <Card.Meta>
            <span>{partOfSpeech}</span>
          </Card.Meta>
          <Card.Description>
            <Definition definitions={definitions} />
          </Card.Description>
        </Card.Content>
      </Card>
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
