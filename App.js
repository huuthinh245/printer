/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  View,
  NativeModules,
  TouchableOpacity
} from 'react-native';

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
  android:
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu'
});
const { PrintModule } = NativeModules;
type Props = {};
export default class App extends Component<Props> {
  constructor(props) {
    super(props);
    this.state = {
      flag: true
    };
  }
  componentDidMount() {
    PrintModule.init();
  }

  _printWithMergeBitMap = () => {
    PrintModule.printWithMergeBitMap(
      'CÔNG TY CP ĐẦU TƯ HẠ TẦNG QUẢNG NAM',
      '90 Phan Bội Châu, P.Tân Thạnh, Tam Kỳ, Quảng Nam',
      '02353.555.111',
      '4000806573',
      '01VEDB2/005',
      'QN/18T',
      '0029813',
      'VÉ XE BUÝT THEO LƯỢT',
      '33',
      'Tam Kỳ',
      'ĐÀO CÔNG DANH',
      '5.000',
      '6/11/2017  17:32:11'
    );
  };
  render() {
    return (
      <View style={styles.container}>
        <Text onPress={() => PrintModule.init()} style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text
          style={styles.instructions}
          onPress={() => PrintModule.printTwoBitMap()}
        >
          printTwoBitMap
        </Text>
        <TouchableOpacity
          activeOpacity={1}
          style={styles.button}
          onPress={this._printWithMergeBitMap}
        >
          <Text style={styles.instructions}>printWithMergeBitMap</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button}>
          <Text
            style={styles.instructions}
            onPress={() => PrintModule.printTwoBitMap()}
          >
            printTwoBitMap
          </Text>
        </TouchableOpacity>
        <Text
          style={styles.instructions}
          onPress={() => PrintModule.printBarCode(1, 300, 300, '1321')}
        >
          print barcode
        </Text>
        <TouchableOpacity style={styles.button}>
          <Text onPress={this._printWithBitmap} style={styles.instructions}>
            print bitmap
          </Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF'
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10
  },
  instructions: {
    textAlign: 'center',
    color: 'white',
    margin: 10
  },
  button: {
    backgroundColor: 'blue',
    margin: 2
  }
});
