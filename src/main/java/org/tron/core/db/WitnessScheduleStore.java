package org.tron.core.db;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.ByteArray;
import org.tron.core.capsule.BytesCapsule;

@Slf4j
public class WitnessScheduleStore extends TronStoreWithRevoking<BytesCapsule>{
  private static final byte[] ACTIVE_WITNESSES = "active_witnesses".getBytes();
  private static final byte[] CURRENT_SHUFFLED_WITNESSES = "current_shuffled_witnesses".getBytes();

  private static final int ADDRESS_BYTE_ARRAY_LENGTH = 21;

  private WitnessScheduleStore(String dbName) {
    super(dbName);
  }


  @Override
  public void delete(byte[] key) {

  }

  @Override
  public BytesCapsule get(byte[] key) {
    return null;
  }

  @Override
  public boolean has(byte[] key) {
    return false;
  }

  private static WitnessScheduleStore instance;

  public void destroy() {
    instance = null;
  }

  /**
   * create fun.
   *
   * @param dbName the name of database
   */

  public static WitnessScheduleStore create(String dbName) {
    if (instance == null) {
      synchronized (WitnessScheduleStore.class) {
        if (instance == null) {
          instance = new WitnessScheduleStore(dbName);
        }
      }
    }
    return instance;
  }


  private void saveData(byte[] species, List<ByteString> witnessesAddressList) {
    byte[] ba = new byte[witnessesAddressList.size() * ADDRESS_BYTE_ARRAY_LENGTH];
    int i = 0;
    for (ByteString address : witnessesAddressList){
      System.arraycopy(address.toByteArray(), 0,
          ba, i * ADDRESS_BYTE_ARRAY_LENGTH, ADDRESS_BYTE_ARRAY_LENGTH);
//      logger.debug("saveCurrentShuffledWitnesses--ba:" + ByteArray.toHexString(ba));
      i++;
    };
    this.put(species, new BytesCapsule(ba));
  }

  private List<ByteString> getData(byte[] species) {
    List<ByteString> witnessesAddressList = new ArrayList<>();
    return Optional.ofNullable(this.dbSource.getData(species))
        .map(ba -> {
          int len = ba.length / ADDRESS_BYTE_ARRAY_LENGTH;
          for (int i = 0; i < len; ++i){
            byte[] b = new byte[ADDRESS_BYTE_ARRAY_LENGTH];
            System.arraycopy(ba, i * ADDRESS_BYTE_ARRAY_LENGTH, b, 0, ADDRESS_BYTE_ARRAY_LENGTH);
//            logger.debug("address number" + i + ":" + ByteArray.toHexString(b));
            witnessesAddressList.add(ByteString.copyFrom(b));
          }
          logger.debug("getWitnesses:" + ByteArray.toStr(species) + witnessesAddressList);
          return witnessesAddressList;
        }).orElseThrow(
            () -> new IllegalArgumentException("not found " + ByteArray.toStr(species) + "Witnesses"));
  }

  public void saveActiveWitnesses(List<ByteString> witnessesAddressList) {
//    logger.debug("saveActiveWitnesses:" + witnessesAddressList);
    saveData(ACTIVE_WITNESSES, witnessesAddressList);
  }

  public List<ByteString> getActiveWitnesses() {
    return getData(ACTIVE_WITNESSES);
  }

//  ByteArray.toHexString(scheduledWitness.toByteArray())

  public void saveCurrentShuffledWitnesses(List<ByteString> witnessesAddressList) {
//    logger.debug("saveCurrentShuffledWitnesses:" + witnessesAddressList);
    saveData(CURRENT_SHUFFLED_WITNESSES, witnessesAddressList);
  }

  public List<ByteString> getCurrentShuffledWitnesses() {
    return getData(CURRENT_SHUFFLED_WITNESSES);
  }
}
