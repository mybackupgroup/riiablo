// automatically generated by the FlatBuffers compiler, do not modify

package com.riiablo.net.packet.d2gs;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class CofComponents extends Table {
  public static CofComponents getRootAsCofComponents(ByteBuffer _bb) { return getRootAsCofComponents(_bb, new CofComponents()); }
  public static CofComponents getRootAsCofComponents(ByteBuffer _bb, CofComponents obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public CofComponents __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int component(int j) { int o = __offset(4); return o != 0 ? bb.get(__vector(o) + j * 1) & 0xFF : 0; }
  public int componentLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer componentAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer componentInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }

  public static int createCofComponents(FlatBufferBuilder builder,
      int componentOffset) {
    builder.startObject(1);
    CofComponents.addComponent(builder, componentOffset);
    return CofComponents.endCofComponents(builder);
  }

  public static void startCofComponents(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addComponent(FlatBufferBuilder builder, int componentOffset) { builder.addOffset(0, componentOffset, 0); }
  public static int createComponentVector(FlatBufferBuilder builder, byte[] data) { builder.startVector(1, data.length, 1); for (int i = data.length - 1; i >= 0; i--) builder.addByte(data[i]); return builder.endVector(); }
  public static void startComponentVector(FlatBufferBuilder builder, int numElems) { builder.startVector(1, numElems, 1); }
  public static int endCofComponents(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

