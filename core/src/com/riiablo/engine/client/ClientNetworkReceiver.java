package com.riiablo.engine.client;

import com.google.flatbuffers.ByteBufferUtil;
import com.google.flatbuffers.Table;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.physics.box2d.Body;
import com.riiablo.CharData;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.Engine;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.MapWrapper;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.map.Map;
import com.riiablo.net.packet.d2gs.AngleP;
import com.riiablo.net.packet.d2gs.ClassP;
import com.riiablo.net.packet.d2gs.CofAlphasP;
import com.riiablo.net.packet.d2gs.CofComponentsP;
import com.riiablo.net.packet.d2gs.CofTransformsP;
import com.riiablo.net.packet.d2gs.ComponentP;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.DS1ObjectWrapperP;
import com.riiablo.net.packet.d2gs.Disconnect;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.MonsterP;
import com.riiablo.net.packet.d2gs.PlayerP;
import com.riiablo.net.packet.d2gs.PositionP;
import com.riiablo.net.packet.d2gs.VelocityP;
import com.riiablo.net.packet.d2gs.WarpP;
import com.riiablo.util.ArrayUtils;
import com.riiablo.util.DebugUtils;
import com.riiablo.widget.TextArea;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

@All
public class ClientNetworkReceiver extends IntervalSystem {
  private static final String TAG = "ClientNetworkReceiver";
  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_PACKET  = DEBUG && !true;
  private static final boolean DEBUG_SYNC    = DEBUG && !true;

//  protected ComponentMapper<Networked> mNetworked;
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofTransforms> mCofTransforms;
  protected ComponentMapper<CofAlphas> mCofAlphas;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<Player> mPlayer;
  protected ComponentMapper<Box2DBody> mBox2DBody;
  protected ComponentMapper<MapWrapper> mMapWrapper;

  protected CofManager cofs;
  protected NetworkIdManager syncIds;

  @Wire(name="client.socket")
  protected Socket socket;

  @Wire(name = "factory")
  protected EntityFactory factory;

  @Wire(name = "map")
  protected Map map;

  @Wire(name = "output")
  protected TextArea output;

  private final ByteBuffer buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
  private final EntitySync sync = new EntitySync();

  public ClientNetworkReceiver() {
    super(null, 1 / 60f);
  }

  @Override
  protected void processSystem() {
    InputStream in = socket.getInputStream();
    try {
      if (in.available() > 0) {
        ReadableByteChannel channel = Channels.newChannel(in);
        buffer.clear();
        int i = channel.read(buffer);
        buffer.rewind().limit(i);
        D2GS d2gs = new D2GS();
        int p = 0;
        while (buffer.hasRemaining()) {
          int size = ByteBufferUtil.getSizePrefix(buffer);
          D2GS.getRootAsD2GS(ByteBufferUtil.removeSizePrefix(buffer), d2gs);
          if (DEBUG_PACKET) Gdx.app.debug(TAG, p++ + " packet type " + D2GSData.name(d2gs.dataType()) + ":" + size + "B");
          process(d2gs);
//          System.out.println(buffer.position() + "->" + (buffer.position() + size + 4));
          buffer.position(buffer.position() + size + 4);
        }
      }
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
  }

  private void process(D2GS packet) {
    switch (packet.dataType()) {
      case D2GSData.Connection:
        Connection(packet);
        break;
      case D2GSData.Disconnect:
        Disconnect(packet);
        break;
      case D2GSData.EntitySync:
        Synchronize(packet);
        break;
      default:
        Gdx.app.error(TAG, "Unknown packet type: " + packet.dataType());
    }
  }

  private void Connection(D2GS packet) {
    Connection connection = (Connection) packet.data(new Connection());
    String charName = connection.charName();
    int charClass = connection.charClass();

    output.appendText(Riiablo.string.format(3641, charName));
    output.appendText("\n");

    Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    if (origin == null) origin = map.find(Map.ID.TOWN_ENTRY_2);
    if (origin == null) origin = map.find(Map.ID.TP_LOCATION);
    int entityId = factory.createPlayer(charName, charClass, origin.x, origin.y);
    syncIds.put(connection.entityId(), entityId);
    int[] component = mCofComponents.get(entityId).component;
//    for (int i = 0; i < 16; i++) component[i] = connection.cofComponents(i);
    float[] alpha = mCofAlphas.get(entityId).alpha;
//    for (int i = 0; i < 16; i++) alpha[i] = connection.cofAlphas(i) / 255f;
    byte[] transform = mCofTransforms.get(entityId).transform;
//    for (int i = 0; i < 16; i++) transform[i] = (byte) connection.cofTransforms(i);

    int alphaFlags = Dirty.NONE;
    int transformFlags = Dirty.NONE;
    for (int i = 0; i < 16; i++) {
      cofs.setComponent(entityId, i, connection.cofComponents(i));
    }
    for (int i = 0; i < 16; i++) {
      alphaFlags |= cofs.setAlpha(entityId, i, connection.cofAlphas(i) / 255f);
      transformFlags |= cofs.setTransform(entityId, i, (byte) connection.cofTransforms(i));
    }

    cofs.updateAlpha(entityId, alphaFlags);
    cofs.updateTransform(entityId, transformFlags);
    cofs.setMode(entityId, Engine.Player.MODE_TN, true);
    cofs.setWClass(entityId, Engine.WEAPON_1HS); // TODO...

    System.out.println("  " + DebugUtils.toByteArray(ArrayUtils.toByteArray(component)));
    System.out.println("  " + Arrays.toString(alpha));
    System.out.println("  " + DebugUtils.toByteArray(transform));
  }

  private void Disconnect(D2GS packet) {
    Disconnect disconnect = (Disconnect) packet.data(new Disconnect());
    int serverEntityId = disconnect.entityId();
    int entityId = syncIds.get(serverEntityId);

    CharData data = mPlayer.get(entityId).data;

    output.appendText(Riiablo.string.format(3642, data.getD2S().header.name));
    output.appendText("\n");

    world.delete(entityId);
    Body body = mBox2DBody.get(entityId).body;
    if (body != null) ;
  }

  @Deprecated
  private int findType(EntitySync s) {
    for (int i = 0, len = s.componentLength(); i < len; i++) {
      if (s.componentType(i) == ComponentP.ClassP) {
        return ((ClassP) s.component(new ClassP(), i)).type();
      }
    }

    return -1;
  }

  private <T extends Table> T findTable(EntitySync s, byte dataType, T table) {
    ByteBuffer dataTypes = s.componentTypeAsByteBuffer();
    for (int i = 0; dataTypes.hasRemaining(); i++) {
      if (dataTypes.get() == dataType) {
        s.component(table, i);
        return table;
      }
    }

    return null;
  }

  private int createEntity(EntitySync sync) {
    assert syncIds.get(sync.entityId()) == Engine.INVALID_ENTITY;
    Class.Type type = Class.Type.valueOf(sync.type());
    switch (type) {
      case OBJ: {
        DS1ObjectWrapperP ds1ObjectWrapper = findTable(sync, ComponentP.DS1ObjectWrapperP, new DS1ObjectWrapperP());
        if (ds1ObjectWrapper != null) {
          PositionP position = findTable(sync, ComponentP.PositionP, new PositionP());
          return factory.createObject(ds1ObjectWrapper.act(), ds1ObjectWrapper.type(), ds1ObjectWrapper.id(), position.x(), position.y());
        }

        return Engine.INVALID_ENTITY;
      }
      case MON: {
        DS1ObjectWrapperP ds1ObjectWrapper = findTable(sync, ComponentP.DS1ObjectWrapperP, new DS1ObjectWrapperP());
        if (ds1ObjectWrapper != null) {
          PositionP position = findTable(sync, ComponentP.PositionP, new PositionP());
          String objectType = Riiablo.files.MonPreset.getPlace(ds1ObjectWrapper.act(), ds1ObjectWrapper.id());
          MonStats.Entry monstats = Riiablo.files.monstats.get(objectType);
          return factory.createMonster(monstats, position.x(), position.y());
        } else {
          PositionP position = findTable(sync, ComponentP.PositionP, new PositionP());
          MonsterP monster = findTable(sync, ComponentP.MonsterP, new MonsterP());
          return factory.createMonster(monster.monsterId(), position.x(), position.y());
        }
      }
      case PLR: {
        PlayerP player = findTable(sync, ComponentP.PlayerP, new PlayerP());
        PositionP position = findTable(sync, ComponentP.PositionP, new PositionP());
        int entityId = factory.createPlayer(player.charName(), player.charClass(), position.x(), position.y());
        cofs.setMode(entityId, Engine.Player.MODE_TN);
        cofs.setWClass(entityId, Engine.WEAPON_1HS); // TODO...
        return entityId;
      }
      case ITM: {
        return Engine.INVALID_ENTITY;
      }
      case WRP: {
        WarpP warp = findTable(sync, ComponentP.WarpP, new WarpP());
        PositionP position = findTable(sync, ComponentP.PositionP, new PositionP());
        int entityId = factory.createWarp(warp.index(), position.x(), position.y());
        Map.Zone zone = mMapWrapper.get(entityId).zone;
        zone.addWarp(entityId);
        return entityId;
      }
      case MIS: {
        return Engine.INVALID_ENTITY;
      }
      default:
        return Engine.INVALID_ENTITY;
    }
  }



  private void Synchronize(D2GS packet) {
    packet.data(sync);
    Synchronize(sync);
  }

  private void Synchronize(EntitySync entityData) {
    int entityId = syncIds.get(entityData.entityId());
    if (entityId == Engine.INVALID_ENTITY) {
      syncIds.put(entityData.entityId(), entityId = createEntity(entityData));
    }

    int tFlags = Dirty.NONE;
    int aFlags = Dirty.NONE;
    if (DEBUG_SYNC) Gdx.app.debug(TAG, "syncing " + entityId);
    for (int i = 0, len = entityData.componentLength(); i < len; i++) {
      switch (entityData.componentType(i)) {
        case ComponentP.ClassP:
        case ComponentP.PlayerP:
        case ComponentP.DS1ObjectWrapperP:
        case ComponentP.WarpP:
        case ComponentP.MonsterP:
          break;
        case ComponentP.CofComponentsP: {
          CofComponentsP data = (CofComponentsP) entityData.component(new CofComponentsP(), i);
          for (int j = 0, s0 = data.componentLength(); j < s0; j++) {
            cofs.setComponent(entityId, j, (byte) data.component(j));
          }
          break;
        }
        case ComponentP.CofTransformsP: {
          CofTransformsP data = (CofTransformsP) entityData.component(new CofTransformsP(), i);
          for (int j = 0, s0 = data.transformLength(); j < s0; j++) {
            tFlags |= cofs.setTransform(entityId, j, (byte) data.transform(j));
          }
          break;
        }
        case ComponentP.CofAlphasP: {
          CofAlphasP data = (CofAlphasP) entityData.component(new CofAlphasP(), i);
          for (int j = 0, s0 = data.alphaLength(); j < s0; j++) {
            aFlags |= cofs.setAlpha(entityId, j, data.alpha(j) / 255f);
          }
          break;
        }
        case ComponentP.PositionP: {
          Vector2 position = mPosition.get(entityId).position;
          PositionP data = (PositionP) entityData.component(new PositionP(), i);
          position.x = data.x();
          position.y = data.y();
          if (mBox2DBody.has(entityId)) {
            Body body = mBox2DBody.get(entityId).body;
            if (body != null) body.setTransform(position, body.getAngle());
          }
          //Gdx.app.log(TAG, "  " + position);
          break;
        }
        case ComponentP.VelocityP: {
          Vector2 velocity = mVelocity.get(entityId).velocity;
          VelocityP data = (VelocityP) entityData.component(new VelocityP(), i);
          velocity.x = data.x();
          velocity.y = data.y();
          //Gdx.app.log(TAG, "  " + velocity);
          break;
        }
        case ComponentP.AngleP: {
          Vector2 angle = mAngle.get(entityId).target;
          AngleP data = (AngleP) entityData.component(new AngleP(), i);
          angle.x = data.x();
          angle.y = data.y();
          //Gdx.app.log(TAG, "  " + angle);
          break;
        }
        default:
          Gdx.app.error(TAG, "Unknown packet type: " + ComponentP.name(entityData.componentType(i)));
      }
    }

    cofs.updateTransform(entityId, tFlags);
    cofs.updateAlpha(entityId, aFlags);
  }
}
