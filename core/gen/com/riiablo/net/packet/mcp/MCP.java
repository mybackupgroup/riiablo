// automatically generated by the FlatBuffers compiler, do not modify

package com.riiablo.net.packet.mcp;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class MCP extends Table {
  public static MCP getRootAsMCP(ByteBuffer _bb) { return getRootAsMCP(_bb, new MCP()); }
  public static MCP getRootAsMCP(ByteBuffer _bb, MCP obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public MCP __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public byte dataType() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table data(Table obj) { int o = __offset(6); return o != 0 ? __union(obj, o) : null; }

  public static int createMCP(FlatBufferBuilder builder,
      byte data_type,
      int dataOffset) {
    builder.startObject(2);
    MCP.addData(builder, dataOffset);
    MCP.addDataType(builder, data_type);
    return MCP.endMCP(builder);
  }

  public static void startMCP(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addDataType(FlatBufferBuilder builder, byte dataType) { builder.addByte(0, dataType, 0); }
  public static void addData(FlatBufferBuilder builder, int dataOffset) { builder.addOffset(1, dataOffset, 0); }
  public static int endMCP(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishMCPBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
  public static void finishSizePrefixedMCPBuffer(FlatBufferBuilder builder, int offset) { builder.finishSizePrefixed(offset); }
}

