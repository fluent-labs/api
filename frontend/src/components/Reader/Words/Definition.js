import React from "react";
import PropTypes from "prop-types";

import { List } from "semantic-ui-react";

const Definition = props => {
  const { definitions } = props;

  if (definitions && definitions != null) {
    return (
      <List bulleted>
        {definitions.map(definition => {
          return <List.Header key={definition}>{definition}</List.Header>;
        })}
      </List>
    );
  } else {
    return <label>No definitions found</label>;
  }
};
Definition.propTypes = {
  definitions: PropTypes.array
};

export default Definition;
