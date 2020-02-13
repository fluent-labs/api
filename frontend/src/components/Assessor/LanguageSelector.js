import React from "react";
import PropTypes from "prop-types";
import { Button } from "semantic-ui-react";

const LanguageSelector = props => {
  const { onLanguageSelected } = props;

  return (
    <div>
      <Button positive onClick={() => onLanguageSelected("ENGLISH")}>
        🇺🇸 Read English
      </Button>
      <Button positive onClick={() => onLanguageSelected("SPANISH")}>
        🇪🇸 Leer español
      </Button>
      <Button positive onClick={() => onLanguageSelected("CHINESE")}>
        🇨🇳 读中文
      </Button>
      <Button positive onClick={() => onLanguageSelected("DANISH")}>
        🇩🇰 Læse Dansk
      </Button>
    </div>
  );
};

LanguageSelector.propTypes = {
  onLanguageSelected: PropTypes.func.isRequired
};

export default LanguageSelector;
