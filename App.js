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
  TouchableOpacity,
  NativeEventEmitter
} from 'react-native';
import ButtonA from './button';

const { PrintModule } = NativeModules;
const printStateEmitter = new NativeEventEmitter(PrintModule);
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
    this._listen = printStateEmitter.addListener('PRINT_PROCESS', () => {
      alert('success');
    });
  }
  componentWillUnmount() {
    printStateEmitter.removeListener(this._listen);
  }

  _printWithMergeBitMap = async () => {
    const data = await PrintModule.printTicket(
      'CÔNG TY CP ĐẦU TƯ PHÁT TRIỂN HẠ TẦNG QUẢNG NAM',
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
    console.log(data);
  };

  _printTest = () => {
    PrintModule.printTest(
      'CÔNG TY CP ĐẦU TƯ PHÁT TRIỂN HẠ TẦNG QUẢNG NAM',
      '90 Phan Bội Châu, P.Tân Thạnh, Tam Kỳ, Quảng Nam',
      '02353.555.111',
      '4000806573',
      '01VEDB2/005',
      'ĐÀO CÔNG DANH',
      'ĐÀO CÔNG TANH',
      '0029813'
    );
  };

  _check = () => {
    PrintModule.readData();
  };

  _printCard = async () => {
    const a = await PrintModule.printCard(
      'CÔNG TY CP ĐẦU TƯ PHÁT TRIỂN HẠ TẦNG QUẢNG NAM',
      '90 Phan Bội Châu, P.Tân Thạnh, Tam Kỳ, Quảng Nam',
      '02353.555.111',
      '4000806573',
      '01VEDB2/005',
      '123123EDF',
      'ĐÀO CÔNG TANH',
      '5.000',
      '232.000',
      '17:32:11',
      '6/11/2017'
    );
    console.log(a);
  };
  abc = data => {
    alert(data);
  };

  _printTotal = () => {
    const a = JSON.stringify([
      { price: '30.000', qty: '123' },
      { price: '30.000', qty: '123' },
      { price: '30.000', qty: '123' },
      { price: '30.000', qty: '123' }
    ]);
    PrintModule.printTotal(
      'CÔNG TY CP ĐẦU TƯ PHÁT TRIỂN HẠ TẦNG QUẢNG NAM',
      '90 Phan Bội Châu, P.Tân Thạnh, Tam Kỳ, Quảng Nam',
      '02353.555.111',
      '4000806573',
      '01VEDB2/005',
      'ĐÀO CÔNG TANH',
      'ĐÀO CÔNG TANH',
      '123123EDF',
      '5.500',
      '11.000',
      '39.000',
      a,
      '6/11/2017-7:32:11',
      '7:32:11',
      '6/11/2017'
    );
  };

  _testCallback = () => {
    console.log('ads');
  };
  render() {
    return (
      <View style={styles.container}>
        <Text onPress={() => PrintModule.init()} style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <ButtonA title={'das'} onDetect={this.abc} />
        <TouchableOpacity
          style={styles.button}
          onPress={() => PrintModule.printText()}
        >
          <Text style={styles.instructions}>print text</Text>
        </TouchableOpacity>
        <TouchableOpacity
          activeOpacity={1}
          style={styles.button}
          onPress={this._printWithMergeBitMap}
        >
          <Text style={styles.instructions}>printWithMergeBitMap</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={this._printTest}>
          <Text style={styles.instructions}>printTest</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={this._printCard}>
          <Text style={styles.instructions}>print card</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={this._printTotal}>
          <Text style={styles.instructions}>print total</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={this._testCallback}>
          <Text style={styles.instructions}>print callback</Text>
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
