import React from "react";
import PropTypes from "prop-types";

import { useQuery } from "@apollo/react-hooks";
import gql from "graphql-tag";

import { Card, Dimmer, Loader } from "semantic-ui-react";
import Word from "./Word";

const GET_WORDS_IN_TEXT = gql`
  query getWordsInText($language: String!, $text: String!) {
    wordsInText(language: $language, text: $text) {
      language
      token
      tag
      lemma
      definitions {
        subdefinitions
        ... on ChineseDefinition {
          pinyin
        }
      }
      ... on ChineseWord {
        hsk
      }
    }
  }
`;

const Vocabulary = props => {
  const { language, text, submitted } = props;

  const { data, loading, error } = useQuery(GET_WORDS_IN_TEXT, {
    variables: { language, text }
  });

  if (!submitted) return <p>Submit text to see some vocabulary.</p>;

  if (loading) {
    return (
      <Dimmer active>
        <Loader />
      </Dimmer>
    );
  }
  if (error) return <p>Error loading vocabulary.</p>;

  return (
    <Card.Group>
      {data.wordsInText.map(word => (
        <Word key={word.token} {...word} />
      ))}
    </Card.Group>
  );
};

Vocabulary.propTypes = {
  language: PropTypes.string.isRequired,
  text: PropTypes.string.isRequired,
  submitted: PropTypes.bool.isRequired
};

export default Vocabulary;
