import React from "react";
import PropTypes from "prop-types";

import { useQuery } from "@apollo/react-hooks";
import gql from "graphql-tag";

import { Card, Dimmer, Loader } from "semantic-ui-react";
import Word from "./Word";

const GET_WORDS_IN_TEXT = gql`
  query getWordsInText($text: String!) {
    wordsInText(text: $text) {
      language
      text
      partOfSpeech
      lemma
      definitions
      ... on ChineseWord {
        hsk
        pinyin
      }
    }
  }
`;

const Vocabulary = props => {
  const { text, submitted } = props;

  const { data, loading, error } = useQuery(GET_WORDS_IN_TEXT, {
    variables: { text }
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
        <Word key={word.text} {...word} />
      ))}
    </Card.Group>
  );
};

Vocabulary.propTypes = {
  text: PropTypes.string.isRequired,
  submitted: PropTypes.bool.isRequired
};

export default Vocabulary;
