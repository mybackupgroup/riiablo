package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.riiablo.map.Map;

@Transient
@PooledWeaver
public class MapWrapper extends Component {
  public Map      map;
  public Map.Zone zone;
}
