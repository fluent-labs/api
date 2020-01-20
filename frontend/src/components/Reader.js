import React from "react";
import { Button, Form, Select, TextArea } from "semantic-ui-react";

const Reader = props => {
  const languages = [
    { key: "en", value: "ENGLISH", text: "English" },
    { key: "es", value: "SPANISH", text: "Spanish" },
    { key: "zh", value: "CHINESE", text: "Chinese" }
  ];

  return (
    <React.Fragment>
      <Form>
        <TextArea placeholder="Enter some text that you would like to read" />
        <Select placeholder="Which language is it in?" options={languages} />
        <Button type="submit">Submit</Button>
      </Form>
    </React.Fragment>
  );
};

export default Reader;
