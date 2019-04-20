/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2019-04-05")
public class FingerTable implements org.apache.thrift.TBase<FingerTable, FingerTable._Fields>, java.io.Serializable, Cloneable, Comparable<FingerTable> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("FingerTable");

  private static final org.apache.thrift.protocol.TField START_FIELD_DESC = new org.apache.thrift.protocol.TField("Start", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField INTERVAL_BEGIN_FIELD_DESC = new org.apache.thrift.protocol.TField("IntervalBegin", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField INTERVAL_END_FIELD_DESC = new org.apache.thrift.protocol.TField("IntervalEnd", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField SUCCESSOR_FIELD_DESC = new org.apache.thrift.protocol.TField("Successor", org.apache.thrift.protocol.TType.STRUCT, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new FingerTableStandardSchemeFactory());
    schemes.put(TupleScheme.class, new FingerTableTupleSchemeFactory());
  }

  public int Start; // required
  public int IntervalBegin; // required
  public int IntervalEnd; // required
  public Node Successor; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    START((short)1, "Start"),
    INTERVAL_BEGIN((short)2, "IntervalBegin"),
    INTERVAL_END((short)3, "IntervalEnd"),
    SUCCESSOR((short)4, "Successor");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // START
          return START;
        case 2: // INTERVAL_BEGIN
          return INTERVAL_BEGIN;
        case 3: // INTERVAL_END
          return INTERVAL_END;
        case 4: // SUCCESSOR
          return SUCCESSOR;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __START_ISSET_ID = 0;
  private static final int __INTERVALBEGIN_ISSET_ID = 1;
  private static final int __INTERVALEND_ISSET_ID = 2;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.START, new org.apache.thrift.meta_data.FieldMetaData("Start", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.INTERVAL_BEGIN, new org.apache.thrift.meta_data.FieldMetaData("IntervalBegin", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.INTERVAL_END, new org.apache.thrift.meta_data.FieldMetaData("IntervalEnd", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.SUCCESSOR, new org.apache.thrift.meta_data.FieldMetaData("Successor", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Node.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(FingerTable.class, metaDataMap);
  }

  public FingerTable() {
  }

  public FingerTable(
    int Start,
    int IntervalBegin,
    int IntervalEnd,
    Node Successor)
  {
    this();
    this.Start = Start;
    setStartIsSet(true);
    this.IntervalBegin = IntervalBegin;
    setIntervalBeginIsSet(true);
    this.IntervalEnd = IntervalEnd;
    setIntervalEndIsSet(true);
    this.Successor = Successor;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public FingerTable(FingerTable other) {
    __isset_bitfield = other.__isset_bitfield;
    this.Start = other.Start;
    this.IntervalBegin = other.IntervalBegin;
    this.IntervalEnd = other.IntervalEnd;
    if (other.isSetSuccessor()) {
      this.Successor = new Node(other.Successor);
    }
  }

  public FingerTable deepCopy() {
    return new FingerTable(this);
  }

  @Override
  public void clear() {
    setStartIsSet(false);
    this.Start = 0;
    setIntervalBeginIsSet(false);
    this.IntervalBegin = 0;
    setIntervalEndIsSet(false);
    this.IntervalEnd = 0;
    this.Successor = null;
  }

  public int getStart() {
    return this.Start;
  }

  public FingerTable setStart(int Start) {
    this.Start = Start;
    setStartIsSet(true);
    return this;
  }

  public void unsetStart() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __START_ISSET_ID);
  }

  /** Returns true if field Start is set (has been assigned a value) and false otherwise */
  public boolean isSetStart() {
    return EncodingUtils.testBit(__isset_bitfield, __START_ISSET_ID);
  }

  public void setStartIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __START_ISSET_ID, value);
  }

  public int getIntervalBegin() {
    return this.IntervalBegin;
  }

  public FingerTable setIntervalBegin(int IntervalBegin) {
    this.IntervalBegin = IntervalBegin;
    setIntervalBeginIsSet(true);
    return this;
  }

  public void unsetIntervalBegin() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __INTERVALBEGIN_ISSET_ID);
  }

  /** Returns true if field IntervalBegin is set (has been assigned a value) and false otherwise */
  public boolean isSetIntervalBegin() {
    return EncodingUtils.testBit(__isset_bitfield, __INTERVALBEGIN_ISSET_ID);
  }

  public void setIntervalBeginIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __INTERVALBEGIN_ISSET_ID, value);
  }

  public int getIntervalEnd() {
    return this.IntervalEnd;
  }

  public FingerTable setIntervalEnd(int IntervalEnd) {
    this.IntervalEnd = IntervalEnd;
    setIntervalEndIsSet(true);
    return this;
  }

  public void unsetIntervalEnd() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __INTERVALEND_ISSET_ID);
  }

  /** Returns true if field IntervalEnd is set (has been assigned a value) and false otherwise */
  public boolean isSetIntervalEnd() {
    return EncodingUtils.testBit(__isset_bitfield, __INTERVALEND_ISSET_ID);
  }

  public void setIntervalEndIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __INTERVALEND_ISSET_ID, value);
  }

  public Node getSuccessor() {
    return this.Successor;
  }

  public FingerTable setSuccessor(Node Successor) {
    this.Successor = Successor;
    return this;
  }

  public void unsetSuccessor() {
    this.Successor = null;
  }

  /** Returns true if field Successor is set (has been assigned a value) and false otherwise */
  public boolean isSetSuccessor() {
    return this.Successor != null;
  }

  public void setSuccessorIsSet(boolean value) {
    if (!value) {
      this.Successor = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case START:
      if (value == null) {
        unsetStart();
      } else {
        setStart((Integer)value);
      }
      break;

    case INTERVAL_BEGIN:
      if (value == null) {
        unsetIntervalBegin();
      } else {
        setIntervalBegin((Integer)value);
      }
      break;

    case INTERVAL_END:
      if (value == null) {
        unsetIntervalEnd();
      } else {
        setIntervalEnd((Integer)value);
      }
      break;

    case SUCCESSOR:
      if (value == null) {
        unsetSuccessor();
      } else {
        setSuccessor((Node)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case START:
      return getStart();

    case INTERVAL_BEGIN:
      return getIntervalBegin();

    case INTERVAL_END:
      return getIntervalEnd();

    case SUCCESSOR:
      return getSuccessor();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case START:
      return isSetStart();
    case INTERVAL_BEGIN:
      return isSetIntervalBegin();
    case INTERVAL_END:
      return isSetIntervalEnd();
    case SUCCESSOR:
      return isSetSuccessor();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof FingerTable)
      return this.equals((FingerTable)that);
    return false;
  }

  public boolean equals(FingerTable that) {
    if (that == null)
      return false;

    boolean this_present_Start = true;
    boolean that_present_Start = true;
    if (this_present_Start || that_present_Start) {
      if (!(this_present_Start && that_present_Start))
        return false;
      if (this.Start != that.Start)
        return false;
    }

    boolean this_present_IntervalBegin = true;
    boolean that_present_IntervalBegin = true;
    if (this_present_IntervalBegin || that_present_IntervalBegin) {
      if (!(this_present_IntervalBegin && that_present_IntervalBegin))
        return false;
      if (this.IntervalBegin != that.IntervalBegin)
        return false;
    }

    boolean this_present_IntervalEnd = true;
    boolean that_present_IntervalEnd = true;
    if (this_present_IntervalEnd || that_present_IntervalEnd) {
      if (!(this_present_IntervalEnd && that_present_IntervalEnd))
        return false;
      if (this.IntervalEnd != that.IntervalEnd)
        return false;
    }

    boolean this_present_Successor = true && this.isSetSuccessor();
    boolean that_present_Successor = true && that.isSetSuccessor();
    if (this_present_Successor || that_present_Successor) {
      if (!(this_present_Successor && that_present_Successor))
        return false;
      if (!this.Successor.equals(that.Successor))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_Start = true;
    list.add(present_Start);
    if (present_Start)
      list.add(Start);

    boolean present_IntervalBegin = true;
    list.add(present_IntervalBegin);
    if (present_IntervalBegin)
      list.add(IntervalBegin);

    boolean present_IntervalEnd = true;
    list.add(present_IntervalEnd);
    if (present_IntervalEnd)
      list.add(IntervalEnd);

    boolean present_Successor = true && (isSetSuccessor());
    list.add(present_Successor);
    if (present_Successor)
      list.add(Successor);

    return list.hashCode();
  }

  @Override
  public int compareTo(FingerTable other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetStart()).compareTo(other.isSetStart());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStart()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Start, other.Start);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIntervalBegin()).compareTo(other.isSetIntervalBegin());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIntervalBegin()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.IntervalBegin, other.IntervalBegin);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIntervalEnd()).compareTo(other.isSetIntervalEnd());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIntervalEnd()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.IntervalEnd, other.IntervalEnd);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSuccessor()).compareTo(other.isSetSuccessor());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSuccessor()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Successor, other.Successor);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("FingerTable(");
    boolean first = true;

    sb.append("Start:");
    sb.append(this.Start);
    first = false;
    if (!first) sb.append(", ");
    sb.append("IntervalBegin:");
    sb.append(this.IntervalBegin);
    first = false;
    if (!first) sb.append(", ");
    sb.append("IntervalEnd:");
    sb.append(this.IntervalEnd);
    first = false;
    if (!first) sb.append(", ");
    sb.append("Successor:");
    if (this.Successor == null) {
      sb.append("null");
    } else {
      sb.append(this.Successor);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
    if (Successor != null) {
      Successor.validate();
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class FingerTableStandardSchemeFactory implements SchemeFactory {
    public FingerTableStandardScheme getScheme() {
      return new FingerTableStandardScheme();
    }
  }

  private static class FingerTableStandardScheme extends StandardScheme<FingerTable> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, FingerTable struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // START
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.Start = iprot.readI32();
              struct.setStartIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // INTERVAL_BEGIN
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.IntervalBegin = iprot.readI32();
              struct.setIntervalBeginIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // INTERVAL_END
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.IntervalEnd = iprot.readI32();
              struct.setIntervalEndIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // SUCCESSOR
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.Successor = new Node();
              struct.Successor.read(iprot);
              struct.setSuccessorIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, FingerTable struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(START_FIELD_DESC);
      oprot.writeI32(struct.Start);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(INTERVAL_BEGIN_FIELD_DESC);
      oprot.writeI32(struct.IntervalBegin);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(INTERVAL_END_FIELD_DESC);
      oprot.writeI32(struct.IntervalEnd);
      oprot.writeFieldEnd();
      if (struct.Successor != null) {
        oprot.writeFieldBegin(SUCCESSOR_FIELD_DESC);
        struct.Successor.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class FingerTableTupleSchemeFactory implements SchemeFactory {
    public FingerTableTupleScheme getScheme() {
      return new FingerTableTupleScheme();
    }
  }

  private static class FingerTableTupleScheme extends TupleScheme<FingerTable> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, FingerTable struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetStart()) {
        optionals.set(0);
      }
      if (struct.isSetIntervalBegin()) {
        optionals.set(1);
      }
      if (struct.isSetIntervalEnd()) {
        optionals.set(2);
      }
      if (struct.isSetSuccessor()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetStart()) {
        oprot.writeI32(struct.Start);
      }
      if (struct.isSetIntervalBegin()) {
        oprot.writeI32(struct.IntervalBegin);
      }
      if (struct.isSetIntervalEnd()) {
        oprot.writeI32(struct.IntervalEnd);
      }
      if (struct.isSetSuccessor()) {
        struct.Successor.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, FingerTable struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.Start = iprot.readI32();
        struct.setStartIsSet(true);
      }
      if (incoming.get(1)) {
        struct.IntervalBegin = iprot.readI32();
        struct.setIntervalBeginIsSet(true);
      }
      if (incoming.get(2)) {
        struct.IntervalEnd = iprot.readI32();
        struct.setIntervalEndIsSet(true);
      }
      if (incoming.get(3)) {
        struct.Successor = new Node();
        struct.Successor.read(iprot);
        struct.setSuccessorIsSet(true);
      }
    }
  }

}
