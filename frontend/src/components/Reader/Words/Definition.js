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

export default Definition;
