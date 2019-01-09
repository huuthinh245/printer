import React from 'react';
import { TouchableOpacity, Text } from 'react-native';

export default (ButtonA = props => {
  const { title, onDetect } = props;
  return (
    <TouchableOpacity onPress={() => onDetect('dsds')}>
      <Text>{props.title}</Text>
    </TouchableOpacity>
  );
});
