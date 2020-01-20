import React from "react";
import PropTypes from "prop-types";

const GenericWord = props => {
  return <label>Word</label>;
};

GenericWord.propTypes = {
  language: PropTypes.string.isRequired,
  text: PropTypes.string.isRequired,
  partOfSpeech: PropTypes.string,
  lemma: PropTypes.string,
  definitions: PropTypes.array
};

export default GenericWord;
